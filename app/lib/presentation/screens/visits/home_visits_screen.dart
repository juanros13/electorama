import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/infrastructure/datasources/geo/dash_geo_datasource.dart';
import 'package:electorama_app/infrastructure/datasources/visits/visited_datasource_impl.dart';
import 'package:electorama_app/infrastructure/repositories/geo/geo_repository_impl.dart';
import 'package:electorama_app/infrastructure/repositories/visits/visited_repository_impl.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/geo/geography_bloc.dart';
import 'package:electorama_app/presentation/screens/screens.dart';
import 'package:electorama_app/presentation/views/visits/visit_create_view.dart';
import 'package:electorama_app/presentation/views/visits/visit_list_sync_view.dart';
import 'package:electorama_app/presentation/widgets/shared/navigation/app_navigation_bar.dart';

class HomeVisitsScreen extends StatelessWidget {
  static const name = 'visits-screen';
  final int pageIndex;
  const HomeVisitsScreen({super.key, required this.pageIndex});

  final viewRouter = const <Widget>[VisitListSyncView(), VisitCreateView()];

  @override
  Widget build(BuildContext context)  {
    final authBloc = context.watch<AuthBloc>();

    return MultiBlocProvider(
      providers: [
        BlocProvider(
          create: (context) => GeographyBloc(GeoRepositoryImpl(DashGeoDatasouce(accessToken:authBloc.state.user?.token ?? ''))),
        ),
        BlocProvider(
          create: (context) => VisitsBloc(VisitedRepositoryImpl(VisitedDataSourceImpl(accessToken:authBloc.state.user?.token ?? ''))),
          
        ),
      ],
      child: Scaffold(body: BlocBuilder<GpsBloc, GpsState>(
        builder: (context, state) {
          return state.isAllGranted
              ? Scaffold(
                  body: IndexedStack(
                    index: pageIndex,
                    children: viewRouter,
                  ),
                  bottomNavigationBar: AppNavigationBar(
                    currentIndex: pageIndex,
                    routePrefix: '/visits',
                    destinations: const [
                      AppNavDestination(
                        icon: Icons.sync_outlined,
                        selectedIcon: Icons.sync,
                        label: 'Sincronizar',
                      ),
                      AppNavDestination(
                        icon: Icons.add_circle_outline,
                        selectedIcon: Icons.add_circle,
                        label: 'Visitas',
                      ),
                    ],
                  ))
              : const GpsAccessScreen();
        },
      )),
    );
  }
}
