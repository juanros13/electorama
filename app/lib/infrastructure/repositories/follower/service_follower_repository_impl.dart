import 'package:electorama_app/domain/datasources/follower/service_follower_datasource.dart';
import 'package:electorama_app/domain/repositories/follower/service_follower_repository.dart';

class ServiceFollowerRepositoryImpl extends ServiceFollowerRepository {
  final ServiceFollowerDatasource datasource;

  ServiceFollowerRepositoryImpl(this.datasource);

  @override
  Future<bool> postFollower(Map<String, dynamic> followerFormData, int isarId) =>
      datasource.postFollower(followerFormData, isarId);
}
