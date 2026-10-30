import 'package:flutter/material.dart';

import 'package:electorama_app/config/theme/app_radius.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';

/// Banner de identidad de módulo. Da a cada módulo (Lonas y Bardas,
/// Convencidos, Visitas) un color de acento, icono y una línea de propósito
/// para que el usuario distinga de inmediato en cuál está, aunque los
/// formularios compartan campos (geolocalización, calle, número).
class ModuleHeader extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final Color accent;

  const ModuleHeader({
    super.key,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.accent,
  });

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final onSurfaceVariant = Theme.of(context).colorScheme.onSurfaceVariant;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: accent.withValues(alpha: 0.12),
        borderRadius: AppRadius.brLg,
        border: Border.all(color: accent.withValues(alpha: 0.45)),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(AppSpacing.sm),
            decoration: BoxDecoration(
              color: accent,
              borderRadius: AppRadius.brMd,
            ),
            child: Icon(icon, color: Colors.white, size: 26),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: textTheme.titleMedium
                      ?.copyWith(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 2),
                Text(
                  subtitle,
                  style: textTheme.bodySmall?.copyWith(color: onSurfaceVariant),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
