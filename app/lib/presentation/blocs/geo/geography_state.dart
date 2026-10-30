part of 'geography_bloc.dart';

class GeographyState extends Equatable {
  final bool loading;
  final String error;
  final bool add;
  final List<Section> listSections;
  final Municipality? municipality;
  final List<Locality> listLocalities;

  const GeographyState({
    this.loading = false, 
    this.error = '', 
    this.add = false, 
    this.listSections = const [],
    this.municipality,
    this.listLocalities = const []
  });

  GeographyState copyWith({
    bool? loading,
    String? error,
    bool? add,
    List<Section>? listSections,
    Municipality? municipality,
    List<Locality>? listLocalities
  }) => GeographyState(
    loading: loading ?? this.loading,
    error: error ?? this.error,
    add: add ?? this.add,
    listSections: listSections ?? this.listSections,
    municipality: municipality ?? this.municipality,
    listLocalities: listLocalities ?? this.listLocalities
  );


  @override
  List<Object?> get props => [
    loading,
    error,
    add,
    listSections,
    municipality,
    listLocalities
  ];
}

class SectionsInitial extends GeographyState {}
