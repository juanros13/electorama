import 'package:formz/formz.dart';

// Define input validation errors
enum GeneralDropdownError { empty }

// Extend FormzInput and provide the input type and error type.
class GeneralDropdown extends FormzInput<String, GeneralDropdownError> {
  // Call super.pure to represent an unmodified form input.
  const GeneralDropdown.pure() : super.pure('');

  // Call super.dirty to represent a modified form input.
  const GeneralDropdown.dirty(String value) : super.dirty(value);

  String? get getErrorMessage{
    if(isValid || isPure) return null;
    if(displayError == GeneralDropdownError.empty) return 'El campo es requerido';
    return null;

  }

  // Override validator to handle validating a given input value.
  @override
  GeneralDropdownError? validator(String value) {
    if(value.isEmpty || value.trim().isEmpty) return GeneralDropdownError.empty;

    return null;
  }
}