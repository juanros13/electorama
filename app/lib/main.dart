import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall/canvas_wall_bloc.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall_form/canvas_wall_form_bloc.dart';
import 'package:flutter/material.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:timezone/data/latest_10y.dart' as tz;
import 'package:electorama_app/config/config.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/coordinator_zone/coordinator_zone_form_bloc.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people/visits_people_bloc.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people_form/visits_people_form_bloc.dart';
import 'package:electorama_app/presentation/blocs/theme/theme_cubit.dart';
import 'package:electorama_app/presentation/blocs/follower/follower_bloc.dart';
import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/presentation/blocs/user_settings/user_settings_bloc.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/catalogs/issue_catalogs_cubit.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_list/issue_list_cubit.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_form/issue_form_cubit.dart';
import 'package:electorama_app/infrastructure/services/key_value_storage_service_impl.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
import 'package:electorama_app/infrastructure/services/capture_catalog.dart';


void main() async {
  await Environment.initEnvironment();
  tz.initializeTimeZones();
  runApp(MultiBlocProvider(
    providers: [
      BlocProvider(create: (context) => GpsBloc()),
      BlocProvider(create: (context) => LocationBloc()),
      BlocProvider(create: (context) => VisitPeopleFormBloc()),
      BlocProvider(create: (context) => VisitsPeopleBloc()),
      BlocProvider(create: (context) => AuthBloc()),
      BlocProvider(create: (context) => CordinatorZoneFormBloc()),
      BlocProvider(create: (context) => CanvasWallBloc()),
      BlocProvider(create: (context) => CanvasWallFormBloc()),
      BlocProvider(create: (context) => ThemeCubit(KeyValueStorageServiceImpl())),
      BlocProvider(create: (context) => FollowerBloc()),
      BlocProvider(create: (context) => FollowerFormBloc()),
      BlocProvider(create: (context) => UserSettingsBloc()),
      BlocProvider(create: (context) => IssueCatalogsCubit()),
      BlocProvider(create: (context) => IssueListCubit()),
      BlocProvider(create: (context) => IssueFormCubit()),
    ],
    
    child: const MyApp(),
  ));
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    
    // al iniciar sesión (o arrancar con sesión) se bajan sus puestos y quedan
    // seleccionadas sus secciones; sin red se usa lo guardado
    return BlocListener<AuthBloc, AuthState>(
      listenWhen: (prev, curr) =>
          prev.authStatus != AuthStatus.authenticated &&
          curr.authStatus == AuthStatus.authenticated,
      listener: (context, state) async {
        final settings = context.read<UserSettingsBloc>();
        try {
          await CaptureCatalog().refresh();
        } catch (_) {
          // sin red: siguen las secciones guardadas
        }
        settings.add(LoadAllSettings());
        settings.add(LoadSettings());
      },
      child: BlocBuilder<ThemeCubit, ThemeMode>(
      builder: (context, themeMode) {
        return MaterialApp.router(
          title: 'Dash App - Canvas and Walls',
          debugShowCheckedModeBanner: false,
          theme: AppTheme.light,
          darkTheme: AppTheme.light,
          themeMode: themeMode,
          routerConfig: createGoRouter(context.read<AuthBloc>()),
          builder: EasyLoading.init(),
        );
      },
      ),
    );
  }
}
