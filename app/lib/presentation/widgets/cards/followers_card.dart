import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/config/theme/app_radius.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/presentation/blocs/follower/follower_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class FollowerCard extends StatelessWidget {
  final Follower follower;
  final int index;
  const FollowerCard({super.key, required this.follower, required this.index});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final synced = follower.isInSync;
    // cómo terminó el envío: pendiente, en la estructura, ya existía o rechazado
    final (statusLabel, statusIcon, statusColor) = switch (follower.syncStatus) {
      FollowerSyncStatus.synced => ('Vinculado', Icons.cloud_done, AppColors.success),
      FollowerSyncStatus.duplicate => ('Ya existía', Icons.how_to_reg, AppColors.success),
      FollowerSyncStatus.rejected => ('Rechazado', Icons.error_outline, colors.error),
      _ => ('Pendiente', Icons.pending_actions, AppColors.warning),
    };

    final fullName = [follower.lastName, follower.surName, follower.names]
        .where((e) => e != null && e!.trim().isNotEmpty)
        .join(' ');
    final fecha = follower.created != null
        ? follower.created!.toString().substring(0, 10)
        : '';

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.lg),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Convencido · $fecha',
                    style: textTheme.bodyMedium
                        ?.copyWith(color: colors.onSurfaceVariant)),
                Text('N.º ${index + 1}', style: textTheme.titleMedium),
              ],
            ),
            const SizedBox(height: AppSpacing.sm),
            Text(
              fullName.isEmpty ? 'Simpatizante (sin nombre)' : fullName,
              style: textTheme.titleLarge,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: AppSpacing.sm),
            if (follower.cellPhoneNumber != null &&
                follower.cellPhoneNumber!.isNotEmpty)
              _MetaRow(
                  icon: Icons.phone_outlined, text: follower.cellPhoneNumber!),
            if (follower.curp != null && follower.curp!.isNotEmpty)
              _MetaRow(icon: Icons.badge_outlined, text: 'CURP: ${follower.curp}'),
            if (follower.ine != null && follower.ine!.isNotEmpty)
              _MetaRow(
                  icon: Icons.account_box_outlined,
                  text: 'Clave: ${follower.ine}'),
            if (follower.positionLabel != null && follower.positionLabel!.isNotEmpty)
              _MetaRow(
                  icon: Icons.account_tree_outlined,
                  text: 'Quedó en ${follower.positionLabel}'),
            // el motivo del servidor: qué corregir (rechazado) o dónde ya estaba
            if (follower.syncError != null && follower.syncError!.isNotEmpty)
              Container(
                width: double.infinity,
                margin: const EdgeInsets.only(top: AppSpacing.sm),
                padding: const EdgeInsets.all(AppSpacing.sm),
                decoration: BoxDecoration(
                  color: statusColor.withValues(alpha: 0.08),
                  borderRadius: const BorderRadius.all(Radius.circular(AppRadius.full)),
                ),
                child: Text(follower.syncError!,
                    style: textTheme.bodySmall?.copyWith(color: statusColor)),
              ),
            const SizedBox(height: AppSpacing.md),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: AppSpacing.md, vertical: AppSpacing.sm),
                  decoration: BoxDecoration(
                    color: statusColor.withValues(alpha: 0.15),
                    borderRadius: const BorderRadius.all(
                        Radius.circular(AppRadius.full)),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(statusIcon, size: 18, color: statusColor),
                      const SizedBox(width: AppSpacing.sm),
                      Text(statusLabel,
                          style: textTheme.labelLarge
                              ?.copyWith(color: statusColor)),
                    ],
                  ),
                ),
                if (!synced)
                  TextButton.icon(
                    style: TextButton.styleFrom(foregroundColor: colors.error),
                    onPressed: () => _confirmDelete(context),
                    icon: const Icon(Icons.delete_outline, size: 18),
                    label: const Text('Eliminar'),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _confirmDelete(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('¿Estás seguro?'),
        content: Text(follower.syncStatus == FollowerSyncStatus.rejected
            ? 'El servidor no lo aceptó. Si lo eliminas, captúralo de nuevo con los datos correctos.'
            : 'Este convencido aún no ha sido sincronizado. ¿Deseas eliminarlo?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: colors.error,
              foregroundColor: colors.onError,
            ),
            onPressed: () {
              Navigator.pop(dialogContext);
              context
                  .read<FollowerBloc>()
                  .add(DeleteFollower(isarId: follower.id!));
            },
            child: const Text('Eliminar'),
          ),
        ],
      ),
    );
  }
}

class _MetaRow extends StatelessWidget {
  final IconData icon;
  final String text;
  const _MetaRow({required this.icon, required this.text});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.only(bottom: 2),
      child: Row(
        children: [
          Icon(icon, size: 16, color: colors.onSurfaceVariant),
          const SizedBox(width: AppSpacing.xs),
          Expanded(
            child: Text(text,
                style: textTheme.bodyMedium
                    ?.copyWith(color: colors.onSurfaceVariant),
                maxLines: 1,
                overflow: TextOverflow.ellipsis),
          ),
        ],
      ),
    );
  }
}
