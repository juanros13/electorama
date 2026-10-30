import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form_parts.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/presentation/blocs/user_settings/user_settings_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_dropdown_form_field.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_text_form_field.dart';

class FollowerFormAddress extends StatefulWidget {
  const FollowerFormAddress({super.key});

  @override
  State<FollowerFormAddress> createState() => _FollowerFormAddressState();
}

class _FollowerFormAddressState extends State<FollowerFormAddress> {
  @override
  void initState() {
    super.initState();
    context.read<UserSettingsBloc>().add(const LoadAllSettings());
    context.read<UserSettingsBloc>().add(const LoadSettings());
    context.read<UserSettingsBloc>().add(const LoadSections());
  }

  @override
  Widget build(BuildContext context) {
    final followerFormBloc = context.watch<FollowerFormBloc>();
    final userSettingsBloc = context.watch<UserSettingsBloc>();
    final textTheme = Theme.of(context).textTheme;

    dynamic nameResidenceMunicipality = '';

    final street = followerFormBloc.state.street;
    final externalNumber = followerFormBloc.state.externalNumber;
    final internalNumber = followerFormBloc.state.internalNumber;
    final zipcode = followerFormBloc.state.zipCode;
    final neighborhood = followerFormBloc.state.neighborhood;
    final catSex = followerFormBloc.state.catSex;

    if (userSettingsBloc.state.listSettings != null &&
        userSettingsBloc.state.listSettings!.isNotEmpty) {
      nameResidenceMunicipality =
          userSettingsBloc.state.municipalitySelected['name'];
      followerFormBloc.add(ResidenceMunicipalityChange(userSettingsBloc
          .state.listSettings!.first.municipality.idMun
          .toString()));
    }
    final sectionList = userSettingsBloc.state.sectionListSelected;

    final colors = Theme.of(context).colorScheme;
    final locked = followerFormBloc.state.sectionLocked;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // municipio y sección en una línea: los dos los fija el puesto
        if (locked)
          _PlaceChip(
            municipality: '$nameResidenceMunicipality',
            section: _sectionName(followerFormBloc.state.targetLabel),
          )
        else ...[
          if (nameResidenceMunicipality != '')
            _PlaceChip(municipality: '$nameResidenceMunicipality', section: null),
          const SizedBox(height: AppSpacing.md),
          if (sectionList.isEmpty)
            Card(
              margin: EdgeInsets.zero,
              clipBehavior: Clip.antiAlias,
              child: ListTile(
                leading: const Icon(Icons.tune),
                title: const Text('Configura tus secciones'),
                subtitle: const Text('Aún no eliges secciones para capturar'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => context.go('/sections'),
              ),
            )
          else
            CustomDropdownFormField(
              iconData: Icons.near_me,
              label: 'Sección',
              obscureText: false,
              isRequired: true,
              dropdownItems: sectionList
                  .map((section) => DropdownMenuItem<String>(
                      value: section.idSec.toString(),
                      child: Text(section.nomSec!)))
                  .toList(),
              onChange: (value) {
                if (value != null) followerFormBloc.add(SectionNumberChange(value));
              },
              isPure: true,
            ),
        ],
        const SizedBox(height: AppSpacing.md),
        CustomTextFormField(
          inputValue: street.value,
          iconData: Icons.signpost_outlined,
          isRequired: true,
          label: 'Calle',
          erroMessage: street.getErrorMessage,
          onChange: (value) => followerFormBloc.add(StreetChange(value)),
          isPure: street.isPure,
        ),
        const SizedBox(height: AppSpacing.md),
        FieldPair(
          left: CustomTextFormField(
            inputValue: externalNumber.value,
            isRequired: true,
            iconData: Icons.numbers,
            label: 'N.º ext.',
            erroMessage: externalNumber.getErrorMessage,
            onChange: (value) => followerFormBloc.add(ExternalNumberChange(value)),
            isPure: externalNumber.isPure,
          ),
          right: CustomTextFormField(
            inputValue: internalNumber.value,
            iconData: Icons.tag,
            label: 'N.º int.',
            erroMessage: internalNumber.getErrorMessage,
            onChange: (value) => followerFormBloc.add(InternalNumberChange(value)),
            isPure: internalNumber.isPure,
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        FieldPair(
          leftFlex: 3,
          rightFlex: 2,
          left: CustomTextFormField(
            inputValue: neighborhood.value,
            iconData: Icons.holiday_village_outlined,
            label: 'Colonia',
            erroMessage: neighborhood.getErrorMessage,
            onChange: (value) => followerFormBloc.add(NeighborhoodChange(value)),
            isPure: neighborhood.isPure,
          ),
          right: CustomTextFormField(
            inputValue: zipcode.value,
            onlyNumbers: true,
            maxLength: 5,
            iconData: Icons.markunread_mailbox_outlined,
            label: 'CP',
            erroMessage: zipcode.getErrorMessage,
            onChange: (value) => followerFormBloc.add(ZipCodeChange(value)),
            isPure: zipcode.isPure,
          ),
        ),
        const SizedBox(height: AppSpacing.md),
        Text('Sexo',
            style: textTheme.labelLarge?.copyWith(color: colors.onSurfaceVariant)),
        const SizedBox(height: AppSpacing.sm),
        SegmentedButton<int>(
          showSelectedIcon: false,
          segments: const [
            ButtonSegment(value: 2, icon: Icon(Icons.female), label: Text('Mujer')),
            ButtonSegment(value: 1, icon: Icon(Icons.male), label: Text('Hombre')),
          ],
          selected: {catSex.id ?? 1},
          onSelectionChanged: (v) => followerFormBloc.add(CatSexChange(v.first == 2
              ? const Sex(id: 2, nombre: 'Mujer')
              : const Sex(id: 1, nombre: 'Hombre'))),
        ),
      ],
    );
  }
}

/// "CENTRO · Sección 233" fijo, con el aviso de que lo pone el puesto.
class _PlaceChip extends StatelessWidget {
  final String municipality;
  final String? section;
  const _PlaceChip({required this.municipality, required this.section});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg, vertical: AppSpacing.md),
      decoration: BoxDecoration(
        color: colors.primary.withValues(alpha: 0.07),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: colors.primary.withValues(alpha: 0.20)),
      ),
      child: Row(
        children: [
          Icon(Icons.place_outlined, color: colors.primary, size: 22),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Text.rich(
              TextSpan(children: [
                TextSpan(
                    text: municipality.isEmpty ? '—' : municipality,
                    style: text.titleSmall?.copyWith(fontWeight: FontWeight.w800)),
                if (section != null)
                  TextSpan(
                      text: '  ·  $section',
                      style: text.titleSmall?.copyWith(
                          color: colors.primary, fontWeight: FontWeight.w800)),
              ]),
            ),
          ),
          if (section != null)
            Text('del puesto',
                style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
        ],
      ),
    );
  }
}

/// "Movilizador · S-0233A1-11 · …" → "Sección 233".
String _sectionName(String targetLabel) {
  final m = RegExp(r'S-(\d{4})').firstMatch(targetLabel);
  return m == null ? 'Sección del puesto' : 'Sección ${int.parse(m[1]!)}';
}
