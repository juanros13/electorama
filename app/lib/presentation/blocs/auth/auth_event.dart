part of 'auth_bloc.dart';

class AuthEvent  {
  const AuthEvent();
}

class Login extends AuthEvent{
  final String username;
  final String password;
  const Login(this.username, this.password);
}


class Logout extends AuthEvent{
  final String error;
  const Logout(this.error);
}

class CheckAuthStatus extends AuthEvent{
  const CheckAuthStatus();
}