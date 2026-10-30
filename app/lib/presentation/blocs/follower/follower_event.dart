part of 'follower_bloc.dart';

sealed class FollowerEvent extends Equatable {
  const FollowerEvent();

  @override
  List<Object> get props => [];
}

class LoadFollowers extends FollowerEvent {
  const LoadFollowers();
}

class SaveFollower extends FollowerEvent {
  final Follower follower;
  const SaveFollower({required this.follower});
}

class DeleteFollower extends FollowerEvent {
  final int isarId;
  const DeleteFollower({required this.isarId});
}

class StartDateChange extends FollowerEvent {
  final DateTime? startDate;
  const StartDateChange(this.startDate);
}

class EndDateChange extends FollowerEvent {
  final DateTime? endDate;
  const EndDateChange(this.endDate);
}
