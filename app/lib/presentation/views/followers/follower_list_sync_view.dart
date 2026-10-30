import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';

import 'package:electorama_app/infrastructure/services/capture_catalog.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/blocs/follower/follower_bloc.dart';
import 'package:electorama_app/presentation/blocs/follower_service/follower_service_bloc.dart';
import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/presentation/widgets/cards/followers_card.dart';
import 'package:electorama_app/presentation/widgets/shared/menus/side_menu.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';
import 'package:electorama_app/presentation/widgets/shared/user_message/no_followers_message.dart';
import 'package:electorama_app/presentation/widgets/shared/user_message/no_followers_sync_message.dart';

class FollowerListSyncView extends StatefulWidget {
  const FollowerListSyncView({super.key});

  @override
  State<FollowerListSyncView> createState() => _FollowerListSyncViewState();
}

class _FollowerListSyncViewState extends State<FollowerListSyncView>
    with TickerProviderStateMixin {
  late final TabController _tabController;
  final scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    context.read<FollowerBloc>().add(const LoadFollowers());
    // con sesión y red, se guardan los puestos y polígonos para capturar sin señal
    if (context.read<AuthBloc>().state.authStatus == AuthStatus.authenticated) {
      CaptureCatalog().refresh().catchError((_) => 0);
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final followerBloc = context.watch<FollowerBloc>();
    final authBloc = context.watch<AuthBloc>();
    final followerServiceBloc = context.watch<FollowerServiceBloc>();

    if (followerServiceBloc.state.loading) {
      EasyLoading.show(
          status: 'Sincronizando',
          maskType: EasyLoadingMaskType.black,
          dismissOnTap: false);
    } else {
      EasyLoading.dismiss();
    }

    if (followerServiceBloc.state.submitSync) {
      final sync = followerServiceBloc.state.followerSync;
      final err = followerServiceBloc.state.followerError;
      final dup = followerServiceBloc.state.followerDuplicate;
      final problem = followerServiceBloc.state.error;
      Future.delayed(Duration.zero, () {
        if (!mounted) return;
        followerServiceBloc.add(ChangeSubmitFollowerSync(value: false));
        context.read<FollowerBloc>().add(const LoadFollowers());
        if (sync > 0) {
          CustomSnackbar.showSnackbar(context, TypeSnackbar.success,
              '$sync convencidos sincronizados correctamente.');
        }
        if (dup > 0) {
          CustomSnackbar.showSnackbar(context, TypeSnackbar.success,
              '$dup ya eran convencidos: no se duplicaron.');
        }
        if (err > 0) {
          CustomSnackbar.showSnackbar(context, TypeSnackbar.error,
              '$err no se aceptaron: revisa el motivo en cada uno.');
        }
        if (problem.isNotEmpty) {
          CustomSnackbar.showSnackbar(context, TypeSnackbar.error, problem);
        }
      });
    }

    return BlocListener<FollowerBloc, FollowerState>(
      listener: (context, state) {
        if (followerServiceBloc.state.loading == false) {
          // pendientes y rechazados (estos se reintentan por si ya se corrigió
          // el puesto o la sección en el servidor)
          final pending =
              state.listFollower.where((f) => f.needsAttention).toList();
          context
              .read<FollowerServiceBloc>()
              .add(ChangeListFollowers(listCanvasWall: pending));
        }
      },
      child: Scaffold(
        key: scaffoldKey,
        drawer: SideMenu(scaffoldKey: scaffoldKey),
        appBar: AppBar(
          centerTitle: true,
          title: const Text('Listado de Convencidos'),
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
                icon: Icons.person_add_alt_1,
                title: 'Convencidos',
                subtitle: 'Enrolamiento formal · CURP, INE y encuesta',
                accent: AppColors.brandGreen,
              ),
            ),
            Expanded(
              child: TabBarView(
                controller: _tabController,
                children: [
                  _PendingTab(
                    followerBloc: followerBloc,
                    authBloc: authBloc,
                  ),
                  _SyncedTab(followerBloc: followerBloc),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PendingTab extends StatelessWidget {
  final FollowerBloc followerBloc;
  final AuthBloc authBloc;
  const _PendingTab({required this.followerBloc, required this.authBloc});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final state = followerBloc.state;
    return SafeArea(
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text('Total Pendientes: ${state.followerPending}',
                style: textTheme.headlineSmall),
          ),
          Expanded(
            child: (state.listFollower.isEmpty || state.followerPending == 0)
                ? const NoFollowersSyncMessage()
                : ListView(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    children: [
                      if (authBloc.state.authStatus == AuthStatus.authenticated)
                        FilledButton.icon(
                          onPressed: () => context
                              .read<FollowerServiceBloc>()
                              .add(const SentFollowerService()),
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
                      for (var i = 0; i < state.listFollower.length; i++)
                        if (!state.listFollower[i].isInSync)
                          Padding(
                            padding: const EdgeInsets.only(bottom: 15),
                            child: FollowerCard(
                                follower: state.listFollower[i], index: i),
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
  final FollowerBloc followerBloc;
  const _SyncedTab({required this.followerBloc});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    final state = followerBloc.state;
    return SafeArea(
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text('Total Vinculados: ${state.followerSync}',
                style: textTheme.headlineSmall),
          ),
          Expanded(
            child: (state.listFollower.isEmpty || state.followerSync == 0)
                ? const NoFollowersMessage()
                : ListView(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    children: [
                      for (var i = 0; i < state.listFollower.length; i++)
                        if (state.listFollower[i].isInSync)
                          Padding(
                            padding: const EdgeInsets.only(bottom: 15),
                            child: FollowerCard(
                                follower: state.listFollower[i], index: i),
                          ),
                    ],
                  ),
          ),
        ],
      ),
    );
  }
}
