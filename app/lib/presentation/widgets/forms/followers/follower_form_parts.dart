import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/config/theme/app_radius.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/infrastructure/services/capture_catalog.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/utils/section_geometry.dart';

// ─────────────────────────────────────────────────────────────────────────────
// La "ficha de campo" de Convencidos: piezas del formulario.
// Orden de arriba abajo = orden en que se trabaja en la calle: dónde queda y
// si el GPS ya está, escanear la INE, si simpatiza, y los pasos numerados.
// ─────────────────────────────────────────────────────────────────────────────

/// Encabezado de un paso: número en círculo, título y ayuda, sobre una
/// tarjeta con el contenido. El riel izquierdo une los pasos visualmente.
class FormStep extends StatelessWidget {
  final int number;
  final String title;
  final String? caption;
  final Widget child;
  final bool last;

  const FormStep({
    super.key,
    required this.number,
    required this.title,
    required this.child,
    this.caption,
    this.last = false,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // el riel: número y línea hacia el paso siguiente
          SizedBox(
            width: 32,
            child: Column(
              children: [
                Container(
                  width: 28,
                  height: 28,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    color: colors.primary,
                    shape: BoxShape.circle,
                  ),
                  child: Text('$number',
                      style: text.labelLarge?.copyWith(
                          color: colors.onPrimary, fontWeight: FontWeight.w800)),
                ),
                if (!last)
                  Expanded(
                    child: Container(
                      width: 2,
                      margin: const EdgeInsets.symmetric(vertical: AppSpacing.xs),
                      decoration: BoxDecoration(
                        color: colors.primary.withValues(alpha: 0.18),
                        borderRadius: AppRadius.brSm,
                      ),
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(bottom: last ? 0 : AppSpacing.xl),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.only(top: 3),
                    child: Text(title,
                        style: text.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
                  ),
                  if (caption != null)
                    Text(caption!,
                        style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant)),
                  const SizedBox(height: AppSpacing.md),
                  child,
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Dos campos en la misma línea (número ext./int., colonia/CP…).
class FieldPair extends StatelessWidget {
  final Widget left;
  final Widget right;
  final int leftFlex;
  final int rightFlex;
  const FieldPair(
      {super.key, required this.left, required this.right, this.leftFlex = 1, this.rightFlex = 1});

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(flex: leftFlex, child: left),
        const SizedBox(width: AppSpacing.md),
        Expanded(flex: rightFlex, child: right),
      ],
    );
  }
}

/// La acción principal: escanear la credencial. A todo lo ancho, con lo que
/// llena explicado, para que nadie capture a mano lo que la INE ya trae.
class ScanIneButton extends StatelessWidget {
  final VoidCallback onTap;
  const ScanIneButton({super.key, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    return Material(
      borderRadius: AppRadius.brLg,
      clipBehavior: Clip.antiAlias,
      child: Ink(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [AppColors.brandTeal, Color(0xFF0E5F7C)],
          ),
        ),
        child: InkWell(
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(
                AppSpacing.lg, AppSpacing.lg, AppSpacing.md, AppSpacing.lg),
            child: Row(
              children: [
                Container(
                  width: 52,
                  height: 52,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.16),
                    borderRadius: AppRadius.brMd,
                    border: Border.all(color: Colors.white.withValues(alpha: 0.35)),
                  ),
                  child: const Icon(Icons.document_scanner_outlined,
                      color: Colors.white, size: 28),
                ),
                const SizedBox(width: AppSpacing.lg),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Escanear credencial INE',
                          style: text.titleMedium?.copyWith(
                              color: Colors.white, fontWeight: FontWeight.w800)),
                      const SizedBox(height: 2),
                      Text('Llena nombre, CURP, clave de elector y domicilio',
                          style: text.bodySmall
                              ?.copyWith(color: Colors.white.withValues(alpha: 0.85))),
                    ],
                  ),
                ),
                const Icon(Icons.arrow_forward_rounded, color: Colors.white),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// ¿Simpatiza? Dos opciones grandes: decide qué más se pide.
class SupportsChoice extends StatelessWidget {
  final bool value;
  final ValueChanged<bool> onChanged;
  const SupportsChoice({super.key, required this.value, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('¿Simpatiza con el movimiento?',
            style: text.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
        Text('Si no simpatiza sólo se registra el domicilio',
            style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant)),
        const SizedBox(height: AppSpacing.md),
        Row(
          children: [
            Expanded(
              child: _ChoiceTile(
                selected: value,
                icon: Icons.favorite_rounded,
                label: 'Sí simpatiza',
                color: AppColors.success,
                onTap: () => onChanged(true),
              ),
            ),
            const SizedBox(width: AppSpacing.md),
            Expanded(
              child: _ChoiceTile(
                selected: !value,
                icon: Icons.home_outlined,
                label: 'No, sólo domicilio',
                color: colors.onSurfaceVariant,
                onTap: () => onChanged(false),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class _ChoiceTile extends StatelessWidget {
  final bool selected;
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;
  const _ChoiceTile(
      {required this.selected,
      required this.icon,
      required this.label,
      required this.color,
      required this.onTap});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return AnimatedContainer(
      duration: const Duration(milliseconds: 180),
      decoration: BoxDecoration(
        color: selected ? color.withValues(alpha: 0.10) : colors.surface,
        borderRadius: AppRadius.brMd,
        border: Border.all(
            color: selected ? color : colors.outlineVariant, width: selected ? 2 : 1),
      ),
      child: InkWell(
        borderRadius: AppRadius.brMd,
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(
              vertical: AppSpacing.md, horizontal: AppSpacing.md),
          child: Row(
            children: [
              Icon(selected ? Icons.radio_button_checked : Icons.radio_button_off,
                  size: 20, color: selected ? color : colors.outline),
              const SizedBox(width: AppSpacing.sm),
              Icon(icon, size: 18, color: selected ? color : colors.onSurfaceVariant),
              const SizedBox(width: AppSpacing.xs),
              Expanded(
                child: Text(label,
                    style: text.labelLarge?.copyWith(
                        color: selected ? color : colors.onSurfaceVariant,
                        fontWeight: selected ? FontWeight.w800 : FontWeight.w600)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// El GPS en una línea: punto de estado, si ya está dentro de la sección (con
/// el polígono guardado) y las coordenadas. Sigue al usuario mientras la ficha
/// está abierta y reporta lat/long al formulario.
class GpsStatusRow extends StatefulWidget {
  const GpsStatusRow({super.key});

  @override
  State<GpsStatusRow> createState() => _GpsStatusRowState();
}

class _GpsStatusRowState extends State<GpsStatusRow> {
  late final LocationBloc _location;
  int? _section;
  String? _geometry;

  @override
  void initState() {
    super.initState();
    _location = BlocProvider.of<LocationBloc>(context);
    _location.startFollowingUser();
  }

  @override
  void dispose() {
    _location.stopFollowingUser();
    super.dispose();
  }

  Future<void> _loadGeometry(int section) async {
    _section = section;
    final g = await CaptureCatalog().geometry(section);
    if (mounted) setState(() => _geometry = g);
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final loc = context.watch<LocationBloc>().state.lastKnownLocation;
    final form = context.read<FollowerFormBloc>();
    final section = int.tryParse(context.select((FollowerFormBloc b) => b.state.sectionNumber.value));

    if (section != null && section != _section) {
      _loadGeometry(section);
    }
    if (loc != null) {
      form.add(LatitudeChange(loc.latitude.toString()));
      form.add(LongitudeChange(loc.longitude.toString()));
    }

    // estado: buscando / dentro / cerca del borde / fuera
    final (Color dot, String title) = switch ((loc, _geometry)) {
      (null, _) => (AppColors.warning, 'Buscando señal de GPS…'),
      (_, null) => (AppColors.success, 'Ubicación lista'),
      _ => () {
          final d = distanceOutsideSection(loc!.latitude, loc.longitude, _geometry!);
          if (d == null || d == 0) return (AppColors.success, 'Dentro de la sección');
          if (d <= sectionToleranceMeters) {
            return (AppColors.success, 'En el borde de la sección (${d.round()} m)');
          }
          return (colors.error, 'A ${d.round()} m fuera de la sección');
        }(),
    };

    return Row(
      children: [
        _PulseDot(color: dot, pulsing: loc == null),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: text.labelLarge?.copyWith(fontWeight: FontWeight.w700)),
              Text(
                loc == null
                    ? 'Activa la ubicación y espera unos segundos'
                    : '${loc.latitude.toStringAsFixed(6)}, ${loc.longitude.toStringAsFixed(6)}',
                style: text.bodySmall?.copyWith(
                    color: colors.onSurfaceVariant,
                    fontFeatures: const [FontFeature.tabularFigures()]),
              ),
            ],
          ),
        ),
        Icon(Icons.gps_fixed, size: 18, color: colors.onSurfaceVariant),
      ],
    );
  }
}

class _PulseDot extends StatefulWidget {
  final Color color;
  final bool pulsing;
  const _PulseDot({required this.color, required this.pulsing});

  @override
  State<_PulseDot> createState() => _PulseDotState();
}

class _PulseDotState extends State<_PulseDot> with SingleTickerProviderStateMixin {
  late final AnimationController _c =
      AnimationController(vsync: this, duration: const Duration(milliseconds: 1100))
        ..repeat();

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 22,
      height: 22,
      child: AnimatedBuilder(
        animation: _c,
        builder: (_, __) => Stack(
          alignment: Alignment.center,
          children: [
            if (widget.pulsing)
              Container(
                width: 10 + 12 * _c.value,
                height: 10 + 12 * _c.value,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: widget.color.withValues(alpha: 0.35 * (1 - _c.value)),
                ),
              ),
            Container(
              width: 10,
              height: 10,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: widget.color,
                boxShadow: [
                  BoxShadow(color: widget.color.withValues(alpha: 0.45), blurRadius: 6),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
