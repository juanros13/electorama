import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/presentation/widgets/cards/canvas_and_walls_card.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall/canvas_wall_bloc.dart';
import 'package:electorama_app/presentation/widgets/widgets.dart';

enum SampleItem { itemOne, itemTwo, itemThree }

class CanvasAndWallsListView extends StatefulWidget {
  const CanvasAndWallsListView({super.key});

  @override
  State<CanvasAndWallsListView> createState() => _CanvasAndWallsListViewState();
}

class _CanvasAndWallsListViewState extends State<CanvasAndWallsListView> with TickerProviderStateMixin {

  late final TabController _tabController;
  
  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    context.read<CanvasWallBloc>().add(const LoadCanvasWall());
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final canvasWallBloc = context.watch<CanvasWallBloc>();
    
    return Scaffold(
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

      body: TabBarView(
        controller: _tabController,
        children: <Widget>[
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.only(top: 10),
              child: Column(
                children: [
                  SizedBox(
                    child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
                      builder: (context, state) {
                        return Padding(
                          padding: const EdgeInsets.all(15.0),
                          child: Column(
                            children: [
                              state.loading
                              ? Center(
                                  child: CircularProgressIndicator(color: colors.primary),
                                )
                              : const Text(
                                "Registros capturados en este teléfono que aun NO están almacenados en el sistema y necesitan ser vinculados.",
                                style: TextStyle(
                                          fontSize: 16
                                        ),
                              ),
                              const SizedBox(height: 10,),
                              Text(
                                'Total Pendientes: ${canvasWallBloc.state.canvasWallPending}', 
                                style: const TextStyle(
                                          fontSize: 25.0,
                                          fontWeight: FontWeight.w700
                                        ) 
                              ),
                            ],
                          ),
                        );
                      },
                    )
                  ),
                  Expanded(
                      child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
                      builder: (context, state) {
                        return 
                        // state.loading 
                        // ? Center(child: CircularProgressIndicator(color: colors.primary),)
                        // : 
                        canvasWallBloc.state.listCanvasWall.isEmpty || canvasWallBloc.state.canvasWallPending == 0
                          ? const NoCanvasWallSyncMessage()
                          : ListView.builder(
                            padding: const EdgeInsets.symmetric(horizontal: 16),
                            itemBuilder: (context, index) {
                              final canvasWall = canvasWallBloc.state.listCanvasWall[index];
                              return !canvasWall.isInSync 
                                      ? Column(
                                          children: [
                                            CanvasAndWallsCard(
                                                canvasWall: canvasWall, 
                                                index: index
                                              ),
                                            const SizedBox(height: 15)
                                          ],
                                        )
                                      : const SizedBox();
                            },
                            itemCount: canvasWallBloc.state.listCanvasWall.length,
                          );
                      },
                    )
                  )
                ],
              ),
            ),
          ),



          SafeArea(
            child: Padding(
              padding: const EdgeInsets.only(top: 10),
              child: Column(
                children: [
                  SizedBox(
                    child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
                      builder: (context, state) {
                        return Padding(
                          padding: const EdgeInsets.all(15.0),
                          child: Column(
                            children: [
                              state.loading
                              ? Center(
                                  child: CircularProgressIndicator(color: colors.primary),
                                )
                              : const Text(
                                "Registros capturados en este teléfono que ya están almacenados en el sistema.",
                                style: TextStyle(
                                          fontSize: 16
                                        ),
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
                    )
                  ),
                  Expanded(
                      child: BlocBuilder<CanvasWallBloc, CanvasWallState>(
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
                                final canvasWall = canvasWallBloc.state.listCanvasWall[index];
                                return canvasWall.isInSync 
                                        ? Column(
                                            children: [
                                              CanvasAndWallsCard(
                                                  canvasWall: canvasWall, 
                                                  index: index
                                                ),
                                              const SizedBox(height: 15)
                                            ],
                                          )
                                        : const SizedBox(height: 0,);
                              },
                              itemCount: canvasWallBloc.state.listCanvasWall.length,
                            );
                      },
                    )
                  )
                ],
              ),
            ),
          ),
        ],
      ),

    );
  }
}
