import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/presentation/views/canvas-and-walls/canvas_wall_list_view.dart';
import 'package:electorama_app/presentation/views/canvas-and-walls/canvas_wall_create_view.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/screens/screens.dart';
import 'package:electorama_app/presentation/views/views.dart';
import 'package:electorama_app/presentation/widgets/shared/navigation/app_navigation_bar.dart';

class HomeScreen extends StatelessWidget {
  static const name = 'home-screen';
  final int pageIndex;
  const HomeScreen({super.key, required this.pageIndex});

  final viewRouter = const <Widget>[LoginView(), CanvasAndWallsListView(), CanvasWallCreateView()];


  @override
  Widget build(BuildContext context) {
    return Scaffold(body: BlocBuilder<GpsBloc, GpsState>(
      builder: (context, state) {
        return 
            state.isAllGranted
              ? Scaffold(
                  body: IndexedStack(
                    index: pageIndex,
                    children: viewRouter,
                  ),
                  bottomNavigationBar: AppNavigationBar(
                    currentIndex: pageIndex,
                    routePrefix: '/home',
                    destinations: const [
                      AppNavDestination(
                        icon: Icons.account_circle_outlined,
                        selectedIcon: Icons.account_circle,
                        label: 'Login',
                      ),
                      AppNavDestination(
                        icon: Icons.checklist_outlined,
                        selectedIcon: Icons.checklist,
                        label: 'Capturados',
                      ),
                      AppNavDestination(
                        icon: Icons.add_circle_outline,
                        selectedIcon: Icons.add_circle,
                        label: 'Lonas y Bardas',
                      ),
                    ],
                  )
                )
              : const GpsAccessScreen();
      },
    ));
  }
}

