import 'package:flutter/material.dart';
import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/presentation/widgets/forms/visits/visit_people_form.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';

class VisitCreateView extends StatelessWidget {
  static GlobalKey<NavigatorState> navKey = GlobalKey();

  const VisitCreateView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Crear Visita'),
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
                    icon: Icons.how_to_reg,
                    title: 'Visitas',
                    subtitle: 'Recorrido territorial · puerta por puerta',
                    accent: AppColors.warning,
                  ),
                ),
                VisitsPeopleForm(),
              ],
            ),
          ),
        ),
      ),
    );
  }
}