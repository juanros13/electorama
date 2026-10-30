import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/infrastructure/datasources/storage/app_database.dart';
import 'package:electorama_app/infrastructure/services/capture_catalog.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';

const _levelNames = {
  'REFERENT': 'Coordinador',
  'ZONE_COORDINATOR': 'Promotor',
  'PROMOTER': 'Movilizador',
};

/// "Queda en": el puesto donde se guarda el convencido, de los que se bajaron
/// con red (capture-targets). El movilizador tiene uno; promotor y referente
/// eligen entre el suyo y los de su rama.
class FollowerFormTarget extends StatefulWidget {
  const FollowerFormTarget({super.key});

  @override
  State<FollowerFormTarget> createState() => _FollowerFormTargetState();
}

class _FollowerFormTargetState extends State<FollowerFormTarget> {
  late final Future<List<CaptureTargetRow>> _targets = CaptureCatalog().targets();

  String _label(CaptureTargetRow t) =>
      '${_levelNames[t.level] ?? t.level} · ${t.label}${t.holderName == null ? '' : ' · ${_clean(t.holderName)}'}';

  /// Los nombres del padrón traen tabuladores y espacios dobles.
  static String _clean(String? s) => (s ?? '').replaceAll(RegExp(r'\s+'), ' ').trim();

  void _pick(CaptureTargetRow t) {
    context.read<FollowerFormBloc>().add(TargetChange(
        positionId: t.positionId, label: _label(t), sectionGeoId: t.sectionGeoId));
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final selected = context.select((FollowerFormBloc b) => b.state.positionId);

    return FutureBuilder<List<CaptureTargetRow>>(
      future: _targets,
      builder: (context, snap) {
        final targets = snap.data ?? const [];
        if (snap.connectionState != ConnectionState.done) {
          return const LinearProgressIndicator();
        }
        if (targets.isEmpty) {
          return _TargetRow(
            icon: Icons.cloud_download_outlined,
            caption: 'SIN PUESTO GUARDADO',
            title: 'Se asigna al sincronizar',
            subtitle: 'Entra con internet una vez para bajar tu puesto',
            warn: true,
          );
        }
        // por omisión el puesto propio de movilizador (o el primero)
        if (selected == null || !targets.any((t) => t.positionId == selected)) {
          final initial = targets.firstWhere(
              (t) => t.own && t.level == 'PROMOTER',
              orElse: () => targets.first);
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (mounted) _pick(initial);
          });
        }
        if (targets.length == 1) {
          final t = targets.first;
          return _TargetRow(
            icon: Icons.account_tree_outlined,
            caption: 'QUEDA EN · ${(_levelNames[t.level] ?? t.level).toUpperCase()}',
            title: t.label,
            subtitle: _clean(t.holderName),
          );
        }
        return Padding(
          padding: const EdgeInsets.only(bottom: AppSpacing.xs),
          child: DropdownButtonFormField<int>(
            initialValue: selected,
            isExpanded: true,
            decoration: const InputDecoration(
              labelText: 'Queda en',
              prefixIcon: Icon(Icons.account_tree_outlined),
            ),
            items: [
              for (final t in targets)
                DropdownMenuItem(
                  value: t.positionId,
                  child: Text('${t.own ? '★ ' : ''}${_label(t)}',
                      overflow: TextOverflow.ellipsis, style: textTheme.bodyMedium),
                ),
            ],
            onChanged: (id) {
              final t = targets.where((t) => t.positionId == id).firstOrNull;
              if (t != null) _pick(t);
            },
          ),
        );
      },
    );
  }
}

/// Una línea de la tarjeta de contexto: ícono en cuadro, rótulo pequeño, la
/// clave del puesto en grande y el titular.
class _TargetRow extends StatelessWidget {
  final IconData icon;
  final String caption;
  final String title;
  final String subtitle;
  final bool warn;
  const _TargetRow(
      {required this.icon,
      required this.caption,
      required this.title,
      required this.subtitle,
      this.warn = false});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final accent = warn ? colors.tertiary : colors.primary;
    return Row(
      children: [
        Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: accent.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: accent, size: 22),
        ),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(caption,
                  style: text.labelSmall?.copyWith(
                      color: colors.onSurfaceVariant, letterSpacing: 1.1)),
              Text(title,
                  style: text.titleLarge?.copyWith(
                      fontWeight: FontWeight.w800, letterSpacing: -0.2)),
              if (subtitle.isNotEmpty)
                Text(subtitle,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant)),
            ],
          ),
        ),
      ],
    );
  }
}
