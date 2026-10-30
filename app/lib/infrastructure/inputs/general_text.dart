import 'package:formz/formz.dart';

enum GeneralTextError { empty, length }

class GeneralText extends FormzInput<String, GeneralTextError> {
  final bool isRequired;
  const GeneralText.pure({this.isRequired = true}) : super.pure('');
  const GeneralText.dirty(String value, {this.isRequired = true}) : super.dirty(value);

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == GeneralTextError.empty && isRequired) return 'El campo es requerido';
    if (displayError == GeneralTextError.length) return 'Minimo 3 caracteres';
    return null;
  }

  @override
  GeneralTextError? validator(String value) {
    if (isRequired && (value.isEmpty || value.trim().isEmpty)) return GeneralTextError.empty;
    if (value.length < 3 && value.isNotEmpty) return GeneralTextError.length;
    return null;
  }
}
