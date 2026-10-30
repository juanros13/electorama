import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/presentation/blocs/blocs.dart';
import 'package:electorama_app/presentation/blocs/visits/visits_people/visits_people_bloc.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
//import 'package:awesome_snackbar_content/awesome_snackbar_content.dart';
import 'package:electorama_app/config/theme/app_colors.dart';
import 'package:electorama_app/presentation/blocs/auth/auth_bloc.dart';
import 'package:electorama_app/presentation/widgets/forms/visits/visit_form.dart';
import 'package:electorama_app/presentation/widgets/shared/login_to_sync_hint.dart';
import 'package:electorama_app/presentation/widgets/shared/menus/side_menu.dart';
import 'package:electorama_app/presentation/widgets/shared/module_header.dart';

enum SampleItem { itemOne, itemTwo, itemThree }

class VisitListSyncView extends StatefulWidget {
  const VisitListSyncView({super.key});

  @override
  State<VisitListSyncView> createState() => _VisitListSyncViewState();
}

class _VisitListSyncViewState extends State<VisitListSyncView> {
  @override
  void initState() {
    context.read<VisitsPeopleBloc>().add(const LoadVisitPeople());
    context.read<VisitsBloc>().state;
    // context.read<VisitsBloc>();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final scaffoldKey = GlobalKey<ScaffoldState>();
    final isAuth = context.select<AuthBloc, bool>(
        (b) => b.state.authStatus == AuthStatus.authenticated);

    return Scaffold(
      drawer: SideMenu( scaffoldKey: scaffoldKey ),
      appBar: AppBar(
        
        centerTitle: true,
        title: const Text('Sincronizar Visitas'),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Column(
            children: [
              const Padding(
                padding: EdgeInsets.fromLTRB(10, 0, 10, 10),
                child: ModuleHeader(
                  icon: Icons.how_to_reg,
                  title: 'Visitas',
                  subtitle: 'Recorrido territorial · puerta por puerta',
                  accent: AppColors.warning,
                ),
              ),
              if (isAuth)
                const VisitForm()
              else
                const LoginToSyncHint(
                    message: 'Inicia sesión para sincronizar tus visitas.'),
              //SlidableList(name: 'HOLA MUNDO',)
              // Align(
              //   alignment: Alignment.centerRight,
              //   child: Padding(
              //     padding: const EdgeInsets.symmetric(horizontal: 10),
              //     child: FilledButton(
              //       child: const Text('Iniciar Sincronización'),
              //       onPressed: () {
              //         showConfirmDialog(
              //             context,
              //             'Sincronización',
              //             '¿Deseas comenzar la sincronización de visitas?',
              //             'Cancelar',
              //             'Si, Continuar');
              //       },
              //     ),
              //   ),
              // ),
              const SizedBox(height: 10),
              SizedBox(child: BlocBuilder<VisitsBloc, VisitsState>(
                builder: (context, state) {
                  return state.loading
                      ? Center(
                          child:
                              CircularProgressIndicator(color: colors.primary),
                        )
                      : Text(
                          'Total de Visitas: ${state.listVisitPeople.length}',
                          style: Theme.of(context).textTheme.headlineSmall);
                },
              )),
              
              // Expanded(child: BlocBuilder<VisitsPeopleBloc, VisitsPeopleState>(
              //   builder: (context, state) {
              //     return state.loading
              //         ? Center(
              //             child:
              //                 CircularProgressIndicator(color: colors.primary),
              //           )
              //         : visitBloc.state.listVisitPeople.isEmpty
              //             ? const Center(
              //                 child: Text('No hay registros por sincronizar'),
              //               )
              //             : ListView.separated(
              //                 itemBuilder: (context, index) {
              //                   final visitPeople =
              //                       visitBloc.state.listVisitPeople[index];

              //                   return !visitPeople.syncVisit 
              //                     ? 
              //                       VisitedPeopleCard(
              //                         visitPeople: visitPeople,
              //                         index: index,
              //                       )
              //                     :
              //                       const SizedBox();
              //                 },
              //                 itemCount: visitBloc.state.listVisitPeople.length,
              //                 separatorBuilder: (context, index) =>
              //                     const SizedBox(height: 10),
              //               );
              //   },
              // ))
            ],
          ),
        ),
      ),
    );
  }

//   Widget _visitedPeople(VisitPeople visitPeople) {
//     final colors = Theme.of(context).colorScheme;
//     return FadeInRight(
//         from: 0,
//         duration: const Duration(milliseconds: 1200),
//         child: Container(
//           padding: const EdgeInsets.all(15.0),
//           decoration: BoxDecoration(
//             color: const Color(0xFFFEFFFF),
//             borderRadius: BorderRadius.circular(10.0),
//             boxShadow: [
//               BoxShadow(
//                 color: Theme.of(context).primaryColor.withOpacity(.1),
//                 offset: const Offset(.1, 1),
//                 blurRadius: 10.0,
//               )
//             ],
//           ),
//           child: Column(
//             crossAxisAlignment: CrossAxisAlignment.stretch,
//             children: [
//               Column(
//                 crossAxisAlignment: CrossAxisAlignment.stretch,
//                 children: [
//                   RichText(
//                     text: TextSpan(
//                       text: 'Calle: ${visitPeople.street}',
//                       style: TextStyle(
//                           color: Theme.of(context).primaryColor,
//                           fontSize: 16.0,
//                           fontWeight: FontWeight.w700),
//                     ),
//                   ),
//                   RichText(
//                     text: TextSpan(
//                       text: 'Numero:  ${visitPeople.internalNumber}',
//                       style: TextStyle(
//                           color: Theme.of(context).primaryColor,
//                           fontSize: 16.0,
//                           fontWeight: FontWeight.w700),
//                     ),
//                   ),
//                   RichText(
//                     text: TextSpan(
//                       text:
//                           'Abrio la puerta: ${visitPeople.openTheDoor ? 'Si' : 'No'}',
//                       style: TextStyle(
//                           color: Theme.of(context).primaryColor,
//                           fontSize: 16.0,
//                           fontWeight: FontWeight.w700),
//                     ),
//                   ),
//                   (visitPeople.names != null)
//                       ? RichText(
//                           text: TextSpan(
//                             text:
//                                 'Nombre:  ${visitPeople.names} ${visitPeople.lastName} ${visitPeople.surName}',
//                             style: TextStyle(
//                                 color: Theme.of(context).primaryColor,
//                                 fontSize: 20.0,
//                                 fontWeight: FontWeight.w700),
//                           ),
//                         )
//                       : const SizedBox(),
//                 ],
//               ),
//               const SizedBox(height: 10.0),
//               Row(
//                 mainAxisAlignment: MainAxisAlignment.spaceBetween,
//                 children: [
//                   Row(
//                     children: [
//                       Icon(Icons.sync, color: colors.secondary),
//                       const Text(' Registro por sincronizar'),
//                     ],
//                   ),
//                   IconButton(
//                       onPressed: () {
//                         _deleteVisitesPeopleDialog(visitPeople);
//                       },
//                       icon:
//                           const Icon(Icons.delete, color: Colors.red, size: 30))
//                 ],
//               )
//             ],
//           ),
//         ));
//   }

//   Future<String?> _deleteVisitesPeopleDialog(VisitPeople visitPeople) {
//     // print(visitPeople.isarId);

//     final int visitPeopleID = visitPeople.isarId!;

//     return showDialog<String>(
//       context: context,
//       builder: (BuildContext context) => AlertDialog(
//         title: const Text('¿Estás seguro?'),
//         content: const Text(
//             'La visita aun no ha sido sincronizada en el sistema. ¿Deseas eliminarla?'),
//         actions: <Widget>[
//           TextButton(
//             child: const Text('Cancelar'),
//             onPressed: () {
//               Navigator.pop(context, 'Cancelar');
//             },
//           ),
//           TextButton(
//             child: const Text('Eliminar'),
//             onPressed: () {
//               Navigator.pop(context, 'Eliminar');
//               context
//                   .read<VisitsPeopleBloc>()
//                   .add(DeleteVisitPeople(isarId: visitPeopleID));
//               // print(context)
//               // print(context);
//             },
//           ),
//         ],
//       ),
//     );
//   }
// }

  Future<void> showConfirmDialog(
      BuildContext context,
      String textTitle,
      String textContent,
      String textButtonCancel,
      String textButtonConfirm) async {
    final counterBloc = BlocProvider.of<VisitsPeopleBloc>(context);

    final readVisitsPeopleBloc = context.read<VisitsPeopleBloc>();

    final listVisitPeople = counterBloc.state.listVisitPeople;

    // final scaffoldContainer = ScaffoldMessenger.of(context);

    bool visitsFlags = false;

    return showDialog<void>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(textTitle),
          content: Text(textContent),
          actions: <Widget>[
            TextButton(
              child: Text(textButtonCancel),
              onPressed: () {
                // Acción al presionar el botón "Cancelar"
                Navigator.of(context).pop();
              },
            ),
            TextButton(
              child: Text(textButtonConfirm),
              onPressed: () {
                // Acción al presionar el botón "Aceptar"
                Navigator.of(context).pop();
                EasyLoading.show(
                    status: 'Sincronizando',
                    maskType: EasyLoadingMaskType.black,
                    dismissOnTap: false);

                Future.delayed(const Duration(seconds: 3), () async {
                  for (final visit in listVisitPeople) {
                    final int visitPeopleID = visit.isarId!;
                    await Future.delayed(const Duration(seconds: 1));
                    readVisitsPeopleBloc
                        .add(DeleteVisitPeople(isarId: visitPeopleID));
                    visitsFlags = true;
                  }

                  EasyLoading.dismiss();

                  if (visitsFlags) {
                    // AdvanceSnackBar(
                    //     message: "Notification Message",
                    //     mode: Mode.ADVANCE,
                    //     duration: const Duration(seconds: 5),
                    //     type: sType.SUCCESS,
                    //     textSize: 20,
                    //     isIcon: true,
                    //     iconPosition: IconPosition.RIGHT,
                    // ).show(context);

                    // final snackBar = SnackBar(
                    //   /// need to set following properties for best effect of awesome_snackbar_content
                    //   elevation: 0,
                    //   behavior: SnackBarBehavior.floating,
                    //   backgroundColor: Colors.transparent,
                    //   content: AwesomeSnackbarContent(
                    //     title: 'Proceso Exitoso',
                    //     message:
                    //         'Tus visitas han sido sincronizadas de forma correcta al sistema.',

                    //     /// change contentType to ContentType.success, ContentType.warning or ContentType.help for variants
                    //     contentType: ContentType.success,
                    //   ),
                    // );

                    // scaffoldContainer
                    //   ..hideCurrentSnackBar()
                    //   ..showSnackBar(snackBar);

                    visitsFlags = false;
                  }
                });
              },
            ),
          ],
        );
      },
    );
  }
}
