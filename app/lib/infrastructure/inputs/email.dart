import 'package:formz/formz.dart';

enum EmailError { empty, format }

class Email extends FormzInput<String, EmailError> {
  static final RegExp emailRegExp = RegExp(
    r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$',
  );
  final bool isRequired;
  const Email.pure({this.isRequired = true}) : super.pure('');
  const Email.dirty(super.value, {this.isRequired = true}) : super.dirty();

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == EmailError.empty && isRequired) return 'El campo es requerido';
    if (displayError == EmailError.format) return 'No tiene formato de correo electronico';
    return null;
  }

  @override
  EmailError? validator(String value) {
    if ((value.isEmpty || value.trim().isEmpty) && isRequired) return EmailError.empty;
    if (value.isNotEmpty && !emailRegExp.hasMatch(value)) return EmailError.format;
    return null;
  }
}
