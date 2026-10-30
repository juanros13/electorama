import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
import 'package:electorama_app/domain/entities/visits/visit_people.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/geo/geography_bloc.dart';
import 'package:electorama_app/presentation/blocs/coordinator_zone/coordinator_zone_form_bloc.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people/visits_people_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_dropdown_form_field.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';

class VisitForm extends StatefulWidget {
  const VisitForm({super.key});

  @override
  State<VisitForm> createState() => _VisitFormState();
}

class _VisitFormState extends State<VisitForm> {
  @override
  void initState() {
    context.read<GeographyBloc>().add(LoadSections(seccion: BlocProvider.of<AuthBloc>(context).state.user!.authId));
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final geographyBloc = context.watch<GeographyBloc>();
    final visitCordinatorZoneBloc = context.watch<CordinatorZoneFormBloc>();
    final visitBloc = context.watch<VisitsBloc>();
    final checkAuth = context.watch<AuthBloc>();
    print(checkAuth.state.user!.authIdReference);

    final section = visitCordinatorZoneBloc.state.sectionSelected;
    String municipalityId = "";
    final municipality = visitCordinatorZoneBloc.state.municipalitySelected;
    final locality = visitCordinatorZoneBloc.state.localitySelected;
    final String coordinateZoneId = checkAuth.state.user!.authId;


    if (geographyBloc.state.loading) {
      EasyLoading.show(
          status: 'Cargando',
          maskType: EasyLoadingMaskType.black,
          dismissOnTap: false);
    } else {
      EasyLoading.dismiss();
    }


    if (visitBloc.state.loading) {
      EasyLoading.show(
          status: 'Cargando',
          maskType: EasyLoadingMaskType.black,
          dismissOnTap: false);
    } else {
      EasyLoading.dismiss();
    }
    if (visitBloc.state.submitSync) {
      
      Future.delayed(Duration.zero, () {
        visitBloc.add(ChangeSubmitSync(value: false));
        context.read<VisitsPeopleBloc>().add(const LoadVisitPeople());
        CustomSnackbar.showSnackbar(context, TypeSnackbar.success, 'Tus visitas han sido sincronizadas correctamente.');
      });

    } 
    return BlocListener<VisitsPeopleBloc, VisitsPeopleState>(
      listener: (context, state) {
        final List<VisitPeople> visitsToAdd = [];
        if(visitBloc.state.loading ==false){
          for(final visit in state.listVisitPeople){
          if (!visit.syncVisit) {
              visitsToAdd.add(visit);
            }
          }
          BlocProvider.of<VisitsBloc>(context).add(ChangeListVisitedPeople(listVisitsPeople: visitsToAdd));
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
                  //selectedValue: selectedValue,
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
                    if (value != null) {
                      geographyBloc
                          .add(LoadMunicipality(seccionId: int.parse(value)));
                      geographyBloc
                          .add(LoadLocalities(seccionId: int.parse(value)));
                      visitCordinatorZoneBloc.add(SectionChange(value));
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
                            visitCordinatorZoneBloc
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
                        //selectedValue: geographyBloc.state.municipality!.nomMun,
                        iconData: Icons.maps_home_work,
                        dropdownItems: state.listLocalities.map((locality) {
                          return DropdownMenuItem<String>(
                            value: locality.id
                                .toString(), // Valor que se asigna al seleccionar esta opción
                            child: Text(locality
                                .localidad), // Texto que se muestra en la opción del Dropdown
                          );
                        }).toList(),
                        obscureText: false,
                        isPure: true,
                        onChange: (value) {
                          if (value != null) {
                            visitCordinatorZoneBloc.add(LocalityChange(value));
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
                const SizedBox(height: 10),
                Align(
                  alignment: Alignment.centerRight,
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 10),
                    child: FilledButton(
                      onPressed: (visitCordinatorZoneBloc.state.isValid && visitBloc.state.listVisitPeople.isNotEmpty )
                          ? () async {
                              context.read<VisitsBloc>().add(SendVisit(
                                section: section.value,
                                municipality: municipalityId,
                                locality: locality.value,
                                coordinateZoneId: coordinateZoneId,
                                referenceAuthId: checkAuth.state.user!.authIdReference,
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
    CustomSnackbar.showSnackbar(context, TypeSnackbar.success, 'Tus visitas han sido sincronizadas correctamente.');
  }
}
