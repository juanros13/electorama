import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';

class CheckAuthStatusScreen extends StatefulWidget {
  const CheckAuthStatusScreen({super.key});

  @override
  State<CheckAuthStatusScreen> createState() => _CheckAuthStatusScreenState();
}

class _CheckAuthStatusScreenState extends State<CheckAuthStatusScreen> {
  @override
  void initState() {
    super.initState();
    BlocProvider.of<AuthBloc>(context).add(const CheckAuthStatus());
  }
  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: CircularProgressIndicator(strokeWidth: 2,),
      ),
    );
  }
}