import 'package:flutter/material.dart';
import 'package:electorama_app/config/theme/app_colors.dart';

enum TypeSnackbar { success, error }

class CustomSnackbar {
  static void showSnackbar(
    BuildContext context,
    TypeSnackbar typeSnackbar,
    String valueText,
  ) {
    final isError = typeSnackbar == TypeSnackbar.error;
    final bg = isError ? AppColors.danger : AppColors.success;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.only(bottom: 58.0, left: 16, right: 16),
        backgroundColor: bg,
        content: Row(
          children: [
            Icon(
              isError ? Icons.error_outline : Icons.check_circle_outline,
              color: Colors.white,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(valueText, style: const TextStyle(color: Colors.white)),
            ),
          ],
        ),
        action: SnackBarAction(
          label: 'Cerrar',
          textColor: Colors.white,
          onPressed: () {},
        ),
      ),
    );
  }
}
