import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people_form/visits_people_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/forms/visits/visit_people_extend_personal_form.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_switch_form_field.dart';
import 'package:electorama_app/presentation/widgets/widgets.dart';


class VisitsPeopleExtendForm extends StatelessWidget {
  const VisitsPeopleExtendForm({super.key});

  @override
  Widget build(BuildContext context) {
    final visitedPeopleFormBloc = context.watch<VisitPeopleFormBloc>();
    final age = visitedPeopleFormBloc.state.age;
    final sex = visitedPeopleFormBloc.state.sex;
    final hasSympathy = visitedPeopleFormBloc.state.hasSympathy;
    final acceptInformation = visitedPeopleFormBloc.state.acceptInformation;


    return Column(
      children: [
        RadioGroup<Sex>(
          groupValue: sex,
          onChanged: (value) {
            if (value != null) {
              visitedPeopleFormBloc.add(SexChange(value));
            }
          },
          child: Row(
            children: [
              Expanded(
                child: RadioListTile<Sex>(
                  title: const Text('Hombre'),
                  //subtitle: const Text('Viajar por carro'),
                  value: Sex.H,
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: RadioListTile<Sex>(
                  title: const Text('Mujer'),
                  //subtitle: const Text('Viajar por bote'),
                  value: Sex.M,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 10),
        CustomTextFormField(
          textInputType: TextInputType.number,
          onlyNumbers: true,
          iconData: Icons.numbers,
          label: 'Edad',
          erroMessage: age?.getErrorMessage,
          onChange: (value) {
            visitedPeopleFormBloc.add(AgeChange(value));
          },
          isPure: age !=null?age.isPure:true
        ),
        const Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: EdgeInsets.all(8.0),
              child: Text('¿Es afin?',style: TextStyle(fontSize: 20,),),
            ),
          ],
        ),
        RadioGroup<String>(
          groupValue: hasSympathy,
          onChanged: (value) {
            if (value != null) {
              visitedPeopleFormBloc.add(HasSympathyChange(value));
            }
          },
          child: Row(
            children: [
              Expanded(
                child: RadioListTile<String>(
                  title: const Text('Si'),
                  value: 'Si',
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: RadioListTile<String>(
                  title: const Text('No'),
                  value: 'No',
                ),
              ),
            ],
          ),
        ),
        const Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: EdgeInsets.all(8.0),
              child: Text('Explicar motivo de la visita', style: TextStyle(fontSize: 20,fontWeight: FontWeight.bold)),
            ),
          ],
        ),
        CustomSwitchFormField(
          value: acceptInformation,
          onChange: (value) {
            visitedPeopleFormBloc.add(AccepInformationChange(value));
          },
          label: '¿La persona acepta tener mas información?'
        ),
        Visibility(
          visible: acceptInformation,
          child: const VisitsPeopleExtendPersonalForm()
        ),
      ],
    );
  }
}
