import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_switch_form_field.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_text_form_field.dart';

class FollowerFormContact extends StatelessWidget {
  const FollowerFormContact({super.key});

  @override
  Widget build(BuildContext context) {
    final followerFormBloc = context.watch<FollowerFormBloc>();

    final supportsWhatsapp = followerFormBloc.state.supportsWhatsapp;
    final phone = followerFormBloc.state.cellPhoneNumber;
    final phoneOther = followerFormBloc.state.cellPhoneNumberOther;
    final email = followerFormBloc.state.email;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // el título lo pone el paso (FormStep) del formulario
        CustomTextFormField(
          isRequired: true,
          textInputType: TextInputType.number,
          onlyNumbers: true,
          maxLength: 10,
          iconData: Icons.phone_iphone,
          label: 'Teléfono',
          erroMessage: phone.getErrorMessage,
          onChange: (value) => followerFormBloc.add(PhoneChange(value)),
          isPure: phone.isPure,
        ),
        CustomSwitchFormField(
            value: supportsWhatsapp,
            onChange: (value) =>
                followerFormBloc.add(SupportsWhatsappChange(value)),
            label: '¿El número proporcionado tiene WhatsApp?',
          ),
        const SizedBox(height: AppSpacing.md),
        CustomTextFormField(
          textInputType: TextInputType.number,
          onlyNumbers: true,
          maxLength: 10,
          iconData: Icons.phone_outlined,
          label: 'Otro teléfono',
          erroMessage: phoneOther.getErrorMessage,
          onChange: (value) => followerFormBloc.add(PhoneOtherChange(value)),
          isPure: phoneOther.isPure,
        ),
        const SizedBox(height: AppSpacing.md),
        CustomTextFormField(
          iconData: Icons.email_outlined,
          label: 'Correo electrónico',
          erroMessage: email.getErrorMessage,
          onChange: (value) => followerFormBloc.add(EmailChange(value)),
          isPure: email.isPure,
        ),
      ],
    );
  }
}
