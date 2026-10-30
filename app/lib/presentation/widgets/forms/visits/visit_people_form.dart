import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/domain/entities/visits/visit_people.dart';
import 'package:electorama_app/infrastructure/mappers/visits/visit_people_mapper.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people/visits_people_bloc.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people_form/visits_people_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/forms/visits/visit_people_extend_form.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_switch_form_field.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';
import 'package:electorama_app/presentation/widgets/widgets.dart';

class VisitsPeopleForm extends StatelessWidget {
  const VisitsPeopleForm({super.key});


  @override
  Widget build(BuildContext context) {
    final visitedPeopleFormBloc = context.watch<VisitPeopleFormBloc>();
    final visitedPeopleBloc = context.watch<VisitsPeopleBloc>();
    print(visitedPeopleFormBloc.state);
    
    final street = visitedPeopleFormBloc.state.street;
    final externalNumber = visitedPeopleFormBloc.state.externalNumber;
    final openTheDoor = visitedPeopleFormBloc.state.openTheDoor;

    final completeSubmit = visitedPeopleFormBloc.state.completeSubmit;
    if(visitedPeopleBloc.state.add){
      Future.delayed(Duration.zero, () {
        context.read<VisitsPeopleBloc>()
              ..add(InitVisitedPeople())
              ..add(const LoadVisitPeople());
        CustomSnackbar.showSnackbar(context,TypeSnackbar.success, 'Visita agregada correctamente');
      });
    }
    if(visitedPeopleBloc.state.error!= ''){
      Future.delayed(Duration.zero, () {
        context.read<VisitsPeopleBloc>()
              .add(InitVisitedPeople());
        CustomSnackbar.showSnackbar(context,TypeSnackbar.error,visitedPeopleBloc.state.error);
      });
    }
    return Visibility(
      visible: !completeSubmit,
      child: FadeIn(
        child: Form(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10),
            child: Column(
            children: [
              const SizedBox(height: 5),
              const CardGeo(),
              const SizedBox(height: 5),
              
              CustomTextFormField(
                  // textEditingController: streetText,
                  inputValue: street.value,
                  iconData: Icons.streetview,
                  label: 'Calle',
                  erroMessage: street.getErrorMessage,
                  onChange: (value) {
                    visitedPeopleFormBloc.add(StreetChange(value));
                    return street.value;
                  },
                  isPure: street.isPure),
              const SizedBox(height: 10),
              CustomTextFormField(
                  //textEditingController: TextEditingController(text: externalNumber.value),
                  inputValue: externalNumber.value,
                  iconData: Icons.numbers,
                  label: 'Numero exterior',
                  erroMessage: externalNumber.getErrorMessage,
                  onChange: (value) {
                    visitedPeopleFormBloc.add(ExternalNumberChange(value));
                    return externalNumber.value;
                  },
                  isPure: externalNumber.isPure),
              const SizedBox(height: 10),
              CustomSwitchFormField(
                  value: openTheDoor,
                  onChange: (value) {
                    visitedPeopleFormBloc.add(OpenTheDoorChange(value));
                    return street.value;
                  },
                  label: '¿Abrieron la puerta?'
              ),
              Visibility(
                visible: openTheDoor,
                child: const VisitsPeopleExtendForm()
              ),
              const SizedBox(height: 15),
              FilledButton.icon(
                  style: FilledButton.styleFrom(
                    minimumSize: const Size.fromHeight(50),
                  ),
                  onPressed: visitedPeopleFormBloc.state.isValid
                      ? () async {
                          FocusManager.instance.primaryFocus?.unfocus();
                          _addVisitedPeopleDialog(
                            context, 
                            visitedPeopleBloc, 
                            visitedPeopleFormBloc
                          );
                        }
                      : null,
                  icon: const Icon(Icons.save),
                  label: const Text('Guardar')
              ),
              const SizedBox(height: 50),
            ],
          ),
          )
        ),
      ),
    );
  }

  Future<String?> _addVisitedPeopleDialog(
    BuildContext context,
    VisitsPeopleBloc visitedPeopleBloc,
    VisitPeopleFormBloc visitPeopleFormBloc,
  ) {
    return showDialog<String>(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        title: const Text('¿Estás seguro?'),
        content: const Text('Se guardará la visita en este teléfono.'),
        actions: <Widget>[
          TextButton(
            onPressed: () {
              Navigator.pop(context, 'Cancelar');
            },
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () async {
              final VisitPeople visitPeople =
                  await VisitPeopleMapper.castToEntity(visitPeopleFormBloc.state);
              visitPeopleFormBloc.add(const SubmitVisitedPeople());
              visitedPeopleBloc.add(AddVisitPeople(visitPeople: visitPeople));
              if (context.mounted) Navigator.pop(context, 'Guardar');
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }
}
