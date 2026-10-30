import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people/visits_people_bloc.dart';

enum SampleItem { itemOne, itemTwo, itemThree }

class VisitListView extends StatefulWidget {
  const VisitListView({super.key});

  @override
  State<VisitListView> createState() => _VisitListViewState();
}

class _VisitListViewState extends State<VisitListView> {
  
  @override
  void initState() {
    context.read<VisitsPeopleBloc>().add(const LoadVisitPeople());
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('Listado de Visitas'),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Column(
            children: [
              //SlidableList(name: 'HOLA MUNDO',)
              SizedBox(
                
                child: BlocBuilder<VisitsPeopleBloc, VisitsPeopleState>(
                  builder: (context, state) {
                    return state.loading 
                      ? Center(child: CircularProgressIndicator(color: colors.primary),)
                      : Text('Total de Visitas: ${state.listVisitPeople.length}', style: TextStyle(
                          color: Theme.of(context).primaryColor,
                          fontSize: 25.0,
                          fontWeight: FontWeight.w700) );
                  },
                )
              ),
              // Expanded(
              //     child: BlocBuilder<VisitsPeopleBloc, VisitsPeopleState>(
              //     builder: (context, state) {
              //       return 
              //       // state.loading 
              //       // ? Center(child: CircularProgressIndicator(color: colors.primary),)
              //       // : 
              //       visitBloc.state.listVisitPeople.isEmpty
              //         ? const Center(child: Text('No hay registros'),)
              //         : ListView.separated(
              //           itemBuilder: (context, index) {
              //             final visitPeople = visitBloc.state.listVisitPeople[index];
              //             return CanvasAndWallsCard(canvasWall: visitPeople, index: index);
              //           },
              //           itemCount: visitBloc.state.listVisitPeople.length,
              //           separatorBuilder: (context, index) =>
              //               const SizedBox(height: 10),
              //         );
              //     },
              //   )
              // )
            ],
          ),
        ),
      ),
    );
  }
}
