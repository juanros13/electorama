import 'package:formz/formz.dart';

enum StreetNumberError { empty, length }

class StreetNumber extends FormzInput<String, StreetNumberError> {
  final bool isRequired;
  const StreetNumber.pure({this.isRequired = true}) : super.pure('');
  const StreetNumber.dirty(String value, {this.isRequired = true}) : super.dirty(value);

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == StreetNumberError.empty && isRequired) return 'El campo es requerido';
    return null;
  }

  @override
  StreetNumberError? validator(String value) {
    if ((value.isEmpty || value.trim().isEmpty) && isRequired) return StreetNumberError.empty;
    return null;
  }
}
