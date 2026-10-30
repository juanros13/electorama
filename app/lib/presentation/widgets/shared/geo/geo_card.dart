import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall_form/canvas_wall_form_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
// import 'package:electorama_app/presentation/blocs/visits/visits_people_form/visits_people_form_bloc.dart';

class CardGeo extends StatefulWidget {
  /// Callbacks opcionales para reportar lat/long. Si se pasan, se usan en vez
  /// de despachar a CanvasWallFormBloc (permite reusar la card en followers).
  final void Function(String lat)? onLat;
  final void Function(String lng)? onLong;

  const CardGeo({
    super.key,
    this.onLat,
    this.onLong,
  });

  @override
  State<CardGeo> createState() => _CardGeoState();
}

class _CardGeoState extends State<CardGeo> {
  late LocationBloc locationBloc;

  @override
  void initState() {
    super.initState();
    locationBloc = BlocProvider.of<LocationBloc>(context);
    //locationBloc.getCurrentPosition();
    locationBloc.startFollowingUser();
  }

  @override
  void dispose() {
    //locationBloc = BlocProvider.of<LocationBloc>(context);
    locationBloc.stopFollowingUser();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final locationleBloc = context.watch<LocationBloc>();

    String longitude = '';
    String latitude = '';
    if (locationleBloc.state.lastKnownLocation != null) {
      longitude = locationleBloc.state.lastKnownLocation!.longitude.toString();
      latitude = locationleBloc.state.lastKnownLocation!.latitude.toString();
      if (widget.onLat != null || widget.onLong != null) {
        widget.onLat?.call(latitude);
        widget.onLong?.call(longitude);
      } else {
        final canvasWallFormBloc = context.read<CanvasWallFormBloc>();
        canvasWallFormBloc.add(LatitudeChange(latitude));
        canvasWallFormBloc.add(LongitudeChange(longitude));
      }
    }
    final onColor = colors.onPrimaryContainer;
    return Card(
      color: colors.primaryContainer,
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.xl),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Geolocalización',
                    style: textTheme.titleMedium?.copyWith(color: onColor)),
                Icon(Icons.radar, color: onColor),
              ],
            ),
            const SizedBox(height: AppSpacing.md),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.location_on_outlined, size: 18, color: onColor),
                const SizedBox(width: AppSpacing.xs),
                Text(
                  latitude.isNotEmpty
                      ? (latitude.length > 10 ? latitude.substring(0, 10) : latitude)
                      : '--',
                  style: textTheme.bodyLarge?.copyWith(color: onColor),
                ),
                const SizedBox(width: AppSpacing.xl),
                Icon(Icons.location_on_outlined, size: 18, color: onColor),
                const SizedBox(width: AppSpacing.xs),
                Text(
                  longitude.isNotEmpty
                      ? (longitude.length > 10 ? longitude.substring(0, 10) : longitude)
                      : '--',
                  style: textTheme.bodyLarge?.copyWith(color: onColor),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}