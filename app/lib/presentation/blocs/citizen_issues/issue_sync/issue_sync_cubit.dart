import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';
import 'package:electorama_app/infrastructure/datasources/citizen_issues/local_storage_issue_datasource_impl.dart';
import 'package:electorama_app/infrastructure/datasources/citizen_issues/service_issue_datasource_impl.dart';

/// Sincroniza las incidencias pendientes (POST /issue + archivos). Requiere
/// token; se crea local en la vista cuando hay sesión.
class IssueSyncCubit extends Cubit<IssueSyncState> {
  final String accessToken;
  final IssueLocalDatasource _local = IssueLocalDatasource();

  IssueSyncCubit({required this.accessToken})
      : super(const IssueSyncState());

  Future<void> syncPending(List<Issue> pending) async {
    if (pending.isEmpty) return;
    emit(const IssueSyncState(loading: true));
    final service = ServiceIssueDatasource(accessToken: accessToken);
    int ok = 0, err = 0;
    for (final issue in pending) {
      try {
        await service.sendIssue(issue);
        await _local.markSynced(issue.uuid);
        ok++;
      } catch (_) {
        err++;
      }
    }
    emit(IssueSyncState(done: true, synced: ok, failed: err));
  }

  void clearDone() => emit(const IssueSyncState());
}

class IssueSyncState extends Equatable {
  final bool loading;
  final bool done;
  final int synced;
  final int failed;
  const IssueSyncState({
    this.loading = false,
    this.done = false,
    this.synced = 0,
    this.failed = 0,
  });

  @override
  List<Object?> get props => [loading, done, synced, failed];
}
