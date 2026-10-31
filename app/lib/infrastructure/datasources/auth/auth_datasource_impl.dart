import 'dart:io';

import 'package:dio/dio.dart';
import 'package:electorama_app/infrastructure/models/auth/keycloak_response.dart';

import '../../../config/constants/environment.dart';
import '../../../domain/datasources/auth/auth_datasource.dart';
import '../../../domain/entities/auth/user.dart';
import '../../errors/auth_errors.dart';
import '../../services/auth_session.dart';
import '../../mappers/auth/user_mapper.dart';


class AuthDataSourceImpl extends AuthDataSource {

  final dio = Dio(
    BaseOptions(
      baseUrl: Environment.apiUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10)
    )
  );

  @override
  Future<User> checkAuthStatus(String token) async{
    try {
      await dio.post(
        '/protocol/openid-connect/userinfo', 
        options: Options(
          contentType: 'application/x-www-form-urlencoded',
          headers: {
            'Authorization': 'Bearer $token'
          }
        ),
      );
      
      final user = UserMapper.userJsonToEntity(token);
      final dioUser = Dio(
        BaseOptions(
          baseUrl: Environment.urlStructure,
          headers: {
            'Authorization': 'Bearer ${user.token}'
          }
        )
      );
      final responseUser = await dioUser.get(
        '/positions/holders/${user.authId}/superior', 
        options: Options(
          contentType: 'application/x-www-form-urlencoded',
          headers: {
            'Authorization': 'Bearer ${user.token}'
          }
        ),
      );
      final userResponse = user.copyWith(authIdReference: responseUser.data['superiorAuthId'] ?? '');
      return userResponse;

    } on DioException catch (e) {
      // sin red la sesión guardada sigue sirviendo para capturar
      if (AuthSession.isOffline(e)) {
        throw const OfflineException('El dispositivo no cuenta con internet');
      }
      if( e.response?.statusCode == 401 ){
        throw CustomError('Token incorrecto' );
      }
      throw Exception();
    }
  }

  @override
  Future<User> login(String email, String password) async {
    try {
      final formData = {
        'client_id': Environment.clientId,
        'grant_type' :'password',
        'scope' : 'openid',
        'username': email,
        'password': password,
        'client_secret' :Environment.clientSecret
      };
      final response = await dio.post(
        '/protocol/openid-connect/token', 
        data: formData,
        options: Options(
          contentType: 'application/x-www-form-urlencoded',
        ),
        
      );

      KeycloakResponse keycloakResponse = KeycloakResponse.fromJson(response.data);
      // el refresh token permite renovar la sesión sin pedir otra vez la contraseña
      await AuthSession.saveTokens(
          keycloakResponse.accessToken, keycloakResponse.refreshToken);
      final user = UserMapper.userJsonToEntity(keycloakResponse.accessToken);
      final dioUser = Dio(
        BaseOptions(
          baseUrl: Environment.urlStructure,
          headers: {
            'Authorization': 'Bearer ${user.token}'
          }
        )
      );
      final responseUser = await dioUser.get(
        '/positions/holders/${user.authId}/superior', 
        options: Options(
          contentType: 'application/x-www-form-urlencoded',
          headers: {
            'Authorization': 'Bearer ${user.token}'
          }
        ),
      );
      final userResponse = user.copyWith(authIdReference: responseUser.data['superiorAuthId'] ?? '');
      return userResponse;
      //return LoginResult(user: user, error: '' );
      
    } on DioException catch (e) {
      if( e.response?.statusCode == 401 ){
        //throw CustomError(e.response?.data['message'] ?? 'Credenciales incorrectas' );
        throw CustomError('Credenciales incorrectas' );
        //return LoginResult(user: null, error: 'Credenciales incorrectas' );
      }
      if (e.error is SocketException) {
        throw CustomError(e.response?.data['message'] ?? 'El dispositivo no cuenta con internet' );
      }
      if ( e.type == DioExceptionType.connectionTimeout ){
        throw CustomError('Revisar conexión a internet');
      }
      throw Exception();
    } catch (e) {
      throw Exception();
    }


  }
  
}
