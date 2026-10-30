import 'package:electorama_app/domain/entities/followers/follower.dart';

abstract class LocalStorageFollowersRepository {
  Future<List<Follower>> loadFollowers();
  Future<List<Follower>> loadFollowersSelected(
      DateTime startDate, DateTime endDate);
  Future<Follower> saveFollower(Follower follower);
  Future<bool> deleteFollower(int isarId);
  Future<Follower> getFollowerById(int isarId);
  Future<Follower> syncFollowers(int isarId);
}
