import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';

class NoFollowersSyncMessage extends StatelessWidget {
  const NoFollowersSyncMessage({super.key});

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
              child: Icon(Icons.person_add_alt_1_outlined,
                  size: 56, color: colors.onSecondaryContainer),
            ),
            const SizedBox(height: AppSpacing.xl),
            Text('No tienes convencidos pendientes',
                textAlign: TextAlign.center, style: textTheme.titleLarge),
          ],
        ),
      ),
    );
  }
}
