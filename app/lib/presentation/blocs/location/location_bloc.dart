import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:geolocator/geolocator.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart' show LatLng;

part 'location_event.dart';
part 'location_state.dart';

class LocationBloc extends Bloc<LocationEvent, LocationState> {
  StreamSubscription<Position>? positionStream;

  LocationBloc() : super(const LocationState()) {
    on<onStartFollowingUser>(
        (event, emit) => emit(state.copyWith(followingUser: true)));

    on<onStopFollowingUser>(
        (event, emit) => emit(state.copyWith(followingUser: false)));

    on<OnNewLocationEvent>((event, emit) {
      emit(state.copyWith(
        lastKnownLocation: event.newLocation,
        locationHistory: [...state.locationHistory, event.newLocation],
      ));
    });
  }

  Future getCurrentPosition() async {
    //final position = await Geolocator.getCurrentPosition();
  }

  void startFollowingUser() {
    add(onStartFollowingUser());
    positionStream = Geolocator.getPositionStream().listen((event) {
      final position = event;
      add(OnNewLocationEvent(LatLng(position.latitude, position.longitude)));
    });
  }

  void stopFollowingUser() {
    positionStream?.cancel();
    add(onStopFollowingUser());
  }

  @override
  Future<void> close() {
    positionStream?.cancel();
    return super.close();
  }
}
