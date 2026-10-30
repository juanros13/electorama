import 'package:flutter/material.dart';
import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/presentation/widgets/forms/canvas_wall/canvas_wall_form.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';


class CanvasWallCreateView extends StatelessWidget {
  static GlobalKey<NavigatorState> navKey = GlobalKey();

  const CanvasWallCreateView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Crear Nueva Lona o Barda'),
      ),
      body: const SafeArea(
        child: Padding(
          padding: EdgeInsets.symmetric(vertical: 10),
          child: SingleChildScrollView(
            child: Column(
              children: [
                Padding(
                  padding: EdgeInsets.fromLTRB(10, 8, 10, 0),
                  child: ModuleHeader(
                    icon: Icons.campaign,
                    title: 'Lonas y Bardas',
                    subtitle: 'Propaganda fija · registro georreferenciado',
                    accent: AppColors.brandTeal,
                  ),
                ),
                CanvasWallForm(),
              ],
            ),
          ),
        ),
      ),
    );
  }
}