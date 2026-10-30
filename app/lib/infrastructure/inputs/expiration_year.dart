import 'package:formz/formz.dart';

enum ExpirationError { empty, length, invalid }

class ExpirationYear extends FormzInput<String, ExpirationError> {
  final bool isRequired;
  const ExpirationYear.pure({required this.isRequired}) : super.pure('');
  const ExpirationYear.dirty(super.value, {required this.isRequired}) : super.dirty();

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == ExpirationError.empty && isRequired) return 'El campo es requerido';
    if (displayError == ExpirationError.length) return 'El año debe tener formato de 4 digitos';
    if (displayError == ExpirationError.invalid) return 'No es un año valido';
    return null;
  }

  @override
  ExpirationError? validator(String value) {
    final year = int.tryParse(value);
    if ((value.isEmpty || value.trim().isEmpty) && isRequired) return ExpirationError.empty;
    if (value.length < 4 && value.isNotEmpty) return ExpirationError.length;
    if (year != null && year <= 2000) return ExpirationError.invalid;
    return null;
  }
}
