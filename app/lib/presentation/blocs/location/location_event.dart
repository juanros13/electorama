part of 'location_bloc.dart';

abstract class LocationEvent extends Equatable {
  const LocationEvent();

  @override
  List<Object> get props => [];
}

class OnNewLocationEvent extends LocationEvent {
  final LatLng newLocation;
  const OnNewLocationEvent(this.newLocation);
}

class onStartFollowingUser extends LocationEvent {}
class onStopFollowingUser extends LocationEvent {}

