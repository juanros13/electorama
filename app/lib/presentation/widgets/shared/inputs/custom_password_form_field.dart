import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class CustomPasswordFormField extends StatefulWidget {
  final String? label;
  final String? hint;
  final String? erroMessage;
  final Function(String)? onChange;
  final String? Function(String?)? validator;
  final bool obscureText;
  final IconData? iconData1;
  final IconData? iconData2;
  final bool isPure;
  final TextInputType? textInputType;
  final bool onlyNumbers;
  final TextEditingController? textEditingController;
  final String inputValue;
  final int? maxLength;
  final bool? enablePrefixButton;
  
  const CustomPasswordFormField({
    super.key, 
    this.label, 
    this.hint, 
    this.erroMessage, 
    this.onChange, 
    this.validator, 
    this.obscureText = false, 
    this.iconData1, 
    this.iconData2, 
    this.isPure = true, 
    this.textInputType = TextInputType.text, 
    this.onlyNumbers = false,
    this.textEditingController,
    this.inputValue = '',
    this.maxLength,
    this.enablePrefixButton = false,
  });

  @override
  State<CustomPasswordFormField> createState() => _CustomPasswordFormFieldState();
}

class _CustomPasswordFormFieldState extends State<CustomPasswordFormField> {

  bool obscureText = true;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return TextFormField(
      initialValue: widget.inputValue,
      keyboardType: widget.textInputType,
      inputFormatters: widget.onlyNumbers
        ? [FilteringTextInputFormatter.digitsOnly]
        : null,
      onChanged: widget.onChange,
      maxLength: widget.maxLength,
      validator: widget.validator,
      obscureText: obscureText,
      controller: widget.textEditingController,
      decoration: InputDecoration(
        label: widget.label != null ? Text(widget.label!) : null,
        hintText: widget.hint,
        counterText: '',
        prefixIcon: widget.enablePrefixButton!
            ? IconButton(
                icon: widget.iconData1 != null
                    ? Icon(obscureText ? widget.iconData1 : widget.iconData2)
                    : const Icon(Icons.lock),
                onPressed: () {
                  setState(() {
                    obscureText = !obscureText;
                  });
                },
              )
            : widget.iconData1 != null ? Icon(widget.iconData1) : null,
        errorText: widget.erroMessage,
        suffixIcon: widget.isPure
            ? null
            : (widget.erroMessage != null)
                ? FadeIn(child: Icon(Icons.close, color: colors.error))
                : FadeIn(child: Icon(Icons.check, color: colors.tertiary)),
      ),
    );
  }
}