import 'package:flutter/material.dart';

class CustomFilledButton extends StatelessWidget {

  final void Function()? onPressed;
  final String text;
  final Color? buttonColor;

  const CustomFilledButton({
    super.key, 
    this.onPressed, 
    required this.text, 
    this.buttonColor
  });

  @override
  Widget build(BuildContext context) {


    return FilledButton(
      style: buttonColor != null
          ? FilledButton.styleFrom(backgroundColor: buttonColor)
          : null,
      onPressed: onPressed,
      child: Text(text),
    );
  }
}