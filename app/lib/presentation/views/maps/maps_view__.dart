import 'package:flutter/material.dart';
import 'package:electorama_app/presentation/screens/screens.dart';

class VisitsView extends StatelessWidget {
  const VisitsView({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
        debugShowCheckedModeBanner: false, title: 'MapsApp', home: MapScreen());
  }
}
