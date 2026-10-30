import 'package:formz/formz.dart';

enum IneError { empty, length, invalidIne }

class IneText extends FormzInput<String, IneError> {
  final bool isRequired;
  const IneText.pure({required this.isRequired}) : super.pure('');
  const IneText.dirty(String value, {required this.isRequired}) : super.dirty(value);

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == IneError.empty && isRequired) return 'El campo es requerido';
    if (displayError == IneError.length) return 'Minimo 18 caracteres';
    if (displayError == IneError.invalidIne) return 'Clave de elector no valido';
    return null;
  }

  @override
  IneError? validator(String value) {
    if ((value.isEmpty || value.trim().isEmpty) && isRequired) return IneError.empty;
    if (value.length != 18 && value.isNotEmpty) return IneError.length;
    final ineRegex = RegExp(r'[A-Z]{6}[0-9]{8}[A-Z]{1}[0-9]{3}');
    if (!ineRegex.hasMatch(value) && value.isNotEmpty) return IneError.invalidIne;
    return null;
  }
}
