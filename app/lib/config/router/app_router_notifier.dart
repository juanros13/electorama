import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';


class GoRouterAuthRefreshStream extends ChangeNotifier {
  late final StreamSubscription<AuthState> _subscription;
  AuthStatus _authStatus = AuthStatus.checking;

  GoRouterAuthRefreshStream(Stream<AuthState> stream) {
    notifyListeners();
    _subscription = stream.asBroadcastStream().listen((state) {
      authStatus = state.authStatus;
      // Only notify router to rebuild on following conditions
      // state.maybeWhen(
      //     authenticated: (_) => notifyListeners(),
      //     loggedIn: (_) => notifyListeners(),
      //     unauthenticated: () => notifyListeners(),
      //     orElse: () {});
    });
  }
  AuthStatus get authStatus => _authStatus;

  set authStatus( AuthStatus value ) {
    _authStatus = value;
    notifyListeners();
  }
  @override
  void dispose() {
    _subscription.cancel();
    super.dispose();
  }
}

// class GoRouterNotifier extends ChangeNotifier {

//   final AuthBloc _authBloc;

//   AuthStatus _authStatus = AuthStatus.checking;

//   GoRouterNotifier(this._authBloc) {
//     _authBloc.stream.listen((state) {
//       authStatus = state.authStatus;
//     });
//   }


//   AuthStatus get authStatus => _authStatus;

//   set authStatus( AuthStatus value ) {
//     _authStatus = value;
//     notifyListeners();
//   }

// }