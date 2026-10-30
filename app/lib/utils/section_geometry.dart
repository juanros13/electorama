import 'dart:convert';
import 'dart:math' as math;

/// El margen del GPS: un punto a esta distancia del borde cuenta como dentro
/// (lo mismo que valida msvc-structure con PostGIS).
const double sectionToleranceMeters = 100;

/// Metros que le faltan al punto para estar en la sección: 0 si está dentro.
/// null si el GeoJSON no es un Polygon/MultiPolygon que se pueda leer.
double? distanceOutsideSection(double lat, double lng, String geoJson) {
  final dynamic g;
  try {
    g = jsonDecode(geoJson);
  } catch (_) {
    return null;
  }
  final type = g['type'];
  final List polygons = type == 'Polygon'
      ? [g['coordinates']]
      : type == 'MultiPolygon'
          ? g['coordinates']
          : const [];
  if (polygons.isEmpty) {
    return null;
  }
  for (final rings in polygons) {
    if (rings is List && rings.isNotEmpty && _inRing(lat, lng, rings[0])) {
      final inHole = rings.skip(1).any((hole) => _inRing(lat, lng, hole));
      if (!inHole) {
        return 0;
      }
    }
  }
  // fuera: la distancia al borde más cercano, en una proyección local en metros
  const mPerLat = 111320.0;
  final mPerLng = 111320.0 * math.cos(lat * math.pi / 180);
  List<double> xy(List c) => [
        ((c[0] as num).toDouble() - lng) * mPerLng,
        ((c[1] as num).toDouble() - lat) * mPerLat,
      ];
  var best = double.infinity;
  for (final rings in polygons) {
    for (final ring in rings as List) {
      final pts = ring as List;
      for (var i = 1; i < pts.length; i++) {
        best = math.min(best, _toSegment(xy(pts[i - 1]), xy(pts[i])));
      }
    }
  }
  return best;
}

bool _inRing(double lat, double lng, List ring) {
  var inside = false;
  for (var i = 0, j = ring.length - 1; i < ring.length; j = i++) {
    final xi = (ring[i][0] as num).toDouble(), yi = (ring[i][1] as num).toDouble();
    final xj = (ring[j][0] as num).toDouble(), yj = (ring[j][1] as num).toDouble();
    if ((yi > lat) != (yj > lat) && lng < (xj - xi) * (lat - yi) / (yj - yi) + xi) {
      inside = !inside;
    }
  }
  return inside;
}

/// Distancia del origen (el punto) al segmento a–b.
double _toSegment(List<double> a, List<double> b) {
  final dx = b[0] - a[0], dy = b[1] - a[1];
  final len2 = dx * dx + dy * dy;
  final t = len2 == 0 ? 0.0 : (-(a[0] * dx + a[1] * dy) / len2).clamp(0.0, 1.0);
  final px = a[0] + t * dx, py = a[1] + t * dy;
  return math.sqrt(px * px + py * py);
}
