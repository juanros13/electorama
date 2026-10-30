import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// Un destino de [AppNavigationBar].
class AppNavDestination {
  final IconData icon;
  final IconData selectedIcon;
  final String label;

  const AppNavDestination({
    required this.icon,
    required this.selectedIcon,
    required this.label,
  });
}

/// Barra de navegación Material 3 reutilizable. Navega con go_router a
/// `$routePrefix/$index`. Reemplaza las antiguas `BottomNavigationBar`
/// inconsistentes (estilo tomado del `navigationBarTheme`).
class AppNavigationBar extends StatelessWidget {
  final int currentIndex;
  final String routePrefix;
  final List<AppNavDestination> destinations;

  const AppNavigationBar({
    super.key,
    required this.currentIndex,
    required this.routePrefix,
    required this.destinations,
  });

  @override
  Widget build(BuildContext context) {
    return NavigationBar(
      selectedIndex: currentIndex,
      onDestinationSelected: (index) {
        FocusManager.instance.primaryFocus?.unfocus();
        context.go('$routePrefix/$index');
      },
      destinations: destinations
          .map(
            (d) => NavigationDestination(
              icon: Icon(d.icon),
              selectedIcon: Icon(d.selectedIcon),
              label: d.label,
            ),
          )
          .toList(),
    );
  }
}
