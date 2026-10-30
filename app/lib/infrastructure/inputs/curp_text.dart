import 'package:formz/formz.dart';

enum CurpError { empty, length, invalidCURP }

class CurpText extends FormzInput<String, CurpError> {
  final bool isRequired;
  const CurpText.pure({required this.isRequired}) : super.pure('');
  const CurpText.dirty(super.value, {required this.isRequired}) : super.dirty();

  String? get getErrorMessage {
    if (isValid || isPure) return null;
    if (displayError == CurpError.empty && isRequired) return 'El campo es requerido';
    if (displayError == CurpError.length) return 'Minimo 18 caracteres';
    if (displayError == CurpError.invalidCURP) return 'CURP no valido';
    return null;
  }

  @override
  CurpError? validator(String value) {
    if ((value.isEmpty || value.trim().isEmpty) && isRequired) return CurpError.empty;
    if (value.length != 18 && value.isNotEmpty) return CurpError.length;
    final curpRegex = RegExp(
        r'^([A-Z][AEIOUX][A-Z]{2}\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])[HM](?:AS|B[CS]|C[CLMSH]|D[FG]|G[TR]|HG|JC|M[CNS]|N[ETL]|OC|PL|Q[TR]|S[PLR]|T[CSL]|VZ|YN|ZS)[B-DF-HJ-NP-TV-Z]{3}[A-Z\d])(\d)$');
    if (!curpRegex.hasMatch(value) && value.isNotEmpty) return CurpError.invalidCURP;
    return null;
  }
}
