import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';

class CustomDropdownFormField extends StatelessWidget {
  final String? selectedValue;
  final List<DropdownMenuItem<String>> dropdownItems;
  final Function(String?)? onChange;
  final bool obscureText;
  final IconData? iconData;
  final bool isPure;
  final String? label;
  final String? hint;
  final String? erroMessage;
  final String? Function(String?)? validator;
  final bool? disabled;
  final bool isRequired;

  const CustomDropdownFormField({
    super.key,
    required this.dropdownItems,
    this.selectedValue = '',
    this.onChange,
    required this.obscureText,
    this.iconData,
    required this.isPure,
    this.label,
    this.hint,
    this.erroMessage,
    this.validator,
    this.disabled = false,
    this.isRequired = false,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return DropdownButtonFormField(
      decoration: InputDecoration(
        label: label != null
            ? Text(isRequired ? '$label *' : label!)
            : null,
        prefixIcon: iconData != null ? Icon(iconData) : null,
        errorText: erroMessage,
        suffixIcon: isPure
            ? null
            : (erroMessage != null)
                ? FadeIn(child: Icon(Icons.close, color: colors.error))
                : FadeIn(child: Icon(Icons.check, color: colors.tertiary)),
      ),
      items: dropdownItems,
      onChanged: disabled! ? null : onChange,
      initialValue: (selectedValue != '') ? selectedValue : null,
    );
  }
}