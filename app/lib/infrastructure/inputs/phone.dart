import 'package:formz/formz.dart';

enum PhoneError { empty, length }

class Phone extends FormzInput<String, PhoneError> {
  final bool isRequired;
  const Phone.pure({this.isRequired = true}) : super.pure('');
  const Phone.dirty(String value, {this.isRequired = true}) : super.dirty(value);

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == PhoneError.empty && isRequired) return 'El campo es requerido';
    if (displayError == PhoneError.length) return 'Los telefonos deben contener 10 digitos';
    return null;
  }

  @override
  PhoneError? validator(String value) {
    if ((value.isEmpty || value.trim().isEmpty) && isRequired) return PhoneError.empty;
    if (value.length != 10 && value.isNotEmpty) return PhoneError.length;
    return null;
  }
}
