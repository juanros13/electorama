import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:formz/formz.dart';
import 'package:electorama_app/infrastructure/inputs/age.dart';
import 'package:electorama_app/infrastructure/inputs/general_text.dart';
import 'package:electorama_app/infrastructure/inputs/street_number.dart';

import 'package:electorama_app/infrastructure/inputs/personal_name.dart';
import 'package:electorama_app/infrastructure/inputs/phone.dart';

part 'visits_people_form_event.dart';
part 'visits_people_form_state.dart';

class VisitPeopleFormBloc extends Bloc<VisitPeopleEvent, VisitPeopleFormState> {
  

  VisitPeopleFormBloc() : super(VisitPeopleInitial()) {
    on<StreetChange>(_onStreetChange);
    on<ExternalNumberChange>(_onExternalNumberChange);
    on<OpenTheDoorChange>(_onOpenTheDoorChange);
    on<LatitudeChange>(_latitudeChange);
    on<LongitudeChange>(_longitudeChange);
    on<AgeChange>(_ageChange);
    on<SexChange>(_sexChange);
    on<HasSympathyChange>(_hasSympathyChange);
    on<NamesChange>(_namesChange);
    on<LastNameChange>(_lastNameChange);
    on<SurNameChange>(_surNameChange);
    on<AccepInformationChange>(_acceptInformationChange);
    on<PhoneChange>(_phoneChange);
    on<CompleteSubmitChange>(_completeSubmitChange);
    on<SubmitVisitedPeople>((_, emit) => _onSubmit(emit),);
    
  }
  void _onSubmit(Emitter<VisitPeopleFormState> emit){
    emit(state.copyWith(
      //street: const GeneralText.pure(),
      //externalNumber: const StreetNumber.pure(),
      formStatus: FormStatus.validating,
      openTheDoor: false,
      latitude: state.latitude,
      longitude: state.longitude,
      isValid: true,
      age: const Age.pure(),
      sex: Sex.H,
      acceptInformation: false,
      hasSympathy: 'Si',
      names: const PersonalName.pure(),
      lastName: const PersonalName.pure(),
      surName: const PersonalName.pure(),
      phone: const Phone.pure(),
    ));
    //add(const StreetChange(''));
    // add(const ExternalNumberChange(''));
    // print('Submit: $state');
  }
  void _onStreetChange(StreetChange event, Emitter<VisitPeopleFormState> emit){
    final street = GeneralText.dirty(event.value);
    emit(state.copyWith(
      street: street,
      isValid: validateForm(streetStateFlag: street)
    ));
  }
  void _onExternalNumberChange(ExternalNumberChange event, Emitter<VisitPeopleFormState> emit){
    final externalNumber = StreetNumber.dirty(event.value);
    emit(state.copyWith(
      externalNumber: externalNumber,
      isValid: validateForm(externalNumberStateFlag: externalNumber)
    ));
  }
  void _onOpenTheDoorChange(OpenTheDoorChange event, Emitter<VisitPeopleFormState> emit){
    if(state.openTheDoor){
      emit(state.copyWith(
        openTheDoor: !state.openTheDoor,
        isValid: validateForm(openTheDoorStateFlag: !state.openTheDoor),
        age: const Age.pure(),
      ));
    }else{
      emit(state.copyWith(
        openTheDoor: !state.openTheDoor,
        isValid: validateForm(openTheDoorStateFlag: !state.openTheDoor),
      ));
    }
    
  }
  void _acceptInformationChange(AccepInformationChange event, Emitter<VisitPeopleFormState> emit){
    if(state.acceptInformation){
      emit(state.copyWith(
        acceptInformation: !state.acceptInformation,
        isValid: validateForm(acceptAditionalInformationStateFlag: !state.acceptInformation),
        names: const PersonalName.pure(),
        lastName: const PersonalName.pure(),
        surName: const PersonalName.pure(),
        phone: const Phone.pure()
      ));
    }else{
      emit(state.copyWith(
        acceptInformation: !state.acceptInformation,
        isValid: validateForm(acceptAditionalInformationStateFlag: !state.acceptInformation),
      ));
    }
  }
  void _latitudeChange(LatitudeChange event, Emitter<VisitPeopleFormState> emit){
    emit(state.copyWith(
      latitude: event.value,
    ));
  }
  void _longitudeChange(LongitudeChange event, Emitter<VisitPeopleFormState> emit){
    emit(state.copyWith(
      longitude: event.value,
    ));
  }
  void _ageChange(AgeChange event, Emitter<VisitPeopleFormState> emit){
    final age = Age.dirty(event.value);
    emit(state.copyWith(
      age: age,
      isValid: validateForm(ageStateFlag: age)
    ));
  }
  void _namesChange(NamesChange event, Emitter<VisitPeopleFormState> emit){
    final names = PersonalName.dirty(event.value);
    emit(state.copyWith(
      names: names,
      isValid: validateForm(namesStateFlag: names)
    ));
  }
  void _lastNameChange(LastNameChange event, Emitter<VisitPeopleFormState> emit){
    final lastName = PersonalName.dirty(event.value);
    emit(state.copyWith(
      lastName: lastName,
      isValid: validateForm(lastNameStateFlag: lastName)
    ));
  }
  void _surNameChange(SurNameChange event, Emitter<VisitPeopleFormState> emit){
    final surName = PersonalName.dirty(event.value);
    emit(state.copyWith(
      surName: surName,
      isValid: validateForm(namesStateFlag: surName)
    ));
  }
  
  void _sexChange(SexChange event, Emitter<VisitPeopleFormState> emit){
    emit(state.copyWith(
      sex: event.value,
    ));
  }
  void _phoneChange(PhoneChange event, Emitter<VisitPeopleFormState> emit){
    final phone = Phone.dirty(event.value);

    emit(state.copyWith(
      phone: phone,
      isValid: validateForm(phoneStateFlag: phone)
    ));
  }
  void _hasSympathyChange(HasSympathyChange event, Emitter<VisitPeopleFormState> emit){
    // if(event.value == 'No') {
    //   // print("Es simpatizante");
    //   emit(state.copyWith(
    //     hasSympathy: event.value,
    //     acceptInformation: false,
    //     isValid: validateForm(acceptAditionalInformationStateFlag: !state.acceptInformation),
    //     age: const Age.pure(),
    //     names: const PersonalName.pure(),
    //     lastName: const PersonalName.pure(),
    //     surName: const PersonalName.pure(),
    //     phone: const Phone.pure()
    //   ));
    // } else {
    //   emit(state.copyWith(
    //     hasSympathy: event.value,
    //     acceptInformation: !state.acceptInformation,
    //     isValid: validateForm(acceptAditionalInformationStateFlag: !state.acceptInformation),
    //   ));
    // }

    emit(state.copyWith(
      hasSympathy: event.value,
    ));
  }
  void _completeSubmitChange(CompleteSubmitChange event, Emitter<VisitPeopleFormState> emit){
    emit(state.copyWith(
      completeSubmit: event.value,
    ));
  }
  
  bool validateForm({
   String? hasSympathy,
   bool? openTheDoorStateFlag,
   bool? acceptAditionalInformationStateFlag,
   StreetNumber? externalNumberStateFlag,
   GeneralText? streetStateFlag,
   Age? ageStateFlag,
   PersonalName? namesStateFlag,
   PersonalName? lastNameStateFlag,
   PersonalName? surNameStateFlag,
   Phone? phoneStateFlag
  }){
    hasSympathy = (hasSympathy == null)?state.hasSympathy:hasSympathy;
    openTheDoorStateFlag = (openTheDoorStateFlag ==null)?state.openTheDoor:openTheDoorStateFlag;
    acceptAditionalInformationStateFlag = (acceptAditionalInformationStateFlag ==null)?state.acceptInformation:acceptAditionalInformationStateFlag;
    externalNumberStateFlag = (externalNumberStateFlag ==null)?state.externalNumber:externalNumberStateFlag;
    streetStateFlag = (streetStateFlag ==null)?state.street:streetStateFlag;
    ageStateFlag = (ageStateFlag ==null)?state.age:ageStateFlag;
    namesStateFlag = (namesStateFlag ==null)?state.names:namesStateFlag;
    lastNameStateFlag = (lastNameStateFlag ==null)?state.lastName:lastNameStateFlag;
    surNameStateFlag = (surNameStateFlag ==null)?state.surName:surNameStateFlag;
    phoneStateFlag = (phoneStateFlag ==null)?state.phone:phoneStateFlag;

    if(!openTheDoorStateFlag){
      return Formz.validate([externalNumberStateFlag, streetStateFlag]);
    }else{
      if(ageStateFlag !=null  ){
          //return Formz.validate([state.externalNumber, state.street, state.age!]);
        
        // if(hasSympathy == 'No') {
        //   return Formz.validate([externalNumberStateFlag, streetStateFlag, ageStateFlag]);
        // }
        // else if(hasSympathy == 'Si') {
        //   return Formz.validate([externalNumberStateFlag, streetStateFlag, ageStateFlag,namesStateFlag!,lastNameStateFlag!,surNameStateFlag!, phoneStateFlag!]);
        // }
      
        if(!acceptAditionalInformationStateFlag){
            return Formz.validate([externalNumberStateFlag, streetStateFlag, ageStateFlag]);
        }else{
          if(namesStateFlag !=null  ){
            return Formz.validate([externalNumberStateFlag, streetStateFlag, ageStateFlag,namesStateFlag,lastNameStateFlag!,surNameStateFlag!, phoneStateFlag!]);
          }
        }

        
      }
      return false;    
    }
  }

}



