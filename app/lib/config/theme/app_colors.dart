import 'package:flutter/material.dart';

/// Paleta de marca y colores semánticos.
///
/// Marca derivada de los logos: teal de "núcleo" (primario/seed) y
/// verde de "electorama" (acento). Los semánticos evitan usar
/// `Colors.red/green/amber` sueltos por la app.
class AppColors {
  AppColors._();

  // Marca
  static const Color brandTeal = Color(0xFF1789B0); // núcleo  -> seed/primary
  static const Color brandGreen = Color(0xFF8BC34A); // electorama -> acento

  // Semánticos
  static const Color success = Color(0xFF2E7D32);
  static const Color warning = Color(0xFFF59E0B);
  static const Color danger = Color(0xFFD32F2F);

  // Acentos de módulo (identidad por vista)
  static const Color issues = Color(0xFF8E24AA); // incidencias ciudadanas
}
