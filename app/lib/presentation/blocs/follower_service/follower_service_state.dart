part of 'follower_service_bloc.dart';

class FollowerServiceState extends Equatable {
  final bool loading;
  final String error;
  final bool submitSync;
  final List<Follower> listFollowers;
  final int followerSync;
  /// Rechazados por el servidor (se guardó el motivo).
  final int followerError;
  /// Ya eran convencidos: no se duplicaron.
  final int followerDuplicate;

  const FollowerServiceState({
    this.loading = false,
    this.error = '',
    this.submitSync = false,
    this.listFollowers = const [],
    this.followerSync = 0,
    this.followerError = 0,
    this.followerDuplicate = 0,
  });

  FollowerServiceState copyWith({
    bool? loading,
    String? error,
    bool? submitSync,
    List<Follower>? listFollowers,
    int? followerSync,
    int? followerError,
    int? followerDuplicate,
  }) =>
      FollowerServiceState(
        loading: loading ?? this.loading,
        error: error ?? this.error,
        submitSync: submitSync ?? this.submitSync,
        listFollowers: listFollowers ?? this.listFollowers,
        followerSync: followerSync ?? this.followerSync,
        followerError: followerError ?? this.followerError,
        followerDuplicate: followerDuplicate ?? this.followerDuplicate,
      );

  @override
  List<Object> get props => [
        loading,
        error,
        submitSync,
        listFollowers,
        followerSync,
        followerError,
        followerDuplicate,
      ];
}

class FollowerServiceInitial extends FollowerServiceState {}
