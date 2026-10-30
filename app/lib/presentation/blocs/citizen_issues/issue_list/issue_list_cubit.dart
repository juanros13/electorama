import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';
import 'package:electorama_app/infrastructure/datasources/citizen_issues/local_storage_issue_datasource_impl.dart';

/// Lista de incidencias guardadas localmente (pendientes / vinculadas).
class IssueListCubit extends Cubit<IssueListState> {
  final IssueLocalDatasource _ds = IssueLocalDatasource();
  IssueListCubit() : super(const IssueListState());

  Future<void> load() async {
    emit(state.copyWith(loading: true));
    final issues = await _ds.loadIssues();
    emit(IssueListState(issues: issues));
  }

  Future<void> save(Issue issue) async {
    await _ds.saveIssue(issue);
    await load();
  }

  Future<void> delete(String uuid) async {
    await _ds.deleteIssue(uuid);
    await load();
  }
}

class IssueListState extends Equatable {
  final bool loading;
  final List<Issue> issues;
  const IssueListState({this.loading = false, this.issues = const []});

  int get pending => issues.where((i) => !i.isInSync).length;
  int get synced => issues.where((i) => i.isInSync).length;

  IssueListState copyWith({bool? loading, List<Issue>? issues}) =>
      IssueListState(
        loading: loading ?? this.loading,
        issues: issues ?? this.issues,
      );

  @override
  List<Object?> get props => [loading, issues];
}
