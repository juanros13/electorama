import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_list/issue_list_cubit.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_sync/issue_sync_cubit.dart';
import 'package:electorama_app/presentation/widgets/cards/citizen_issues/issue_card.dart';
import 'package:electorama_app/presentation/widgets/shared/menus/side_menu.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';

class IssueListSyncView extends StatefulWidget {
  const IssueListSyncView({super.key});

  @override
  State<IssueListSyncView> createState() => _IssueListSyncViewState();
}

class _IssueListSyncViewState extends State<IssueListSyncView>
    with TickerProviderStateMixin {
  late final TabController _tabController;
  final scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    context.read<IssueListCubit>().load();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final listCubit = context.watch<IssueListCubit>();
    final syncCubit = context.watch<IssueSyncCubit>();
    final isAuth = context.select<AuthBloc, bool>(
        (b) => b.state.authStatus == AuthStatus.authenticated);
    final state = listCubit.state;

    if (syncCubit.state.done) {
      final s = syncCubit.state.synced;
      final e = syncCubit.state.failed;
      Future.delayed(Duration.zero, () {
        if (!mounted) return;
        syncCubit.clearDone();
        context.read<IssueListCubit>().load();
        if (s > 0) {
          CustomSnackbar.showSnackbar(
              context, TypeSnackbar.success, '$s incidencias sincronizadas.');
        }
        if (e > 0) {
          CustomSnackbar.showSnackbar(
              context, TypeSnackbar.error, '$e no se pudieron sincronizar.');
        }
      });
    }

    final pending = state.issues.where((i) => !i.isInSync).toList();
    final synced = state.issues.where((i) => i.isInSync).toList();

    return Scaffold(
      key: scaffoldKey,
      drawer: SideMenu(scaffoldKey: scaffoldKey),
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Listado de Incidencias'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(icon: Icon(Icons.pending_actions), text: 'Pendientes'),
            Tab(icon: Icon(Icons.cloud_done), text: 'Vinculados'),
          ],
        ),
      ),
      body: Column(
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 12, 16, 0),
            child: ModuleHeader(
              icon: Icons.report,
              title: 'Incidencias',
              subtitle: 'Atención ciudadana · captura offline',
              accent: AppColors.issues,
            ),
          ),
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _PendingTab(
                    pending: pending, isAuth: isAuth, syncCubit: syncCubit),
                _SyncedTab(synced: synced),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _PendingTab extends StatelessWidget {
  final List<Issue> pending;
  final bool isAuth;
  final IssueSyncCubit syncCubit;
  const _PendingTab(
      {required this.pending, required this.isAuth, required this.syncCubit});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return SafeArea(
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text('Total Pendientes: ${pending.length}',
                style: textTheme.headlineSmall),
          ),
          Expanded(
            child: pending.isEmpty
                ? const Center(child: Text('No tienes incidencias pendientes'))
                : ListView(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    children: [
                      if (isAuth)
                        FilledButton.icon(
                          onPressed: syncCubit.state.loading
                              ? null
                              : () => syncCubit.syncPending(pending),
                          icon: const Icon(Icons.sync),
                          label: const Text('Iniciar sincronización'),
                        )
                      else
                        const Card(
                          child: ListTile(
                            leading: Icon(Icons.cloud_off_outlined),
                            title: Text('Inicia sesión para sincronizar'),
                          ),
                        ),
                      const SizedBox(height: 16),
                      for (final issue in pending)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: IssueCard(issue: issue),
                        ),
                    ],
                  ),
          ),
        ],
      ),
    );
  }
}

class _SyncedTab extends StatelessWidget {
  final List<Issue> synced;
  const _SyncedTab({required this.synced});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return SafeArea(
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text('Total Vinculados: ${synced.length}',
                style: textTheme.headlineSmall),
          ),
          Expanded(
            child: synced.isEmpty
                ? const Center(child: Text('Aún no hay incidencias vinculadas'))
                : ListView(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    children: [
                      for (final issue in synced)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 12),
                          child: IssueCard(issue: issue),
                        ),
                    ],
                  ),
          ),
        ],
      ),
    );
  }
}
