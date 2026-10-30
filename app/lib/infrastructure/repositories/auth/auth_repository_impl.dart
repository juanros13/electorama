import '../../../domain/datasources/auth/auth_datasource.dart';
import '../../../domain/entities/auth/user.dart';
import '../../../domain/repositories/auth/auth_repository.dart';
import '../../datasources/auth/auth_datasource_impl.dart';


class AuthRepositoryImpl extends AuthRepository {

  final AuthDataSource dataSource;

  AuthRepositoryImpl({
    AuthDataSource? dataSource
  }) : dataSource = dataSource ?? AuthDataSourceImpl();

  @override
  Future<User> checkAuthStatus(String token) {
    return dataSource.checkAuthStatus(token);
  }

  @override
  Future<User> login(String email, String password) {
    return dataSource.login(email, password);
  }

}