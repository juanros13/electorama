import 'package:electorama_app/domain/entities/geo/section.dart';

class SectionMapper {
  /// Una fila de `GET /positions/scope/{authId}` (msvc-structure): la sección y desde qué
  /// puesto la cubre la cuenta. El código del territorio es "S-0815".
  static Section scopeToEntity(Map<String, dynamic> scope, String authId) => Section(
      id: scope['territoryId'],
      userAuthId: authId,
      seccionNumber: (scope['territoryCode'] as String? ?? '').replaceFirst('S-', ''),
      seccionGeoId: scope['sectionGeoId'],
      meta: scope['goal'] ?? 0,
      municipalityId: scope['municipalityGeoId'] ?? 0);
}
