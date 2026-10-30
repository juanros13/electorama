import 'package:flutter/material.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
import 'package:animate_do/animate_do.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/domain/entities/followers/follower.dart';
import 'package:electorama_app/infrastructure/mappers/follower/follower_mapper.dart';
import 'package:electorama_app/presentation/blocs/follower/follower_bloc.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form_address.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form_contact.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form_personal_information.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form_survey.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form_target.dart';
import 'package:electorama_app/infrastructure/services/capture_catalog.dart';
import 'package:electorama_app/utils/section_geometry.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form_parts.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';

class FollowerForm extends StatelessWidget {
  const FollowerForm({super.key});

  @override
  Widget build(BuildContext context) {
    final followerFormBloc = context.watch<FollowerFormBloc>();
    final followerBloc = context.watch<FollowerBloc>();
    final supportsMovement = followerFormBloc.state.supportsMovement;
    final textTheme = Theme.of(context).textTheme;

    return FadeIn(
      child: Form(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: AppSpacing.md),
              // contexto: dónde queda y si el GPS ya está (dentro de la sección)
              Card(
                margin: EdgeInsets.zero,
                child: const Padding(
                  padding: EdgeInsets.all(AppSpacing.lg),
                  child: Column(
                    children: [
                      FollowerFormTarget(),
                      Padding(
                        padding: EdgeInsets.symmetric(vertical: AppSpacing.md),
                        child: Divider(height: 1),
                      ),
                      GpsStatusRow(),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: AppSpacing.lg),
              // push y no go: la ficha queda debajo (con lo capturado) y el
              // escáner tiene a dónde regresar
              ScanIneButton(onTap: () => context.push('/scanner')),
              const SizedBox(height: AppSpacing.xl),
              SupportsChoice(
                value: supportsMovement,
                onChanged: (v) => followerFormBloc.add(SupportsMovementChange(v)),
              ),
              const SizedBox(height: AppSpacing.xl),
              FormStep(
                number: 1,
                title: 'Domicilio',
                caption: 'Donde vive; queda en la sección del puesto',
                last: !supportsMovement,
                child: const FollowerFormAddress(),
              ),
              if (supportsMovement) ...[
                const FormStep(
                  number: 2,
                  title: 'Datos de la persona',
                  caption: 'Si escaneaste la INE ya vienen llenos',
                  child: FollowerFormPersonalInformation(),
                ),
                const FormStep(
                  number: 3,
                  title: 'Contacto',
                  caption: 'Para invitarle a las actividades',
                  child: FollowerFormContact(),
                ),
                const FormStep(
                  number: 4,
                  title: 'Encuesta',
                  caption: '¿Cómo quiere participar?',
                  last: true,
                  child: FollowerFormSurvey(),
                ),
              ],
              const SizedBox(height: AppSpacing.xl),
              FilledButton.icon(
                style: FilledButton.styleFrom(
                  minimumSize: const Size.fromHeight(56),
                  textStyle: textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800),
                ),
                onPressed: followerFormBloc.state.isValid
                    ? () async {
                        FocusManager.instance.primaryFocus?.unfocus();
                        final problem = await _checkLocation(followerFormBloc.state);
                        if (!context.mounted) return;
                        if (problem != null) {
                          CustomSnackbar.showSnackbar(
                              context, TypeSnackbar.error, problem);
                          return;
                        }
                        _openConfirmDialog(
                            context, followerBloc, followerFormBloc);
                      }
                    : null,
                icon: const Icon(Icons.check_circle_outline),
                label: const Text('Guardar convencido'),
              ),
              const SizedBox(height: AppSpacing.sm),
              Text(
                followerFormBloc.state.isValid
                    ? 'Se guarda en el teléfono y se envía al sincronizar'
                    : 'Completa los campos marcados para guardar',
                textAlign: TextAlign.center,
                style: textTheme.bodySmall
                    ?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
              ),
              const SizedBox(height: AppSpacing.xxl),
            ],
          ),
        ),
      ),
    );
  }

  /// La ubicación: tiene que haber GPS y, si se tiene el polígono de la
  /// sección, caer dentro o a no más de 100 m del borde (como valida el
  /// servidor). Sin polígono guardado se deja pasar: el servidor revisa.
  Future<String?> _checkLocation(FollowerFormState s) async {
    final lat = double.tryParse(s.latitude);
    final lng = double.tryParse(s.longitude);
    if (lat == null || lng == null) {
      return 'Espera a que el GPS tome la ubicación';
    }
    final section = int.tryParse(s.sectionNumber.value);
    if (section == null) {
      return null;
    }
    final geo = await CaptureCatalog().geometry(section);
    if (geo == null) {
      return null;
    }
    final outside = distanceOutsideSection(lat, lng, geo);
    if (outside != null && outside > sectionToleranceMeters) {
      return 'Estás a ${outside.round()} m fuera de la sección: captura en el domicilio, dentro de la sección';
    }
    return null;
  }

  Future<void> _openConfirmDialog(
    BuildContext context,
    FollowerBloc followerBloc,
    FollowerFormBloc followerFormBloc,
  ) {
    return showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('¿Estás seguro?'),
        content: const Text('Se guardará el convencido en este teléfono.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () async {
              final Follower follower =
                  await FollowerMapper.castToEntity(followerFormBloc.state);
              followerBloc.add(SaveFollower(follower: follower));
              EasyLoading.dismiss();
              if (dialogContext.mounted) {
                followerBloc.add(const LoadFollowers());
                followerFormBloc.add(const ResetForm());
                Navigator.pop(dialogContext);
              }
              if (context.mounted) {
                CustomSnackbar.showSnackbar(context, TypeSnackbar.success,
                    'Convencido agregado correctamente');
              }
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }
}
