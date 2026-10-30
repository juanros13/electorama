import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/domain/entities/user_settings/user_settings.dart';
import 'package:electorama_app/presentation/blocs/user_settings/user_settings_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_dropdown_form_field.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';

/// Configuración de las secciones electorales que cubre el usuario.
/// Alimenta el dropdown "Sección" del formulario de Convencidos
/// (que lee de `sectionListSelected`). Funciona offline: municipios y
/// secciones salen del CSV en assets/geography.
class SectionsScreen extends StatefulWidget {
  static const name = 'sections';
  const SectionsScreen({super.key});

  @override
  State<SectionsScreen> createState() => _SectionsScreenState();
}

class _SectionsScreenState extends State<SectionsScreen> {
  String _query = '';

  @override
  void initState() {
    super.initState();
    context.read<UserSettingsBloc>().add(const LoadSettings());
    context.read<UserSettingsBloc>().add(const LoadSections());
  }

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;
    final userSettingsBloc = context.watch<UserSettingsBloc>();

    final municipalityList = userSettingsBloc.state.municipalitiesList;
    final municipalitySelected = userSettingsBloc.state.municipalitySelected;
    final selectedMunValue =
        '${municipalitySelected['id']}, ${municipalitySelected['name']}';
    final hasMatch = municipalityList
        .any((m) => '${m['id']}, ${m['nomNum']}' == selectedMunValue);

    final sectionList = userSettingsBloc.state.sectionList;
    final selected = userSettingsBloc.state.sectionListSelected;

    final filtered = _query.isEmpty
        ? sectionList
        : sectionList
            .where((s) => (s.nomSec ?? '').contains(_query.trim()))
            .toList();

    bool isSel(SectionsSelected s) => selected.any((e) => e.idSec == s.idSec);

    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Mis secciones'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/follower/1'),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const ModuleHeader(
                icon: Icons.tune,
                title: 'Configura tus secciones',
                subtitle: 'Elige municipio y las secciones que cubres',
                accent: AppColors.brandGreen,
              ),
              const SizedBox(height: AppSpacing.lg),
              Text('Municipio', style: textTheme.titleSmall),
              const SizedBox(height: AppSpacing.sm),
              CustomDropdownFormField(
                key: ValueKey('mun-$selectedMunValue-${municipalityList.length}'),
                selectedValue: hasMatch ? selectedMunValue : '',
                iconData: Icons.location_city,
                label: 'Municipio',
                obscureText: false,
                isRequired: true,
                dropdownItems: municipalityList
                    .map((m) => DropdownMenuItem<String>(
                          value: '${m['id']}, ${m['nomNum']}',
                          child: Text('${m['nomNum']}'),
                        ))
                    .toList(),
                onChange: (value) {
                  if (value == null) return;
                  final parts = value.split(',');
                  userSettingsBloc.add(SelectMunicipality({
                    'id': int.parse(parts[0].trim()),
                    'name': parts[1].trim(),
                  }));
                  userSettingsBloc.add(const LoadSections());
                  setState(() => _query = '');
                },
                isPure: true,
              ),
              const SizedBox(height: AppSpacing.lg),
              Row(
                children: [
                  Text('Secciones', style: textTheme.titleSmall),
                  const Spacer(),
                  Text('${selected.length} seleccionadas',
                      style: textTheme.bodySmall
                          ?.copyWith(color: colors.onSurfaceVariant)),
                ],
              ),
              const SizedBox(height: AppSpacing.sm),
              TextField(
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  prefixIcon: Icon(Icons.search),
                  hintText: 'Buscar sección',
                ),
                onChanged: (v) => setState(() => _query = v),
              ),
              if (selected.isNotEmpty) ...[
                const SizedBox(height: AppSpacing.sm),
                Wrap(
                  spacing: AppSpacing.sm,
                  children: selected
                      .map((s) => InputChip(
                            label: Text(s.nomSec ?? ''),
                            onDeleted: () =>
                                userSettingsBloc.add(RemoveSectionsSelected(s)),
                          ))
                      .toList(),
                ),
              ],
              const SizedBox(height: AppSpacing.sm),
              Expanded(
                child: sectionList.isEmpty
                    ? Center(
                        child: Text('Cargando secciones…',
                            style: textTheme.bodyMedium
                                ?.copyWith(color: colors.onSurfaceVariant)),
                      )
                    : ListView.builder(
                        itemCount: filtered.length,
                        itemBuilder: (context, i) {
                          final s = filtered[i];
                          return CheckboxListTile(
                            value: isSel(s),
                            title: Text('Sección ${s.nomSec ?? ''}'),
                            controlAffinity: ListTileControlAffinity.leading,
                            onChanged: (v) {
                              if (v == true) {
                                userSettingsBloc.add(AddSectionsSelected(s));
                              } else {
                                userSettingsBloc.add(RemoveSectionsSelected(s));
                              }
                            },
                          );
                        },
                      ),
              ),
              const SizedBox(height: AppSpacing.sm),
              FilledButton.icon(
                style: FilledButton.styleFrom(
                    minimumSize: const Size.fromHeight(50)),
                onPressed: selected.isEmpty
                    ? null
                    : () {
                        userSettingsBloc.add(const AddSectionSelected());
                        CustomSnackbar.showSnackbar(context, TypeSnackbar.success,
                            'Secciones guardadas correctamente.');
                        context.go('/follower/1');
                      },
                icon: const Icon(Icons.save),
                label: const Text('Guardar'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
