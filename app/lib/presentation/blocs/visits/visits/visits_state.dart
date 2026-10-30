part of 'visits_bloc.dart';

class VisitsState extends Equatable {
  
  final bool loading;
  final String error;
  final bool submitSync;
  final int isarId;
  // final String sectionSelected;
  // final String municipalitySelected;
  // final String localitySelected;
  final List<VisitPeople> listVisitPeople;
  final VisitedModel? visited;
  
  
  const VisitsState({
    this.loading = false, 
    this.error = '', 
    this.submitSync = false, 
    this.isarId = 0,
    this.visited,
    this.listVisitPeople = const []
  });

  VisitsState copyWith({
    bool? loading,
    String? error,
    bool? submitSync,
    VisitedModel? visited,
    List<VisitPeople>? listVisitPeople
  }) => VisitsState(
    loading: loading ?? this.loading,
    error: error ?? this.error,
    submitSync: submitSync ?? this.submitSync,
    isarId: isarId,
    visited: visited ?? this.visited,
    listVisitPeople: listVisitPeople ?? this.listVisitPeople
  );


  @override
  List<Object?> get props => [
    loading,
    error,
    submitSync,
    isarId,
    visited,
    listVisitPeople
  ];
}