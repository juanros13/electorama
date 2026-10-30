import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/domain/repositories/follower/local_storage_followers_repository.dart';
import 'package:electorama_app/infrastructure/datasources/follower/local_storage_follower_datasource_impl.dart';
import 'package:electorama_app/infrastructure/repositories/follower/local_storage_follower_repository_impl.dart';

part 'follower_event.dart';
part 'follower_state.dart';

class FollowerBloc extends Bloc<FollowerEvent, FollowerState> {
  LocalStorageFollowersRepository localStorageRepository =
      LocalStorageFollowersRepositoryImpl(LocalStorageFollowerDatasourceImpl());

  FollowerBloc() : super(FollowerInitial()) {
    on<LoadFollowers>(_loadFollowers);
    on<SaveFollower>(_saveFollower);
    on<DeleteFollower>(_deleteFollower);
    on<StartDateChange>(_onStartDateChange);
    on<EndDateChange>(_onEndDateChange);
  }

  void _loadFollowers(LoadFollowers event, Emitter<FollowerState> emit) async {
    emit(state.copyWith(loading: true));

    final listResp = await localStorageRepository.loadFollowers();

    int followersPending = 0;
    int followersSync = 0;

    for (int i = 0; i < listResp.length; i++) {
      if (listResp[i].isInSync) {
        followersSync++;
      } else {
        followersPending++;
      }
    }

    List<Follower> listRespSelected = [];
    var followerSelected = 0;
    if (state.startDate == null && state.endDate == null) {
      listRespSelected = listResp;
      followerSelected = followersPending;
    } else {
      listRespSelected = await localStorageRepository.loadFollowersSelected(
          state.startDate!, state.endDate!);
      followerSelected = listRespSelected.length;
    }

    emit(state.copyWith(
      listFollower: listResp,
      followerPending: followersPending,
      followerSync: followersSync,
      loading: false,
      listFollowerSelected: listRespSelected,
      followerSelected: followerSelected,
    ));
  }

  void _saveFollower(SaveFollower event, Emitter<FollowerState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      await localStorageRepository.saveFollower(event.follower);
      emit(state.copyWith(loading: false, add: true));
      add(const LoadFollowers());
    } catch (error) {
      emit(state.copyWith(loading: false, error: 'Ocurrio un error'));
    }
  }

  void _onStartDateChange(StartDateChange event, Emitter<FollowerState> emit) {
    emit(state.copyWith(startDate: event.startDate));
  }

  void _onEndDateChange(EndDateChange event, Emitter<FollowerState> emit) {
    emit(state.copyWith(endDate: event.endDate));
  }

  void _deleteFollower(DeleteFollower event, Emitter<FollowerState> emit) async {
    try {
      emit(state.copyWith(loading: true));
      await localStorageRepository.deleteFollower(event.isarId);
      emit(state.copyWith(loading: false));
      add(const LoadFollowers());
    } catch (error) {
      emit(state.copyWith(loading: false, error: 'Ocurrio un error'));
    }
  }
}
