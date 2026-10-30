import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/infrastructure/datasources/follower/local_storage_follower_datasource_impl.dart';
import 'package:electorama_app/infrastructure/datasources/structure/structure_capture_datasource.dart';
import 'package:electorama_app/infrastructure/services/auth_session.dart';
import 'package:electorama_app/infrastructure/services/capture_catalog.dart';

part 'follower_service_event.dart';
part 'follower_service_state.dart';

/// Manda a msvc-structure los convencidos capturados sin red.
///
/// Cada uno termina en SYNCED, DUPLICATE (ya era convencido), REJECTED (el
/// servidor no lo aceptó: se guarda el motivo) o se queda PENDING si no hay
/// red o el servidor falló. Antes de mandar se refrescan los puestos y los
/// polígonos de sección para las capturas siguientes.
class FollowerServiceBloc extends Bloc<FollowerServiceEvent, FollowerServiceState> {
  final LocalStorageFollowerDatasourceImpl _local = LocalStorageFollowerDatasourceImpl();
  final StructureCaptureDatasource _remote = StructureCaptureDatasource();
  final CaptureCatalog _catalog = CaptureCatalog();

  FollowerServiceBloc() : super(const FollowerServiceState()) {
    on<SentFollowerService>(_send);
    on<ChangeListFollowers>(_changeListFollowers);
    on<ChangeSubmitFollowerSync>(_changeSubmitSync);
  }

  Future<void> _send(SentFollowerService event, Emitter<FollowerServiceState> emit) async {
    emit(state.copyWith(loading: true, error: ''));
    var synced = 0, duplicates = 0, rejected = 0, left = 0;
    String error = '';
    try {
      var token = await AuthSession.validToken();
      // los puestos y polígonos al día para lo que se capture después
      try {
        await _catalog.refresh();
      } catch (_) {/* no impide mandar */}

      final queue = state.listFollowers.where((f) => f.needsAttention && f.id != null).toList();
      for (var i = 0; i < queue.length; i++) {
        final f = queue[i];
        CaptureResult result;
        try {
          result = await _remote.capture(token, f);
        } on SessionExpiredException {
          // el access token venció a la mitad: se renueva una vez
          token = await AuthSession.validToken(forceRefresh: true);
          result = await _remote.capture(token, f);
        }
        switch (result) {
          case CaptureSaved(:final membershipId, :final positionLabel):
            synced++;
            await _local.markSyncResult(f.id!,
                status: FollowerSyncStatus.synced,
                membershipId: membershipId,
                positionLabel: positionLabel);
          case CaptureDuplicate(:final where):
            duplicates++;
            await _local.markSyncResult(f.id!,
                status: FollowerSyncStatus.duplicate, error: 'Ya era convencido: $where');
          case CaptureRejected(:final message):
            rejected++;
            await _local.markSyncResult(f.id!,
                status: FollowerSyncStatus.rejected, error: message);
          case CaptureRetryLater(:final message):
            // sin red o el servidor falló: lo que falta se manda después
            left = queue.length - i;
            error = message == 'Sin conexión'
                ? 'Se perdió la conexión: $left quedan pendientes'
                : 'El servidor falló ($message): $left quedan pendientes';
        }
        if (left > 0) {
          break;
        }
      }
    } on OfflineException {
      error = 'Sin conexión: se mandan cuando haya señal';
    } on SessionExpiredException catch (e) {
      error = e.message;
    } catch (e) {
      error = 'No se pudo sincronizar: $e';
    }
    emit(state.copyWith(
      loading: false,
      error: error,
      listFollowers: await _local.loadFollowers(),
      submitSync: true,
      followerSync: synced,
      followerDuplicate: duplicates,
      followerError: rejected,
    ));
  }

  void _changeListFollowers(ChangeListFollowers event, Emitter<FollowerServiceState> emit) {
    emit(state.copyWith(loading: false, listFollowers: event.listCanvasWall));
  }

  void _changeSubmitSync(ChangeSubmitFollowerSync event, Emitter<FollowerServiceState> emit) {
    emit(state.copyWith(submitSync: event.value));
  }
}
