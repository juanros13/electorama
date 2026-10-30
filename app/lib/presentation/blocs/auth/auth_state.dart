part of 'auth_bloc.dart';

enum AuthStatus { checking, authenticated, notAuthenticated }


class AuthState extends Equatable {
  final AuthStatus authStatus;
  final User? user;
  final String errorMessage;
  final bool loading;

  
  const AuthState({
    this.authStatus = AuthStatus.checking, 
    this.user, 
    this.errorMessage = '',
    this.loading = false
  });

  AuthState copyWith({
    AuthStatus? authStatus,
    User? user,
    String? errorMessage,
    bool? loading
  }) => AuthState(
    authStatus: authStatus ?? this.authStatus,
    user: user ?? this.user,
    errorMessage: errorMessage ?? this.errorMessage,
    loading: loading ?? this.loading
  );
  
  @override
  List<Object?> get props => [authStatus, user, errorMessage,loading];
}

class AuthInitial extends AuthState {}
