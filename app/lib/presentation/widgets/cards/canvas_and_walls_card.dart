import 'dart:typed_data';

import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall/canvas_wall_bloc.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall_form/canvas_wall_form_bloc.dart';
import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/config/theme/app_radius.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class CanvasAndWallsCard extends StatefulWidget {
  final CanvasWall canvasWall;
  final int index;
  const CanvasAndWallsCard({super.key, required this.canvasWall, required this.index});

  @override
  State<CanvasAndWallsCard> createState() => _CanvasAndWallsCardState();
}

class _CanvasAndWallsCardState extends State<CanvasAndWallsCard> {
  @override
  void initState() {
    context.read<CanvasWallBloc>().add(const LoadCanvasWall());
    super.initState();
  }
  @override
  Widget build(BuildContext context) {
    final canvasWallFormBloc = context.read<CanvasWallFormBloc>();
    final projectsOptions = canvasWallFormBloc.state.typesProjects;
    final categoriesOptions = canvasWallFormBloc.state.typesCategories;

    final categoryName = categoriesOptions.where((category) =>category.id == int.parse(widget.canvasWall.categoryId)).first.name;

    final projectName = projectsOptions.where((category) =>category.id == int.parse(widget.canvasWall.projectId)).first.name;

    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final synced = widget.canvasWall.isInSync;
    final statusColor = synced ? AppColors.success : AppColors.warning;

    return Card(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // ── Hero: foto protagonista + badge de estado ──
          Stack(
            children: [
              SizedBox(
                height: 190,
                width: double.infinity,
                child: widget.canvasWall.file != null
                    ? Image.memory(
                        Uint8List.fromList(widget.canvasWall.file!),
                        fit: BoxFit.cover,
                      )
                    : Container(
                        color: colors.surfaceContainerHigh,
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.image_not_supported_outlined,
                                size: 40, color: colors.onSurfaceVariant),
                            const SizedBox(height: AppSpacing.sm),
                            Text('Sin foto',
                                style: textTheme.bodyMedium
                                    ?.copyWith(color: colors.onSurfaceVariant)),
                          ],
                        ),
                      ),
              ),
              Positioned(
                top: AppSpacing.md,
                right: AppSpacing.md,
                child: _StatusBadge(synced: synced, color: statusColor),
              ),
            ],
          ),

          // ── Info ──
          Padding(
            padding: const EdgeInsets.all(AppSpacing.lg),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Text(
                        categoryName,
                        style: textTheme.titleLarge,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    const SizedBox(width: AppSpacing.sm),
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: AppSpacing.sm, vertical: 2),
                      decoration: BoxDecoration(
                        color: colors.surfaceContainerHigh,
                        borderRadius: const BorderRadius.all(
                            Radius.circular(AppRadius.full)),
                      ),
                      child: Text('N.º ${widget.index + 1}',
                          style: textTheme.labelMedium
                              ?.copyWith(color: colors.onSurfaceVariant)),
                    ),
                  ],
                ),
                const SizedBox(height: AppSpacing.sm),
                Row(
                  children: [
                    Icon(Icons.flag_outlined,
                        size: 16, color: colors.onSurfaceVariant),
                    const SizedBox(width: AppSpacing.xs),
                    Flexible(
                      child: Text(
                        projectName,
                        style: textTheme.bodyMedium
                            ?.copyWith(color: colors.onSurfaceVariant),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Icon(Icons.calendar_today_outlined,
                        size: 16, color: colors.onSurfaceVariant),
                    const SizedBox(width: AppSpacing.xs),
                    Text(widget.canvasWall.date,
                        style: textTheme.bodyMedium
                            ?.copyWith(color: colors.onSurfaceVariant)),
                  ],
                ),
                if (!synced) ...[
                  const SizedBox(height: AppSpacing.sm),
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton.icon(
                      onPressed: () {
                        _deleteVisitesPeopleDialog(
                            widget.canvasWall, categoryName);
                      },
                      style: TextButton.styleFrom(foregroundColor: colors.error),
                      icon: const Icon(Icons.delete_outline, size: 18),
                      label: const Text('Eliminar'),
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<String?> _deleteVisitesPeopleDialog(CanvasWall canvasWall, String categoryName) {

    final int canvasWallId = canvasWall.isarId!;
    final colors = Theme.of(context).colorScheme;

    return showDialog<String>(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        title: const Text('¿Estás seguro?'),
        content: Text('La $categoryName aun no ha sido sincronizada en el sistema. ¿Deseas eliminarla?'),
        actions: <Widget>[
          TextButton(
            onPressed: () {
              Navigator.pop(context, 'Cancelar');
            },
            child: const Text('Cancelar'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: colors.error,
              foregroundColor: colors.onError,
            ),
            onPressed: () {
              Navigator.pop(context, 'Eliminar');
              context.read<CanvasWallBloc>().add(DeleteCanvasWall(isarId: canvasWallId));
            },
            child: const Text('Eliminar'),
          ),
        ],
      ),
    );
  }
}

/// Pill sólido de estado que se superpone sobre la foto.
class _StatusBadge extends StatelessWidget {
  final bool synced;
  final Color color;
  const _StatusBadge({required this.synced, required this.color});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md, vertical: 6),
      decoration: BoxDecoration(
        color: color,
        borderRadius: const BorderRadius.all(Radius.circular(AppRadius.full)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.25),
            blurRadius: 6,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(synced ? Icons.cloud_done : Icons.pending_actions,
              size: 16, color: Colors.white),
          const SizedBox(width: 6),
          Text(
            synced ? 'Vinculado' : 'Pendiente',
            style: textTheme.labelLarge
                ?.copyWith(color: Colors.white, fontWeight: FontWeight.w700),
          ),
        ],
      ),
    );
  }
}