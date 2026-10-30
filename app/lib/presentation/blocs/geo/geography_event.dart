part of 'geography_bloc.dart';

abstract class GeographyEvent {
  const GeographyEvent();
}


class InitGeo extends GeographyEvent{}

class LoadSections extends GeographyEvent{
  final String seccion;
  const LoadSections({required this.seccion});
}

class LoadMunicipality extends GeographyEvent{
  final int seccionId;
  const LoadMunicipality({required this.seccionId});
}


class LoadLocalities extends GeographyEvent{
  final int seccionId;
  const LoadLocalities({required this.seccionId});
}