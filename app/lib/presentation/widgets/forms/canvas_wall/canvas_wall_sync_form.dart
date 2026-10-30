import 'package:flutter/material.dart';
import 'package:animate_do/animate_do.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';

import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/geo/geography_bloc.dart';
import 'package:electorama_app/presentation/blocs/coordinator_zone/coordinator_zone_form_bloc.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall/canvas_wall_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_dropdown_form_field.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';

class CanvasWallSyncForm extends StatefulWidget {
  const CanvasWallSyncForm({super.key});

  @override
  State<CanvasWallSyncForm> createState() => _CanvasWallSyncFormState();
}

class _CanvasWallSyncFormState extends State<CanvasWallSyncForm> {
  @override
  void initState() {
    context.read<GeographyBloc>().add(LoadSections(seccion: BlocProvider.of<AuthBloc>(context).state.user!.authId));
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final geographyBloc = context.watch<GeographyBloc>();
    final coordinatorZoneBloc = context.watch<CordinatorZoneFormBloc>();
    final canvasWallServiceBloc = context.watch<CanvasWallServiceBloc>();
    final checkAuth = context.watch<AuthBloc>();

    final section = coordinatorZoneBloc.state.sectionSelected;
    String municipalityId = "";
    final municipality = coordinatorZoneBloc.state.municipalitySelected;
    final locality = coordinatorZoneBloc.state.localitySelected;
    final String coordinateZoneId = checkAuth.state.user!.authId;


    if (geographyBloc.state.loading) {
      EasyLoading.show(
          status: 'Cargando',
          maskType: EasyLoadingMaskType.black,
          dismissOnTap: false);
    } else {
      EasyLoading.dismiss();
    }

    if (canvasWallServiceBloc.state.loading) {
      EasyLoading.show(
          status: 'Cargando',
          maskType: EasyLoadingMaskType.black,
          dismissOnTap: false);
    } else {
      EasyLoading.dismiss();
    }
    if (canvasWallServiceBloc.state.submitSync) {
      if(canvasWallServiceBloc.state.canvasWallSync > 0) {
        final canvasWallSync = canvasWallServiceBloc.state.canvasWallSync;
        Future.delayed(Duration.zero, () {
          canvasWallServiceBloc.add(ChangeSubmitCanvasWallSync(value: false));
          context.read<CanvasWallBloc>().add(const LoadCanvasWall());
          CustomSnackbar.showSnackbar(
            context, 
            TypeSnackbar.success,
            '$canvasWallSync lonas o bardas han sido sincronizadas correctamente.'
          );
        });
      }

      if(canvasWallServiceBloc.state.canvasWallError > 0) {
        final canvasWallError = canvasWallServiceBloc.state.canvasWallError;
        Future.delayed(Duration.zero, () {
          canvasWallServiceBloc.add(ChangeSubmitCanvasWallSync(value: false));
          context.read<CanvasWallBloc>().add(const LoadCanvasWall());
          CustomSnackbar.showSnackbar(
            context, 
            TypeSnackbar.error,
            'UPPS! $canvasWallError lonas o bardas no han sido sincronizadas correctamente.'
          );
        });
      }
    } 

    return BlocListener<CanvasWallBloc, CanvasWallState>(
      listener: (context, state) {
        final List<CanvasWall> canvasWallToAdd = [];
        if(canvasWallServiceBloc.state.loading ==false) {
          for(final canvasWall in state.listCanvasWall) {
            if (!canvasWall.isInSync) {
              canvasWallToAdd.add(canvasWall);
            }
          }
          BlocProvider.of<CanvasWallServiceBloc>(context).add(ChangeListCanvasWall(listCanvasWall: canvasWallToAdd));
        }
      },
      child: FadeIn(child: BlocBuilder<GeographyBloc, GeographyState>(
        builder: (context, state) {
          return Form(
              child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10),
            child: Column(
              children: [
                const SizedBox(height: 5),
                CustomDropdownFormField(
                  selectedValue: coordinatorZoneBloc.state.sectionSelected.value,
                  iconData: Icons.map,
                  dropdownItems: state.listSections
                      .map((section) => DropdownMenuItem<String>(
                            value: section.seccionGeoId
                                .toString(), // Valor que se asigna al seleccionar esta opción
                            child: Text(section
                                .seccionNumber), // Texto que se muestra en la opción del Dropdown
                          ))
                      .toList(),
                  obscureText: false,
                  isPure: true,
                  onChange: (value) {
                    print(value);
                    if (value != "") {
                      geographyBloc
                          .add(LoadMunicipality(seccionId: int.parse(value!)));
                      geographyBloc
                          .add(LoadLocalities(seccionId: int.parse(value)));
                      coordinatorZoneBloc.add(SectionChange(value));
                      return section.value;
                    }
                  },
                ),
                const SizedBox(
                  height: 10,
                ),
                //Text(geographyBloc.state.municipality != null? geographyBloc.state.municipality!.nomMun:'No se encontro'),
                (geographyBloc.state.municipality != null)
                    ? CustomDropdownFormField(
                        iconData: Icons.location_on,
                        selectedValue: geographyBloc.state.municipality!.nomMun,
                        dropdownItems: <String>[
                          geographyBloc.state.municipality!.nomMun
                        ].map<DropdownMenuItem<String>>((String value) {

                          municipalityId = geographyBloc.state.municipality!.municipioId.toString();

                          return DropdownMenuItem<String>(
                            value: value,
                            child: Text(
                              value,
                            ),
                          );
                        }).toList(),
                        obscureText: false,
                        isPure: true,
                        disabled: true,
                        onChange: (value) {
                          if (value != null) {
                            coordinatorZoneBloc
                                .add(MunicipalityChange(value));
                            return municipality.value;
                          }
                        },
                      )
                    : CustomDropdownFormField(
                        iconData: Icons.location_on,
                        selectedValue: ' -- Sin municipio -- ',
                        dropdownItems: <String>[' -- Sin municipio -- ']
                            .map<DropdownMenuItem<String>>((String value) {
                          return DropdownMenuItem<String>(
                            value: value,
                            child: Text(
                              value,
                            ),
                          );
                        }).toList(),
                        obscureText: false,
                        isPure: true,
                        disabled: true,
                        onChange: (value) {},
                      ),
                const SizedBox(
                  height: 10,
                ),
                (geographyBloc.state.listLocalities.isNotEmpty)
                    ? CustomDropdownFormField(
                        selectedValue: locality.value,
                        iconData: Icons.maps_home_work,
                        dropdownItems: state.listLocalities.map((locality) {
                          return DropdownMenuItem<String>(
                            value: locality.id.toString(), // Valor que se asigna al seleccionar esta opción
                            child: Text(locality.localidad), // Texto que se muestra en la opción del Dropdown
                          );
                        }).toList(),
                        obscureText: false,
                        isPure: true,
                        onChange: (value) {
                          if (value != null) {
                            coordinatorZoneBloc.add(LocalityChange(value));
                            return locality.value;
                          }
                        },
                      )
                    : CustomDropdownFormField(
                        selectedValue: ' -- Sin localidad -- ',
                        iconData: Icons.maps_home_work,
                        dropdownItems: <String>[' -- Sin localidad -- ']
                            .map<DropdownMenuItem<String>>((String value) {
                          return DropdownMenuItem<String>(
                            value: value,
                            child: Text(value,),
                          );
                        }).toList(),
                        obscureText: false,
                        isPure: true,
                        disabled: true,
                        onChange: (value) {},
                      ),
                const SizedBox(
                  height: 10,
                ),
                const SizedBox(height: 10),
                Align(
                  alignment: Alignment.centerRight,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 10),
                    child: FilledButton(
                      onPressed: (coordinatorZoneBloc.state.isValid && canvasWallServiceBloc.state.listCanvasWall.isNotEmpty )
                          ? () async {
                            
                              context.read<CanvasWallServiceBloc>().add(SentCanvasWallService(
                                section: section.value, 
                                municipality: municipalityId, 
                                locality: locality.value, 
                                coordinateZoneId: coordinateZoneId, 
                                referenceAuthId: checkAuth.state.user!.authIdReference
                              ));
                              
                            }
                          : null,
                      child: const Text('Iniciar Sincronización'),
                    ),
                  ),
                ),
              ],
            ),
          ));
        },
      )),
    );
  }

  showNotification() {
    CustomSnackbar.showSnackbar(context, TypeSnackbar.success, 'Tus lonas y bardas han sido sincronizadas correctamente.');
  }
}
