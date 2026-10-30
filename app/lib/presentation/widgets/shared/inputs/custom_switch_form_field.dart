import 'package:flutter/material.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';

/// Un sí/no como fila completa: se toca en cualquier parte para cambiarlo.
/// La etiqueta va en tamaño de cuerpo (antes 20 fijo, más grande que los
/// campos) y el switch con los colores del tema.
class CustomSwitchFormField extends StatelessWidget {
  final bool value;
  final String label;
  final Function(bool)? onChange;

  const CustomSwitchFormField({
    super.key,
    required this.label,
    this.onChange,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return Material(
      color: value ? colors.primary.withValues(alpha: 0.06) : Colors.transparent,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onChange == null ? null : () => onChange!(!value),
        child: Padding(
          padding: const EdgeInsets.symmetric(
              horizontal: AppSpacing.md, vertical: AppSpacing.sm),
          child: Row(
            children: [
              Expanded(
                child: Text(label,
                    style: text.bodyLarge?.copyWith(
                        fontWeight: value ? FontWeight.w700 : FontWeight.w500)),
              ),
              const SizedBox(width: AppSpacing.md),
              Switch(value: value, onChanged: onChange),
            ],
          ),
        ),
      ),
    );
  }
}
