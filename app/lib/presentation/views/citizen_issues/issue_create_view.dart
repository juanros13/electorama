import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_form/issue_form_cubit.dart';
import 'package:electorama_app/presentation/widgets/forms/citizen_issues/issue_form.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';

class IssueCreateView extends StatelessWidget {
  const IssueCreateView({super.key});

  @override
  Widget build(BuildContext context) {
    // Al reiniciar el form, el primer detalle cambia de uuid → re-key del form
    // para limpiar los campos de texto.
    final firstUuid = context
        .select<IssueFormCubit, String>((c) => c.state.details.first.uuid);

    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Nueva Incidencia'),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Column(
            children: [
              const Padding(
                padding: EdgeInsets.fromLTRB(
                    AppSpacing.lg, AppSpacing.sm, AppSpacing.lg, 0),
                child: ModuleHeader(
                  icon: Icons.report,
                  title: 'Incidencias',
                  subtitle: 'Atención ciudadana · captura offline',
                  accent: AppColors.issues,
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              IssueForm(key: ValueKey(firstUuid)),
            ],
          ),
        ),
      ),
    );
  }
}
