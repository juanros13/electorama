import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/infrastructure/datasources/geo/dash_geo_datasource.dart';
import 'package:electorama_app/infrastructure/repositories/geo/geo_repository_impl.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/follower_service/follower_service_bloc.dart';
import 'package:electorama_app/presentation/blocs/geo/geography_bloc.dart';
import 'package:electorama_app/presentation/screens/gps/gps_access_screen.dart';
import 'package:electorama_app/presentation/views/followers/follower_create_view.dart';
import 'package:electorama_app/presentation/views/followers/follower_list_sync_view.dart';
import 'package:electorama_app/presentation/widgets/shared/navigation/app_navigation_bar.dart';

class HomeFollowerScreen extends StatelessWidget {
  static const name = 'follower-screen';
  final int pageIndex;
  const HomeFollowerScreen({super.key, required this.pageIndex});

  final viewRouter = const <Widget>[
    FollowerListSyncView(),
    FollowerCreateView()
  ];

  @override
  Widget build(BuildContext context) {
    final authBloc = context.watch<AuthBloc>();

    return MultiBlocProvider(
      providers: [
        BlocProvider(
          create: (context) => GeographyBloc(GeoRepositoryImpl(
              DashGeoDatasouce(accessToken: authBloc.state.user?.token ?? ''))),
        ),
        BlocProvider(
          // manda a msvc-structure; el token sale de la sesión guardada
          create: (context) => FollowerServiceBloc(),
        ),
      ],
      child: Scaffold(
        body: BlocBuilder<GpsBloc, GpsState>(
          builder: (context, state) {
            return state.isAllGranted
                ? Scaffold(
                    body: IndexedStack(index: pageIndex, children: viewRouter),
                    bottomNavigationBar: AppNavigationBar(
                      currentIndex: pageIndex,
                      routePrefix: '/follower',
                      destinations: const [
                        AppNavDestination(
                          icon: Icons.sync_outlined,
                          selectedIcon: Icons.sync,
                          label: 'Sincronizar',
                        ),
                        AppNavDestination(
                          icon: Icons.person_add_alt,
                          selectedIcon: Icons.person_add_alt_1,
                          label: 'Convencidos',
                        ),
                      ],
                    ),
                  )
                : const GpsAccessScreen();
          },
        ),
      ),
    );
  }
}
