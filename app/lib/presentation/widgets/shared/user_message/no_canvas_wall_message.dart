import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';

class NoCanvasWallMessage extends StatelessWidget {
  const NoCanvasWallMessage({super.key});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;

    return FadeInUp(
      duration: const Duration(milliseconds: 400),
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.xl),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(AppSpacing.xl),
              decoration: BoxDecoration(
                color: colors.secondaryContainer,
                shape: BoxShape.circle,
              ),
              child: Icon(Icons.travel_explore,
                  size: 56, color: colors.onSecondaryContainer),
            ),
            const SizedBox(height: AppSpacing.xl),
            Text(
              'No tienes registros vinculados',
              textAlign: TextAlign.center,
              style: textTheme.titleLarge,
            ),
            const SizedBox(height: AppSpacing.sm),
            Text(
              'Accede al menú de Lonas y Bardas para comenzar a capturar la información.',
              textAlign: TextAlign.center,
              style: textTheme.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }
}
