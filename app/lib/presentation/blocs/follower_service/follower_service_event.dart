part of 'follower_service_bloc.dart';

abstract class FollowerServiceEvent {
  const FollowerServiceEvent();
}

/// Manda los pendientes; el token sale de la sesión guardada.
class SentFollowerService extends FollowerServiceEvent {
  const SentFollowerService();
}

class ChangeListFollowers extends FollowerServiceEvent {
  final List<Follower> listCanvasWall;
  ChangeListFollowers({required this.listCanvasWall});
}

class ChangeSubmitFollowerSync extends FollowerServiceEvent {
  final bool value;
  ChangeSubmitFollowerSync({required this.value});
}
