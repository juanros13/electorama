part of 'visits_people_bloc.dart';

class VisitsPeopleState extends Equatable {
  final bool loading;
  final String error;
  final bool add;
  final List<VisitPeople> listVisitPeople;

  const VisitsPeopleState({
    this.loading = false, 
    this.error = '', 
    this.add = false, 
    this.listVisitPeople = const []
  });

  VisitsPeopleState copyWith({
    bool? loading,
    String? error,
    bool? add,
    List<VisitPeople>? listVisitPeople
  }) => VisitsPeopleState(
    loading: loading ?? this.loading,
    error: error ?? this.error,
    add: add ?? this.add,
    listVisitPeople: listVisitPeople ?? this.listVisitPeople
  );


  @override
  List<Object> get props => [
    loading,
    error,
    add,
    listVisitPeople
  ];
}

class VisitsPeopleInitial extends VisitsPeopleState {}
