import 'package:dio/dio.dart';

import '../../../config/constants/environment.dart';
import '../../../domain/entities/followers/follower.dart';
import '../../services/auth_session.dart';

/// Cómo terminó el envío de un convencido.
sealed class CaptureResult {
  const CaptureResult();
}

class CaptureSaved extends CaptureResult {
  final int membershipId;
  final String positionLabel;
  const CaptureSaved(this.membershipId, this.positionLabel);
}

/// Ya era convencido en la estructura: no se duplica.
class CaptureDuplicate extends CaptureResult {
  final String where;
  const CaptureDuplicate(this.where);
}

/// El servidor no lo acepta tal cual (sección, ubicación, CURP…).
class CaptureRejected extends CaptureResult {
  final String message;
  const CaptureRejected(this.message);
}

/// Hubo que dejarlo para después: sin red o el servidor falló.
class CaptureRetryLater extends CaptureResult {
  final String message;
  const CaptureRetryLater(this.message);
}

/// La captura de convencidos en msvc-structure (POST /memberships), los puestos
/// donde se puede capturar y el polígono de la sección (geography).
class StructureCaptureDatasource {
  final Dio _dio = Dio(BaseOptions(
    connectTimeout: const Duration(seconds: 15),
    receiveTimeout: const Duration(seconds: 20),
  ));

  Options _auth(String token) => Options(headers: {'Authorization': 'Bearer $token'});

  /// [{positionId, level, label, holderName, sectionGeoId, municipalityGeoId, own}]
  Future<List<Map<String, dynamic>>> captureTargets(String token) async {
    final r = await _dio.get('${Environment.urlStructure}/memberships/capture-targets',
        options: _auth(token));
    return List<Map<String, dynamic>>.from(r.data as List);
  }

  /// El GeoJSON de la sección como texto; null si no tiene geometría.
  Future<String?> sectionGeometry(String token, int sectionGeoId) async {
    try {
      final r = await _dio.get(
        '${Environment.urlGeography}/catalog/sections/$sectionGeoId/geometry',
        options: _auth(token).copyWith(responseType: ResponseType.plain),
      );
      return r.data as String;
    } on DioException catch (e) {
      if (e.response?.statusCode == 404) {
        return null;
      }
      rethrow;
    }
  }

  /// Manda el convencido. Lanza [SessionExpiredException] si el token ya no
  /// sirve (el que llama decide renovar y reintentar).
  Future<CaptureResult> capture(String token, Follower f) async {
    try {
      final r = await _dio.post('${Environment.urlStructure}/memberships',
          data: toBody(f), options: _auth(token));
      final data = r.data as Map<String, dynamic>;
      return CaptureSaved(
          (data['membershipId'] as num).toInt(), data['positionLabel']?.toString() ?? '');
    } on DioException catch (e) {
      if (AuthSession.isOffline(e)) {
        return const CaptureRetryLater('Sin conexión');
      }
      final status = e.response?.statusCode ?? 0;
      final body = e.response?.data;
      final message = body is Map ? (body['message']?.toString() ?? '') : '';
      final detail = body is Map ? body['detail'] : null;
      if (status == 401) {
        throw const SessionExpiredException();
      }
      if (status == 409 && detail is Map && detail['code'] == 'ALREADY_SUPPORTER') {
        return CaptureDuplicate(detail['where']?.toString() ?? message);
      }
      if (status == 400 || status == 403 || status == 404 || status == 409) {
        return CaptureRejected(message.isEmpty ? 'El servidor no lo aceptó ($status)' : message);
      }
      return CaptureRetryLater(message.isEmpty ? 'Error del servidor ($status)' : message);
    }
  }

  /// El cuerpo de POST /memberships desde lo capturado en el teléfono.
  static Map<String, dynamic> toBody(Follower f) {
    final supports = f.supportsMovement == 'SI';
    final inviting = f.invitingOthersToParticipate == 'SI';
    return {
      'positionId': f.positionId,
      'supportsMovement': supports,
      'sex': f.catSex?.id == 2 ? 'MUJER' : (f.catSex?.id == 1 ? 'HOMBRE' : null),
      'sectionGeoId': int.tryParse(f.sectionNumber),
      'street': f.street,
      'externalNumber': f.externalNumber,
      'internalNumber': f.internalNumber,
      'neighborhood': f.neighborhood,
      'zipCode': f.zipCode,
      'latitude': double.tryParse(f.latitude),
      'longitude': double.tryParse(f.longitude),
      if (supports) ...{
        'names': f.names,
        'lastName': f.lastName,
        'surName': f.surName,
        'curp': f.curp,
        'ine': f.ine,
        'ineValidity': int.tryParse(f.validity ?? ''),
        'birthdate': isoDate(f.birthdate),
        'cellPhoneNumber': f.cellPhoneNumber,
        'supportWhatsapp': f.supportsWhatsapp == 'SI',
        'cellPhoneNumberOther': f.cellPhoneNumberOther,
        'email': f.email,
        'committeeParticipation': f.committeeParticipation == 'SI',
        'pollingRepParticipation': f.pollinStationRepresentativeParticipation == 'SI',
        'invitingOthers': inviting,
        'numberOfPeopleInvited': inviting ? f.numberOfPeopleInvited : null,
      },
    };
  }

  /// 'dd-MM-yyyy' (como lo guarda la app) a 'yyyy-MM-dd'; null si no se puede.
  static String? isoDate(String? ddMMyyyy) {
    if (ddMMyyyy == null) {
      return null;
    }
    final m = RegExp(r'^(\d{2})-(\d{2})-(\d{4})$').firstMatch(ddMMyyyy.trim());
    return m == null ? null : '${m[3]}-${m[2]}-${m[1]}';
  }
}
