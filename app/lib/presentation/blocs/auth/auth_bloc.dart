import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:electorama_app/domain/entities/auth/user.dart';
import 'package:electorama_app/domain/repositories/auth/auth_repository.dart';
import 'package:electorama_app/infrastructure/errors/auth_errors.dart';
import 'package:electorama_app/infrastructure/repositories/auth/auth_repository_impl.dart';
import 'package:electorama_app/infrastructure/services/key_value_storage_service_impl.dart';
import 'package:electorama_app/infrastructure/services/auth_session.dart';
import 'package:electorama_app/infrastructure/services/capture_catalog.dart';

part 'auth_event.dart';
part 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final AuthRepository authRepository = AuthRepositoryImpl();
  final keyValueStorageServiceImpl = KeyValueStorageServiceImpl();


  AuthBloc() : super(AuthInitial()) {
    on<Login>(_login);
    on<Logout>(_logout);
    on<CheckAuthStatus>(_checkAuthStatus);
  }
  /// Al arrancar: con red valida (y renueva) la sesión; sin red sigue con el
  /// usuario guardado para capturar offline. Sólo cierra sesión si Keycloak la
  /// rechaza.
  Future<void> _checkAuthStatus(CheckAuthStatus event, Emitter<AuthState> emit) async {
    final token = await AuthSession.storedToken();
    if (token == null) {
      return add(const Logout(''));
    }
    final cached = await AuthSession.loadUser();

    try {
      final fresh = await AuthSession.validToken();
      final user = await authRepository.checkAuthStatus(fresh);
      await AuthSession.saveUser(user);
      emit(state.copyWith(
        authStatus: AuthStatus.authenticated,
        errorMessage: '',
        user: user,
      ));
    } on OfflineException {
      _offlineOrLogout(emit, cached);
    } on SessionExpiredException catch (e) {
      add(Logout(e.message));
    } on CustomError catch (e) {
      add(Logout(e.message));
    } catch (_) {
      // el servidor falló, no la sesión: se sigue con la guardada
      _offlineOrLogout(emit, cached);
    }
  }

  void _offlineOrLogout(Emitter<AuthState> emit, User? cached) {
    if (cached == null) {
      add(const Logout('Sin conexión: inicia sesión cuando tengas internet'));
      return;
    }
    emit(state.copyWith(
      authStatus: AuthStatus.authenticated,
      errorMessage: '',
      user: cached,
    ));
  }
  void _login(Login event,Emitter<AuthState> emit) async{
    try {
      emit(state.copyWith(
        authStatus: AuthStatus.checking,
        loading: true
      ));
      String username = event.username;
      String password = event.password;
      final user = await authRepository.login(username, password);
      //_setLoggedUser(emit, user );
      await keyValueStorageServiceImpl.setKeyValue('token', user.token);
      await AuthSession.saveUser(user);
      emit(state.copyWith(
        user: user,
        authStatus: AuthStatus.authenticated,
        loading: false,
        errorMessage: ''
      ));

    } on CustomError catch (e) {
      //_logout( emit, e.message );
      add(Logout( e.message));
    } catch (e){
      //_logout(emit, 'Error no controlado' );
      //_logout( emit, 'Error no controlado' );
      add(const Logout('Error no controlado'));

    }

    // emit(state.copyWith(
    //   formStatus: LoginFormStatus.validating,
    // ));
    // print('Submit: $state');
    // //authRepository.login(state.email.value, state.password.value);
    // final LoginResult responseLogin = await authRepository.login('anaeufraciacastroguzman408@gmail.com','anaeufrac332');
    // if(responseLogin.user ==null){
    //   emit(state.copyWith(
    //     formStatus: LoginFormStatus.onError,
    //     error: responseLogin.error
    //   ));
    // }else{
    //   emit(state.copyWith(
    //     formStatus: LoginFormStatus.valid,
    //     error: ''
    //   ));
    // }
  }
  Future<void> _logout(Logout event, Emitter<AuthState> emit) async {
    await AuthSession.clear();
    // los puestos y polígonos son de esa cuenta: quien entre después baja los
    // suyos. Los convencidos pendientes se quedan en el teléfono.
    await CaptureCatalog().clear();
    // estado nuevo, no copyWith: copyWith(user: null) conservaba al usuario
    emit(AuthState(
      authStatus: AuthStatus.notAuthenticated,
      errorMessage: event.error,
    ));
    
    
  }
}
