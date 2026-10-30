import 'package:flutter/material.dart';
// import 'package:flutter/services.dart';

class CustomDateFormField extends StatefulWidget {
  final DateTime? selectedDate;
  final Function(DateTime?)? onDateChanged;
  final IconData? iconData;
  final String? label;
  final String? errorMessage;
  final bool? disabled;

  const CustomDateFormField({
    super.key,
    this.selectedDate,
    this.onDateChanged,
    this.iconData,
    this.label,
    this.errorMessage,
    this.disabled = false,
  });

  @override
  State<CustomDateFormField> createState() => _CustomDateFormFieldState();
  // _CustomDateFormFieldState createState() => _CustomDateFormFieldState();
}

class _CustomDateFormFieldState extends State<CustomDateFormField> {
  late TextEditingController _controller;
  late DateTime _selectedDate;

  @override
  void initState() {
    super.initState();
    _selectedDate = widget.selectedDate ?? DateTime.now();
    _controller = TextEditingController(
      text: _selectedDate.toLocal().toString().split(' ')[0],
    );
  }

  Future<void> _selectDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2101),
    );

    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
        _controller.text = _selectedDate.toLocal().toString().split(' ')[0];
      });
      widget.onDateChanged?.call(_selectedDate);
    }
  }

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: _controller,
      onTap: widget.disabled!
          ? null
          : () {
              if (widget.disabled == false) {
                _selectDate(context);
              }
            },
      readOnly: true,
      decoration: InputDecoration(
        labelText: widget.label,
        prefixIcon: widget.iconData != null ? Icon(widget.iconData) : null,
        errorText: widget.errorMessage,
      ),
    );
  }
}