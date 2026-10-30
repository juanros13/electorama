part of 'follower_bloc.dart';

class FollowerState extends Equatable {
  final bool loading;
  final String error;
  final bool add;
  final List<Follower> listFollower;
  final List<Follower> listFollowerSelected;
  final int followerPending;
  final int followerSelected;
  final DateTime? startDate;
  final DateTime? endDate;
  final int followerSync;

  const FollowerState({
    this.loading = false,
    this.error = '',
    this.add = false,
    this.listFollower = const [],
    this.listFollowerSelected = const [],
    this.followerPending = 0,
    this.followerSelected = 0,
    this.followerSync = 0,
    this.startDate,
    this.endDate,
  });

  FollowerState copyWith({
    bool? loading,
    String? error,
    bool? add,
    List<Follower>? listFollower,
    List<Follower>? listFollowerSelected,
    int? followerPending,
    int? followerSelected,
    int? followerSync,
    DateTime? startDate,
    DateTime? endDate,
  }) =>
      FollowerState(
        loading: loading ?? this.loading,
        error: error ?? this.error,
        add: add ?? this.add,
        listFollower: listFollower ?? this.listFollower,
        listFollowerSelected: listFollowerSelected ?? this.listFollowerSelected,
        followerPending: followerPending ?? this.followerPending,
        followerSelected: followerSelected ?? this.followerSelected,
        followerSync: followerSync ?? this.followerSync,
        startDate: startDate ?? this.startDate,
        endDate: endDate ?? this.endDate,
      );

  @override
  List<Object?> get props => [
        loading,
        error,
        add,
        listFollower,
        listFollowerSelected,
        followerPending,
        followerSync,
        startDate,
        endDate,
      ];
}

final class FollowerInitial extends FollowerState {}
