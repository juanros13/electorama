part of 'local_storage_bloc.dart';

class LocalStorageState extends Equatable {
  final String name;

  const LocalStorageState({
    this.name = ''
  });


  @override
  List<Object> get props => [name];
}