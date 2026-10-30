import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_switch_form_field.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_text_form_field.dart';

class FollowerFormPersonalInformation extends StatelessWidget {
  const FollowerFormPersonalInformation({super.key});

  @override
  Widget build(BuildContext context) {
    final followerFormBloc = context.watch<FollowerFormBloc>();

    final names = followerFormBloc.state.names;
    final lastName = followerFormBloc.state.lastName;
    final surName = followerFormBloc.state.surName;
    final curp = followerFormBloc.state.curp;
    final ine = followerFormBloc.state.ine;
    final expirationYear = followerFormBloc.state.expirationYear;
    final isLegalAge = followerFormBloc.state.isLegalAge;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // el título lo pone el paso (FormStep) del formulario
        CustomSwitchFormField(
            value: isLegalAge,
            onChange: (value) => followerFormBloc.add(IsLegalAgeChange(value)),
            label: '¿Es mayor de edad?',
          ),
        const SizedBox(height: AppSpacing.sm),
        CustomTextFormField(
          inputValue: names.value,
          onlyUppercase: true,
          isRequired: true,
          iconData: Icons.person,
          label: 'Nombre(s)',
          erroMessage: names.getErrorMessage,
          onChange: (value) => followerFormBloc.add(NamesChange(value)),
          isPure: names.isPure,
        ),
        const SizedBox(height: AppSpacing.md),
        CustomTextFormField(
          inputValue: lastName.value,
          onlyUppercase: true,
          isRequired: true,
          iconData: Icons.person,
          label: 'Apellido paterno',
          erroMessage: lastName.getErrorMessage,
          onChange: (value) => followerFormBloc.add(LastNameChange(value)),
          isPure: lastName.isPure,
        ),
        const SizedBox(height: AppSpacing.md),
        CustomTextFormField(
          inputValue: surName.value,
          onlyUppercase: true,
          isRequired: true,
          iconData: Icons.person,
          label: 'Apellido materno',
          erroMessage: surName.getErrorMessage,
          onChange: (value) => followerFormBloc.add(SurNameChange(value)),
          isPure: surName.isPure,
        ),
        const SizedBox(height: AppSpacing.md),
        CustomTextFormField(
          inputValue: curp.value,
          onlyUppercase: true,
          maxLength: 18,
          iconData: Icons.account_box_outlined,
          label: 'CURP',
          erroMessage: curp.getErrorMessage,
          onChange: (value) => followerFormBloc.add(CurpChange(value)),
          isPure: curp.isPure,
        ),
        const SizedBox(height: AppSpacing.md),
        CustomTextFormField(
          inputValue: ine.value,
          onlyUppercase: true,
          maxLength: 18,
          iconData: Icons.badge_outlined,
          label: 'Clave de elector',
          erroMessage: ine.getErrorMessage,
          onChange: (value) => followerFormBloc.add(IneChange(value)),
          isPure: ine.isPure,
        ),
        const SizedBox(height: AppSpacing.md),
        if (ine.value.isNotEmpty)
          CustomTextFormField(
            inputValue: expirationYear.value,
            textInputType: TextInputType.number,
            onlyNumbers: true,
            maxLength: 4,
            iconData: Icons.event_available_outlined,
            label: 'Año de expiración',
            erroMessage: expirationYear.getErrorMessage,
            onChange: (value) =>
                followerFormBloc.add(ExpirationYearChange(value)),
            isPure: expirationYear.isPure,
          ),
      ],
    );
  }
}
