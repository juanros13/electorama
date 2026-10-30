import 'package:flutter/material.dart';
import 'package:electorama_app/presentation/widgets/forms/followers/follower_form.dart';

/// La ficha de captura de un convencido. El contexto (puesto y GPS) va dentro
/// del formulario; aquí sólo el título.
class FollowerCreateView extends StatelessWidget {
  const FollowerCreateView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Nuevo convencido'),
      ),
      body: const SafeArea(
        child: SingleChildScrollView(
          child: FollowerForm(),
        ),
      ),
    );
  }
}
