import 'package:animate_do/animate_do.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/login/login_bloc.dart';
import 'package:electorama_app/presentation/widgets/widgets.dart';

class LoginForm extends StatefulWidget {
  const LoginForm({super.key});

  @override
  State<LoginForm> createState() => _LoginFormState();
}

class _LoginFormState extends State<LoginForm> {
  late AuthBloc authBloc;
  @override
  void initState() {
    super.initState();
    BlocProvider.of<AuthBloc>(context).add(const CheckAuthStatus());
  }
  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final loginBloc = context.watch<LoginBloc>();
    final email = loginBloc.state.email;
    final password = loginBloc.state.password;
    final authBloc = context.watch<AuthBloc>();
    final textTheme = Theme.of(context).textTheme;
    if(authBloc.state.loading){
      EasyLoading.show(
        status: 'Cargando',
        maskType: EasyLoadingMaskType.black,
        dismissOnTap: false
      );
    }else{
      EasyLoading.dismiss();
    }
    
    
    return Form(
      child: Column(
        children: [
          const SizedBox(height: 20),
          FadeIn(
            duration: const Duration(milliseconds: 1000),
            child: const Image(
              image: AssetImage('assets/images/logo_normal.png'),
              height: 250,
            )
          ),
          const SizedBox(height: 10,),
          Text('Módulo de Lonas y Bardas',
              textAlign: TextAlign.center,
              style: textTheme.headlineSmall?.copyWith(color: colors.primary)),
          const SizedBox(height: 4),
          Text('Inicia sesión solamente para sincronizar',
              textAlign: TextAlign.center,
              style: textTheme.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
          (authBloc.state.errorMessage.isNotEmpty)
          ? Center(
              child: SizedBox(
                width: MediaQuery.of(context).size.width, // Ancho igual al ancho de la pantalla
                child: _ErrorCard(error: authBloc.state.errorMessage),
              ),
            )
          :const SizedBox(),
          const SizedBox(height: 20),
          // Text('Iniciar Sesión',style: TextStyle(
          //   fontSize: 20,
          //   fontWeight: FontWeight.bold,
          //   color: colors.primary
          // )),
          const SizedBox(height: 30),
          CustomTextFormField(
            iconData: Icons.person,
            label: 'Correo electrónico',
            erroMessage: loginBloc.state.isValid ? 
            null:email.getErrorMessage,
            onChange: (value) {
              loginBloc.add(EmailChange(value));
              
            },
            isPure: email.isPure
          ),
          const SizedBox(height: 20),
          CustomPasswordFormField(
            iconData1: Icons.lock,
            iconData2: Icons.lock_open,
            label: 'Contraseña',
            obscureText: true,
            erroMessage: password.getErrorMessage,
            onChange: (value) {
              loginBloc.add(PasswordChange(value));
              //_formKey.currentState?.validate();
            },
            isPure: password.isPure,
            enablePrefixButton: true,
          ),
          const SizedBox(height: 30),
          FilledButton.icon(
            onPressed: loginBloc.state.isValid
                ? () {
                    FocusManager.instance.primaryFocus?.unfocus();
                    loginBloc.add(const Submit());
                    authBloc.add(Login(email.value, password.value));
                  }
                : null,
            icon: const Icon(Icons.login),
            label: const Text('Iniciar Sesión'),
          ),

          // TextButton(
          //   onPressed: () => context.push('/loading'), 
          //   child: const Text('Iniciar sesion mas tarde')
          // )

        ],
      )
    );
  }
}

class _ErrorCard extends StatelessWidget {
  final String error;
  const _ErrorCard({required this.error});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;

    return Card(
      color: colors.errorContainer,
      margin: const EdgeInsets.symmetric(vertical: 8.0),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.error_outline, color: colors.onErrorContainer),
            const SizedBox(width: 8.0),
            Flexible(
              child: Text(
                error,
                style: textTheme.bodyMedium?.copyWith(
                  color: colors.onErrorContainer,
                  fontWeight: FontWeight.w600,
                ),
                textAlign: TextAlign.center,
              ),
            ),
          ],
        ),
      ),
    );
  }
}