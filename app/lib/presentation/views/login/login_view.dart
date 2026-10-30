import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/login/login_bloc.dart';
import 'package:electorama_app/presentation/widgets/forms/login/login_form.dart';
import 'package:electorama_app/presentation/widgets/shared/module_access_card.dart';


class LoginView extends StatelessWidget {

  const LoginView({super.key});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final isAuth = context.select<AuthBloc, bool>(
        (b) => b.state.authStatus == AuthStatus.authenticated);

    // Sin sesion aterriza directo en la pestana de captura (indice 1);
    // con sesion va a la vista de sincronizar (indice 0).
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: SingleChildScrollView(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [

                  MultiBlocProvider(
                    providers: [
                      BlocProvider(create: (context) => LoginBloc()),
                    ],
                    child: const LoginForm()
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  Align(
                    alignment: Alignment.centerLeft,
                    child: Text('Módulos', style: textTheme.titleSmall),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  ModuleAccessCard(
                    icon: Icons.campaign,
                    title: 'Lonas y Bardas',
                    subtitle: 'Captura sin conexión · sincroniza con sesión',
                    accent: AppColors.brandTeal,
                    onTap: () => context.go('/home/2'),
                  ),
                  ModuleAccessCard(
                    icon: Icons.person_add_alt_1,
                    title: 'Convencidos',
                    subtitle: 'Captura sin conexión · sincroniza con sesión',
                    accent: AppColors.brandGreen,
                    onTap: () => context.go(isAuth ? '/follower/0' : '/follower/1'),
                  ),
                  ModuleAccessCard(
                    icon: Icons.how_to_reg,
                    title: 'Visitas',
                    subtitle: 'Captura sin conexión · sincroniza con sesión',
                    accent: AppColors.warning,
                    onTap: () => context.go(isAuth ? '/visits/0' : '/visits/1'),
                  ),
                  ModuleAccessCard(
                    icon: Icons.report,
                    title: 'Incidencias',
                    subtitle: 'Atención ciudadana · captura offline',
                    accent: AppColors.issues,
                    onTap: () => context.go(isAuth ? '/issues/0' : '/issues/1'),
                  ),
                  const SizedBox(height: AppSpacing.xl),
                ],

              ),
            ),
          ),
        )

      ),
    );
  }
}

