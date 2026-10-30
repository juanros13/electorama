import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class MapsView extends StatelessWidget {
  final LatLng initialLocation;
  const MapsView({super.key, required this.initialLocation});

  @override
  Widget build(BuildContext context) {
    final CameraPosition inicialCameraPosition =
        CameraPosition(target: initialLocation, zoom: 15);

    final size = MediaQuery.of(context).size;
    return SizedBox(
        width: size.width,
        height: size.height,
        child: GoogleMap(
          initialCameraPosition: inicialCameraPosition,
          compassEnabled: false,
          myLocationButtonEnabled: true,
          myLocationEnabled: true,
          zoomControlsEnabled: true,

        ));
  }
}
