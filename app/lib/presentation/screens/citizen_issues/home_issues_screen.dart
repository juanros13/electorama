import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/catalogs/issue_catalogs_cubit.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_list/issue_list_cubit.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_sync/issue_sync_cubit.dart';
import 'package:electorama_app/presentation/views/citizen_issues/issue_create_view.dart';
import 'package:electorama_app/presentation/views/citizen_issues/issue_list_sync_view.dart';
import 'package:electorama_app/presentation/widgets/shared/navigation/app_navigation_bar.dart';

class HomeIssuesScreen extends StatefulWidget {
  static const name = 'issues-screen';
  final int pageIndex;
  const HomeIssuesScreen({super.key, required this.pageIndex});

  @override
  State<HomeIssuesScreen> createState() => _HomeIssuesScreenState();
}

class _HomeIssuesScreenState extends State<HomeIssuesScreen> {
  static const _views = <Widget>[IssueListSyncView(), IssueCreateView()];

  @override
  void initState() {
    super.initState();
    context.read<IssueCatalogsCubit>().load();
    context.read<IssueListCubit>().load();
  }

  @override
  Widget build(BuildContext context) {
    final authBloc = context.watch<AuthBloc>();
    return BlocProvider(
      create: (_) =>
          IssueSyncCubit(accessToken: authBloc.state.user?.token ?? ''),
      child: Scaffold(
        body: IndexedStack(index: widget.pageIndex, children: _views),
        bottomNavigationBar: AppNavigationBar(
          currentIndex: widget.pageIndex,
          routePrefix: '/issues',
          destinations: const [
            AppNavDestination(
              icon: Icons.sync_outlined,
              selectedIcon: Icons.sync,
              label: 'Sincronizar',
            ),
            AppNavDestination(
              icon: Icons.add_circle_outline,
              selectedIcon: Icons.add_circle,
              label: 'Incidencias',
            ),
          ],
        ),
      ),
    );
  }
}
