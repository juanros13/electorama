import 'package:flutter/material.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';

/// Aviso para la pestaña de sincronización cuando no hay sesión.
/// La captura funciona offline; sincronizar requiere iniciar sesión.
class LoginToSyncHint extends StatelessWidget {
  final String message;
  const LoginToSyncHint({super.key, required this.message});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;

    return Padding(
      padding: const EdgeInsets.all(AppSpacing.xl),
      child: Column(
        children: [
          Icon(Icons.cloud_off_outlined, size: 48, color: colors.onSurfaceVariant),
          const SizedBox(height: AppSpacing.md),
          Text(
            message,
            textAlign: TextAlign.center,
            style: textTheme.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
          ),
        ],
      ),
    );
  }
}
