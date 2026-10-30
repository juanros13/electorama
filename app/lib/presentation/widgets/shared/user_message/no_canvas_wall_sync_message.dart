import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';

class NoCanvasWallSyncMessage extends StatelessWidget {
  const NoCanvasWallSyncMessage({super.key});

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
              child: Icon(Icons.assignment_outlined,
                  size: 56, color: colors.onSecondaryContainer),
            ),
            const SizedBox(height: AppSpacing.xl),
            Text(
              'No tienes registros pendientes',
              textAlign: TextAlign.center,
              style: textTheme.titleLarge,
            ),
          ],
        ),
      ),
    );
  }
}
