import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

enum TypesTextInputs { text, password }

class CustomTextFormField extends StatelessWidget {
  final String? label;
  final String? hint;
  final String? erroMessage;
  final Function(String)? onChange;
  final String? Function(String?)? validator;
  final bool obscureText;
  final IconData? iconData;
  final bool isPure;
  final TextInputType? textInputType;
  final bool onlyNumbers;
  final bool onlyUppercase;
  final bool isRequired;
  final bool readOnly;
  final bool enabled;
  final TextEditingController? textEditingController;
  final String inputValue;
  final int? maxLength;

  const CustomTextFormField({
    super.key,
    this.label,
    this.hint,
    this.erroMessage,
    this.onChange,
    this.validator,
    this.obscureText = false,
    this.iconData,
    this.isPure = true,
    this.textInputType = TextInputType.text,
    this.onlyNumbers = false,
    this.onlyUppercase = false,
    this.isRequired = false,
    this.readOnly = false,
    this.enabled = true,
    this.textEditingController,
    this.inputValue = '',
    this.maxLength,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    final formatters = <TextInputFormatter>[
      if (onlyNumbers) FilteringTextInputFormatter.digitsOnly,
      if (onlyUppercase) _UpperCaseTextFormatter(),
    ];

    return TextFormField(
      initialValue: inputValue,
      keyboardType: textInputType,
      inputFormatters: formatters.isEmpty ? null : formatters,
      onChanged: onChange,
      maxLength: maxLength,
      validator: validator,
      obscureText: obscureText,
      readOnly: readOnly,
      enabled: enabled,
      controller: textEditingController,
      decoration: InputDecoration(
        label: label != null
            ? Text(isRequired ? '$label *' : label!)
            : null,
        hintText: hint,
        counterText: '',
        prefixIcon: iconData != null ? Icon(iconData) : null,
        errorText: erroMessage,
        suffixIcon: isPure
            ? null
            : (erroMessage != null)
                ? FadeIn(child: Icon(Icons.close, color: colors.error))
                : FadeIn(child: Icon(Icons.check, color: colors.tertiary)),
      ),
    );
  }
}

class _UpperCaseTextFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
      TextEditingValue oldValue, TextEditingValue newValue) {
    return TextEditingValue(
      text: newValue.text.toUpperCase(),
      selection: newValue.selection,
    );
  }
}
