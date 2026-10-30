import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_switch_form_field.dart';

class FollowerFormSurvey extends StatelessWidget {
  const FollowerFormSurvey({super.key});

  @override
  Widget build(BuildContext context) {
    final followerFormBloc = context.watch<FollowerFormBloc>();
    final textTheme = Theme.of(context).textTheme;

    final numberSelected = followerFormBloc.state.numberOfPeopleInvited;
    final committee = followerFormBloc.state.committeeParticipation;
    final pollin =
        followerFormBloc.state.pollinStationRepresentativeParticipation;
    final inviting = followerFormBloc.state.invitingOthersToParticipate;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // el título lo pone el paso (FormStep); la pregunta va como guía
        Text('¿En cuál de estas formas podría apoyar a la cuarta transformación?',
            style: textTheme.bodyMedium),
        const SizedBox(height: AppSpacing.sm),
        CustomSwitchFormField(
            value: committee,
            onChange: (value) =>
                followerFormBloc.add(CommitteeParticipationChange(value)),
            label: 'Participar en un comité de defensa de la 4T',
          ),
        CustomSwitchFormField(
            value: pollin,
            onChange: (value) => followerFormBloc
                .add(PollinStationRepresentativeParticipationChange(value)),
            label: 'Participar en la defensa del voto como representante de casilla',
          ),
        CustomSwitchFormField(
            value: inviting,
            onChange: (value) =>
                followerFormBloc.add(InvitingOthersToParticipateChange(value)),
            label: 'Invitando a más personas a participar',
          ),
        // cuántas, sólo si está invitando (el servidor lo ignora si no)
        if (followerFormBloc.state.invitingOthersToParticipate) ...[
          const SizedBox(height: AppSpacing.md),
          Text('¿A cuántas personas podría compartirle información sobre la 4T?',
              style: textTheme.bodyMedium),
          const SizedBox(height: AppSpacing.sm),
          SizedBox(
            width: double.infinity,
            child: SegmentedButton<NumberOfPeopleInvited>(
              showSelectedIcon: false,
              segments: const [
                ButtonSegment(value: NumberOfPeopleInvited.uno, label: Text('1')),
                ButtonSegment(value: NumberOfPeopleInvited.dos, label: Text('2')),
                ButtonSegment(value: NumberOfPeopleInvited.tres, label: Text('3')),
                ButtonSegment(value: NumberOfPeopleInvited.cuatro, label: Text('4')),
                ButtonSegment(value: NumberOfPeopleInvited.cinco, label: Text('5')),
                ButtonSegment(value: NumberOfPeopleInvited.masdecinco, label: Text('5+')),
              ],
              selected: {numberSelected},
              onSelectionChanged: (v) =>
                  followerFormBloc.add(NumberOfPeopleInvitingChange(v.first)),
            ),
          ),
        ],
      ],
    );
  }
}
