import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/infrastructure/datasources/canvas_wall/service_canvas_wall_datasource_impl.dart';
import 'package:electorama_app/infrastructure/datasources/geo/dash_geo_datasource.dart';
import 'package:electorama_app/infrastructure/repositories/canvas_wall/service_canvas_wall_repository_impl.dart';
import 'package:electorama_app/infrastructure/repositories/geo/geo_repository_impl.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/geo/geography_bloc.dart';
import 'package:electorama_app/presentation/screens/gps/gps_access_screen.dart';
import 'package:electorama_app/presentation/views/canvas-and-walls/canvas_wall_list_sync_view.dart';
import 'package:electorama_app/presentation/views/canvas-and-walls/canvas_wall_create_view.dart';
import 'package:electorama_app/presentation/widgets/shared/navigation/app_navigation_bar.dart';


class HomeCanvasWallScreen extends StatelessWidget {
  
  static const name = 'canvas-wall-screen';
  final int pageIndex;
  const HomeCanvasWallScreen({super.key, required this.pageIndex});

  final viewRouter = const <Widget>[CanvasWallListSync(), CanvasWallCreateView()];

  @override
  Widget build(BuildContext context) {
    final authBloc = context.watch<AuthBloc>();

    return MultiBlocProvider(
      providers: [
        BlocProvider(
          create: (context) => GeographyBloc(GeoRepositoryImpl(DashGeoDatasouce(accessToken: authBloc.state.user?.token ?? ''))),
        ),
        BlocProvider(
          create: (context) => CanvasWallServiceBloc(ServiceCanvasWallRepositoryImpl(ServiceCanvasWallDatasourceImpl(accessToken: authBloc.state.user?.token ?? ''))),
        )
      ], 
      child: Scaffold(
        body: BlocBuilder<GpsBloc, GpsState>(
          builder: (context, state) {
            return state.isAllGranted
              ? Scaffold(
                  body: IndexedStack(
                    index: pageIndex,
                    children: viewRouter,
                  ),
                  bottomNavigationBar: AppNavigationBar(
                    currentIndex: pageIndex,
                    routePrefix: '/canvas-wall',
                    destinations: const [
                      AppNavDestination(
                        icon: Icons.sync_outlined,
                        selectedIcon: Icons.sync,
                        label: 'Sincronizar',
                      ),
                      AppNavDestination(
                        icon: Icons.add_circle_outline,
                        selectedIcon: Icons.add_circle,
                        label: 'Lonas y Bardas',
                      ),
                    ],
                  ))
              : const GpsAccessScreen();
          },
        ),
      ),
    );
  }
}