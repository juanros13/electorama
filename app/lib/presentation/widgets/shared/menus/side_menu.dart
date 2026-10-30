import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/theme/theme_cubit.dart';
import 'package:electorama_app/presentation/widgets/shared/buttons/custom_filled_button.dart';

class SideMenu extends StatefulWidget {

  final GlobalKey<ScaffoldState> scaffoldKey;

  const SideMenu({
    super.key, 
    required this.scaffoldKey
  });

  @override
  State<SideMenu> createState() => _SideMenuState();
}

class _SideMenuState extends State<SideMenu> {

  int navDrawerIndex = 0;
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {

    final hasNotch = MediaQuery.of(context).viewPadding.top > 35;
    final textStyles = Theme.of(context).textTheme;
    final authBloc = context.watch<AuthBloc>();


    return NavigationDrawer(
      elevation: 1,
      selectedIndex: navDrawerIndex,
      onDestinationSelected: (value) {

        setState(() {
          navDrawerIndex = value;
        });

        // final menuItem = appMenuItems[value];
        // context.push( menuItem.link );
        widget.scaffoldKey.currentState?.closeDrawer();

      },
      children: [

        Padding(
          padding: EdgeInsets.fromLTRB(20, hasNotch ? 0 : 20, 16, 0),
          child: Text('Saludos', style: textStyles.titleMedium ),
        ),

        Padding(
          padding: const EdgeInsets.fromLTRB(20, 0, 16, 10),
          child: Text(
            '${authBloc.state.user?.firstName ?? ''} ${authBloc.state.user?.lastName ?? ''}'.trim(),
            style: textStyles.titleSmall,
          ),
        ),

        // const NavigationDrawerDestination(
        //     icon: Icon( Icons.home_outlined ), 
        //     label: Text( 'Productosasd' ),
        //     backgroundColor: Colors.blue
        // ),


        const Padding(
          padding: EdgeInsets.fromLTRB(28, 16, 28, 10),
          child: Divider(),
        ),

        Padding(
          padding: const EdgeInsets.fromLTRB(28, 0, 16, 4),
          child: Text('Módulos', style: textStyles.titleSmall),
        ),
        ListTile(
          leading: const Icon(Icons.add_circle_outline),
          title: const Text('Lonas y Bardas'),
          onTap: () {
            widget.scaffoldKey.currentState?.closeDrawer();
            context.go('/canvas-wall/0');
          },
        ),
        ListTile(
          leading: const Icon(Icons.person_add_alt),
          title: const Text('Convencidos'),
          onTap: () {
            widget.scaffoldKey.currentState?.closeDrawer();
            context.go('/follower/0');
          },
        ),
        ListTile(
          leading: const Icon(Icons.how_to_reg_outlined),
          title: const Text('Visitas'),
          onTap: () {
            widget.scaffoldKey.currentState?.closeDrawer();
            context.go('/visits/0');
          },
        ),
        ListTile(
          leading: const Icon(Icons.report_outlined),
          title: const Text('Incidencias'),
          onTap: () {
            widget.scaffoldKey.currentState?.closeDrawer();
            context.go('/issues/0');
          },
        ),

        const Padding(
          padding: EdgeInsets.fromLTRB(28, 8, 28, 10),
          child: Divider(),
        ),

        Padding(
          padding: const EdgeInsets.fromLTRB(28, 0, 16, 4),
          child: Text('Configuración', style: textStyles.titleSmall),
        ),
        ListTile(
          leading: const Icon(Icons.tune),
          title: const Text('Mis secciones'),
          subtitle: const Text('Secciones para Convencidos'),
          onTap: () {
            widget.scaffoldKey.currentState?.closeDrawer();
            context.go('/sections');
          },
        ),

        const Padding(
          padding: EdgeInsets.fromLTRB(28, 8, 28, 10),
          child: Divider(),
        ),

        Padding(
          padding: const EdgeInsets.fromLTRB(28, 10, 16, 10),
          child: Text('Apariencia', style: textStyles.titleSmall),
        ),

        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: BlocBuilder<ThemeCubit, ThemeMode>(
            builder: (context, mode) {
              return SizedBox(
                width: double.infinity,
                child: SegmentedButton<ThemeMode>(
                  segments: const [
                    ButtonSegment(value: ThemeMode.light, label: Text('Claro')),
                    ButtonSegment(value: ThemeMode.dark, label: Text('Oscuro')),
                    ButtonSegment(value: ThemeMode.system, label: Text('Auto')),
                  ],
                  selected: {mode},
                  showSelectedIcon: false,
                  onSelectionChanged: (set) =>
                      context.read<ThemeCubit>().setMode(set.first),
                ),
              );
            },
          ),
        ),

        const SizedBox(height: 16),

        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: CustomFilledButton(
            onPressed: () {
              authBloc.add(const Logout(''));
              // los módulos de captura se pueden usar sin sesión, así que el
              // router no saca solo: se regresa al inicio de sesión
              Navigator.of(context).maybePop();
              context.go('/home/0');
            },
            text: 'Cerrar sesión'
          ),
        ),

      ]
    );
  }
}