import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people_form/visits_people_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/widgets.dart';


class VisitsPeopleExtendPersonalForm extends StatelessWidget {
  const VisitsPeopleExtendPersonalForm({super.key});

  @override
  Widget build(BuildContext context) {
    final visitedPeopleFormBloc = context.watch<VisitPeopleFormBloc>();
    final names = visitedPeopleFormBloc.state.names;
    final lastName = visitedPeopleFormBloc.state.lastName;
    final surName = visitedPeopleFormBloc.state.surName;
    final phone = visitedPeopleFormBloc.state.phone;


    return Column(
      children: [
        const SizedBox(height: 10,),
        CustomTextFormField(
          iconData: Icons.person,
          label: 'Nombres',
          erroMessage: names?.getErrorMessage,
          onChange: (value) {
            visitedPeopleFormBloc.add(NamesChange(value));
          },
          isPure: names !=null?names.isPure:true
        ),
        const SizedBox(height: 10),
        CustomTextFormField(
          iconData: Icons.person_3,
          label: 'Apellido paterno',
          erroMessage: lastName?.getErrorMessage,
          onChange: (value) {
            visitedPeopleFormBloc.add(LastNameChange(value));
          },
          isPure: lastName !=null?lastName.isPure:true
        ),
        const SizedBox(height: 10),
        CustomTextFormField(
          iconData: Icons.person_2,
          label: 'Apellido marterno',
          erroMessage: surName?.getErrorMessage,
          onChange: (value) {
            visitedPeopleFormBloc.add(SurNameChange(value));
          },
          isPure: surName !=null?surName.isPure:true
        ),
        const SizedBox(height: 10),
        CustomTextFormField(
          textInputType: TextInputType.number,
          onlyNumbers: true,
          maxLength: 10,
          iconData: Icons.phone_iphone,
          label: 'Telefono',
          erroMessage: phone?.getErrorMessage,
          onChange: (value) {
            visitedPeopleFormBloc.add(PhoneChange(value));
          },
          isPure: phone !=null?phone.isPure:true
        ),
      ],
    );
  }
}
