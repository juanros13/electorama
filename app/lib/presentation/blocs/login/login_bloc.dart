import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:formz/formz.dart';
import 'package:electorama_app/infrastructure/inputs/email.dart';
import 'package:electorama_app/infrastructure/inputs/password.dart';

part 'login_event.dart';
part 'login_state.dart';

class LoginBloc extends Bloc<LoginEvent, LoginState> {
  LoginBloc() : super(LoginInitial()) {
      on<EmailChange>(_onEmailChange);
      on<PasswordChange>(_onPasswordChange);
      on<Submit>((_, emit) => _onSubmit(emit),);
  }

  void _onEmailChange(EmailChange event, Emitter<LoginState> emit){
    final email = Email.dirty(event.value);
    emit(state.copyWith(
      email: email,
      isValid: Formz.validate([email, state.password]),
      isPure: false
    ));
  }
  void _onPasswordChange(PasswordChange event, Emitter<LoginState> emit){
    final password = Password.dirty(event.value);
    emit(state.copyWith(
      password: password,
      isValid: Formz.validate([password,  state.email]),
      isPure: false
    ));
    print(state.email);
    print(state.password);
    print(Formz.validate([password,  state.email]));
  }
  void _onSubmit(Emitter<LoginState> emit) async{
    //authBloc.add(Login(state.email.value, state.password.value));
    //authBloc.add(const Login('anaeufraciacastroguzman408@gmail.com', 'anaeufrac332--'));
    print('State $state');
  }
}
