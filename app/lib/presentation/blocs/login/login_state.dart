part of 'login_bloc.dart';

enum LoginFormStatus {pure, validating, valid, onError}


class LoginState extends Equatable {
  final bool isValid;
  final Email email;
  final Password password;
  final bool isPure;

  const LoginState( {
    this.email = const Email.pure(), 
    this.password =  const Password.pure(),
    this.isValid = false,
    this.isPure = true,
  });



  
  LoginState copyWith({
    Email? email,
    Password? password,
    bool? isValid,
    bool? isPure,
    String? error
  }) =>LoginState(
    email: email ?? this.email,
    password: password ?? this.password,
    isValid: isValid ?? this.isValid,
    isPure: isPure ?? this.isPure,
  );
  
  @override
  List<Object> get props => [email, password, isValid,isPure];
}

class LoginInitial extends LoginState {}
