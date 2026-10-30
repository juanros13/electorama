import 'package:go_router/go_router.dart';
import 'package:electorama_app/config/router/app_router_notifier.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/screens/screens.dart';

// Crea tu AuthBloc
//final AuthBloc authBloc = AuthBloc();

// Crea tu ChangeNotifier y proporciona el AuthBloc

GoRouter createGoRouter(AuthBloc bloc) {
//GoRouter createGoRouter() {
  //final authChangeNotifier = GoRouterNotifier(bloc);
  
  return  GoRouter(
    initialLocation: '/splash', 
    //initialLocation: '/splash', 
    refreshListenable: GoRouterAuthRefreshStream(bloc.stream),
    routes: [
      
      GoRoute(
        path: '/splash',
        builder: (context, state) {
          return const CheckAuthStatusScreen();
        },
      ),
      GoRoute(
        path: '/home/:page',
        name: HomeScreen.name,
        builder: (context, state) {
          final pageIndex = state.pathParameters['page'] ?? '0';
          //if(pageIndex >2 || pageIndex )
          return HomeScreen(pageIndex: int.parse(pageIndex));
        },
      ),
      GoRoute(
        path: '/visits/:page',
        name: HomeVisitsScreen.name,
        builder: (context, state) {
          final pageIndex = state.pathParameters['page'] ?? '0';
          return HomeVisitsScreen(pageIndex: int.parse(pageIndex));
        },
      ),
      GoRoute(
        path: '/follower/:page',
        name: HomeFollowerScreen.name,
        builder: (context, state) {
          final pageIndex = state.pathParameters['page'] ?? '0';
          return HomeFollowerScreen(pageIndex: int.parse(pageIndex));
        },
      ),
      GoRoute(
        path: '/scanner',
        name: Scanner.name,
        builder: (context, state) => const Scanner(),
      ),
      GoRoute(
        path: '/sections',
        name: SectionsScreen.name,
        builder: (context, state) => const SectionsScreen(),
      ),
      GoRoute(
        path: '/issues/:page',
        name: HomeIssuesScreen.name,
        builder: (context, state) {
          final pageIndex = state.pathParameters['page'] ?? '0';
          return HomeIssuesScreen(pageIndex: int.parse(pageIndex));
        },
      ),
      GoRoute(
        path: '/canvas-wall/:page',
        name: HomeCanvasWallScreen.name,
        builder: (context, state) {
          final pageIndex = state.pathParameters['page'] ?? '0';
          //if(pageIndex >2 || pageIndex )
          return HomeCanvasWallScreen(pageIndex: int.parse(pageIndex));
        },
      ),
      GoRoute(
        path: '/',
        redirect: (_, __) => '/home/0',
      )
    ],
    redirect: (context, state) async {
      final isGoingTo = state.matchedLocation;
      final authStatus = bloc.state.authStatus;

      if ( isGoingTo == '/splash' && authStatus == AuthStatus.checking ) return null;

      if ( authStatus == AuthStatus.notAuthenticated ) {
        // Modulos con captura offline (login solo para sincronizar):
        // Lonas y Bardas (/home/1,2), Convencidos (/follower) y Visitas (/visits),
        // mas el escaner de INE. El resto se manda al login.
        if ( isGoingTo == '/home/0' || isGoingTo == '/home/1' || isGoingTo == '/home/2' ||
             isGoingTo.startsWith('/follower') || isGoingTo.startsWith('/visits') ||
             isGoingTo.startsWith('/issues') ||
             isGoingTo == '/scanner' || isGoingTo == '/sections' ) return null;

        return '/home/0';
      }

      // if ( authStatus == AuthStatus.authenticated ) {
      //   if ( isGoingTo == '/home/0' || isGoingTo == '/home/1' || isGoingTo == '/home/2' || isGoingTo == '/splash' ){
      //     return '/visits/0';
      //   }
      // }

      if ( authStatus == AuthStatus.authenticated ) {
        if ( isGoingTo == '/home/0' || isGoingTo == '/home/1' || isGoingTo == '/home/2' || isGoingTo == '/splash' ){
          return '/canvas-wall/0';
        }
      }


      return null;
    },
  );
}