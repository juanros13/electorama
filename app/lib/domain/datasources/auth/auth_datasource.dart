import '../../entities/auth/user.dart';

abstract class AuthDataSource {

  Future<User> login( String email, String password );
  Future<User> checkAuthStatus( String token );

}