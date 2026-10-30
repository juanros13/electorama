part of 'login_bloc.dart';

abstract class LoginEvent  {
  const LoginEvent();
}

class EmailChange extends LoginEvent{
  final String value;
  const EmailChange(this.value);
}

class PasswordChange extends LoginEvent{
  final String value;
  const PasswordChange(this.value);
}
class Submit extends LoginEvent{
  const Submit();
}