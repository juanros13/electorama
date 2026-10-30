import 'package:formz/formz.dart';

// Define input validation errors
enum PersonalNameError { empty, length }

// Extend FormzInput and provide the input type and error type.
class PersonalName extends FormzInput<String, PersonalNameError> {
  // Call super.pure to represent an unmodified form input.
  const PersonalName.pure() : super.pure('');

  // Call super.dirty to represent a modified form input.
  const PersonalName.dirty(String value) : super.dirty(value);

  String? get getErrorMessage{
    if(isValid || isPure) return null;
    if(displayError == PersonalNameError.empty) return 'El campo es requerido';
    if(displayError == PersonalNameError.length) return 'Minimo 2 caracteres';
    return null;

  }

  // Override validator to handle validating a given input value.
  @override
  PersonalNameError? validator(String value) {
    if(value.isEmpty || value.trim().isEmpty) return PersonalNameError.empty;
    if(value.length <2) return PersonalNameError.length;


    return null;
  }
}