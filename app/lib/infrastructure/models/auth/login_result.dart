import 'package:electorama_app/domain/entities/auth/user.dart';

class LoginResult {
  final User? user;
  final String? error;

  LoginResult({this.user, this.error});
}