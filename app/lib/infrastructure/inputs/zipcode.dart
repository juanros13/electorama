import 'package:formz/formz.dart';

enum ZipcodeError { length }

class Zipcode extends FormzInput<String, ZipcodeError> {
  const Zipcode.pure() : super.pure('');
  const Zipcode.dirty(String value) : super.dirty(value);

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == ZipcodeError.length) return 'Minimo 5 caracteres';
    return null;
  }

  @override
  ZipcodeError? validator(String value) {
    if (value.length < 5 && value.isNotEmpty) return ZipcodeError.length;
    return null;
  }
}
