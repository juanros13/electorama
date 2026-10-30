import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall/canvas_wall_bloc.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall_service/canvas_wall_service_bloc.dart';
import 'package:electorama_app/presentation/widgets/forms/canvas_wall/canvas_wall_sync_form.dart';
import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/presentation/widgets/cards/canvas_and_walls_card.dart';
import 'package:electorama_app/presentation/widgets/shared/menus/side_menu.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';
import 'package:electorama_app/presentation/widgets/widgets.dart';

class CanvasWallListSync extends StatefulWidget {
  const CanvasWallListSync({super.key});

  @override
  State<CanvasWallListSync> createState() => _CanvasWallListSyncState();
}

class _CanvasWallListSyncState extends State<CanvasWallListSync>
    with TickerProviderStateMixin {
  late final TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    context.read<CanvasWallBloc>().add(const LoadCanvasWall());
    context.read<CanvasWallServiceBloc>().state;
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    
    final colors = Theme.of(context).colorScheme;
    final scaffoldKey = GlobalKey<ScaffoldState>();
    final canvasWallBloc = context.watch<CanvasWallBloc>();
    bool showForm = true;
    // final canvasWallServiceBloc = context.watch<CanvasWallServiceBloc>();

    // int canvasWallPending = 0;
    // int canvasWallSync = 0;

    return Scaffold(
      drawer: SideMenu( scaffoldKey: scaffoldKey ),
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Listado de Lonas y Bardas'),
        bottom: TabBar(
          controller: _tabController,
          tabs: const <Widget>[
            Tab(
              icon: Icon(Icons.language),
              text: "Pendientes",
            ),
            Tab(
              icon: Icon(Icons.data_usage),
              text: "Vinculados",
            ),
          ],
        ),
      ),




      body: Column(
        children: [
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 12, 16, 0),
            child: ModuleHeader(
              icon: Icons.campaign,
              title: 'Lonas y Bardas',
              subtitle: 'Propaganda fija · registro georreferenciado',
              accent: AppColors.brandTeal,
            ),
          ),
          Expanded(
            child: TabBarView(
        controller: _tabController,
        children: <Widget>[
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.only(top: 10),
              child: Column(
                children: [
                  SizedBox(child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
                    builder: (context, state) {
                      return Padding(
                        padding: const EdgeInsets.all(15.0),
                        child: Column(
                          children: [
                            
                            
                            const Text(
                                  "Registros capturados en este teléfono que aun NO están almacenados en el sistema y necesitan ser vinculados.",
                                  style: TextStyle(fontSize: 16),
                                ),
                            const SizedBox(height: 10,),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                Text(
                                  'Total Pendientes: ${canvasWallBloc.state.canvasWallPending}',
                                  style: const TextStyle(
                                      fontSize: 25.0,
                                      fontWeight: FontWeight.w700
                                  )
                                ),
                                const SizedBox(width: 16.0),
                                state.loading
                                ? CircularProgressIndicator(color: colors.primary)
                                : const SizedBox(),
                              ],
                            ),


                          ],
                        ),
                      );
                    },
                  )),
                  Expanded(
                    child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
                    builder: (context, state) {
                      if (canvasWallBloc.state.listCanvasWall.isEmpty || canvasWallBloc.state.canvasWallPending == 0) {
                        return const NoCanvasWallSyncMessage();
                      } else {
                        return ListView.builder(
                                  padding: const EdgeInsets.symmetric(horizontal: 16),
                                  itemBuilder: (context, index) {
                                    final canvasWall = canvasWallBloc.state.listCanvasWall[index];

                                    if (!canvasWall.isInSync) {


                                      if(showForm == true) {
                                        showForm = false;
                                        return Column(
                                            children: [
                                              const CanvasWallSyncForm(),
                                              const SizedBox(height: 15),
                                              CanvasAndWallsCard(
                                                canvasWall: canvasWall,
                                                index: index
                                              ),
                                              const SizedBox(height: 15),
                                            ],
                                          );
                                      } else {
                                        return Column(
                                            children: [
                                              CanvasAndWallsCard(
                                                canvasWall: canvasWall,
                                                index: index
                                              ),
                                              const SizedBox(height: 15)
                                            ],
                                          );
                                      }


                                    } else {
                                      return const SizedBox();
                                    }
                                  },
                                  itemCount: canvasWallBloc
                                      .state.listCanvasWall.length,
                                );
                      }
                    },
                  ))
                ],
              ),
            ),
          ),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.only(top: 10),
              child: Column(
                children: [
                  SizedBox(child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
                    builder: (context, state) {
                      return  Padding(
                        padding: const EdgeInsets.all(15.0),
                        child: Column(
                          children: [
                            state.loading
                              ? Center(
                                  child: CircularProgressIndicator(color: colors.primary),
                                )
                              : const Text(
                                  "Registros capturados en este teléfono que ya están almacenados en el sistema.",
                                  style: TextStyle(fontSize: 16),
                                ),
                            const SizedBox(height: 10,),
                            Text(
                              'Total Vinculados: ${canvasWallBloc.state.canvasWallSync}',
                              style: const TextStyle(
                                fontSize: 25.0,
                                fontWeight: FontWeight.w700
                              )
                            ),
                          ],
                        ),
                      );
                    },
                  )),
                  Expanded(child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
                    builder: (context, state) {
                      return
                          // state.loading
                          // ? Center(child: CircularProgressIndicator(color: colors.primary),)
                          // :
                          canvasWallBloc.state.listCanvasWall.isEmpty || canvasWallBloc.state.canvasWallSync == 0
                              ? const NoCanvasWallMessage()
                              : ListView.builder(
                                  padding: const EdgeInsets.symmetric(horizontal: 16),
                                  itemBuilder: (context, index) {
                                    final canvasWall = canvasWallBloc
                                        .state.listCanvasWall[index];
                                    return canvasWall.isInSync
                                        ? Column(
                                            children: [
                                              CanvasAndWallsCard(
                                                  canvasWall: canvasWall,
                                                  index: index),
                                              const SizedBox(height: 15)
                                            ],
                                          )
                                        : const SizedBox(
                                            height: 0,
                                          );
                                  },
                                  itemCount: canvasWallBloc
                                      .state.listCanvasWall.length,
                                );
                    },
                  ))
                ],
              ),
            ),
          ),
        ],
            ),
          ),
        ],
      ),


    );
  }
}
