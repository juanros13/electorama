import 'package:electorama_app/domain/datasources/follower/local_storage_followers_datasource.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/domain/repositories/follower/local_storage_followers_repository.dart';

class LocalStorageFollowersRepositoryImpl
    extends LocalStorageFollowersRepository {
  final LocalStorageFollowersDatasource datasource;

  LocalStorageFollowersRepositoryImpl(this.datasource);

  @override
  Future<bool> deleteFollower(int isarId) => datasource.deleteFollower(isarId);

  @override
  Future<Follower> getFollowerById(int isarId) =>
      datasource.getFollowerById(isarId);

  @override
  Future<List<Follower>> loadFollowers() => datasource.loadFollowers();

  @override
  Future<Follower> saveFollower(Follower follower) =>
      datasource.saveFollower(follower);

  @override
  Future<Follower> syncFollowers(int isarId) =>
      datasource.syncFollowers(isarId);

  @override
  Future<List<Follower>> loadFollowersSelected(
          DateTime startDate, DateTime endDate) =>
      datasource.loadFollowersByRangeOfDates(startDate, endDate);
}
