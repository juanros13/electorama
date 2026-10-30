import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_easyloading/flutter_easyloading.dart';
import 'package:animate_do/animate_do.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:electorama_app/domain/entities/canvas_wall/canvas_wall.dart';
import 'package:electorama_app/infrastructure/datasources/canvas_wall/canvas_wall_mapper.dart';
import 'package:electorama_app/infrastructure/services/camera_gallery_service_impl.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall/canvas_wall_bloc.dart';
import 'package:electorama_app/presentation/blocs/canvas_wall/canvas_wall_form/canvas_wall_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/shared/inputs/custom_dropdown_form_field.dart';
import 'package:electorama_app/presentation/widgets/widgets.dart';

class CanvasWallForm extends StatelessWidget {
  const CanvasWallForm({super.key});

  @override
  Widget build(BuildContext context) {
    context.read<CanvasWallFormBloc>().add(const LoadProjects());
    context.read<CanvasWallFormBloc>().add(const LoadCategories());
    final canvasWallFormBloc = context.watch<CanvasWallFormBloc>();
    final canvasWallBloc = context.watch<CanvasWallBloc>();

    final projectsOptions = canvasWallFormBloc.state.typesProjects;
    final projectSelected = canvasWallFormBloc.state.projectSelected;

    final categoriesOptions = canvasWallFormBloc.state.typesCategories;
    final categorySelected = canvasWallFormBloc.state.categorySelected;

    return Visibility(
        child: FadeIn(
            child: Form(
                child: Padding(
      padding: const EdgeInsets.symmetric(horizontal: 10),
      child: Column(
        children: [
          const SizedBox(height: 5),
          const CardGeo(),
          const SizedBox(height: 20),
          CustomDropdownFormField(
            iconData: Icons.assignment,
            dropdownItems: projectsOptions
                .map((project) => DropdownMenuItem<String>(
                    value: project.id.toString(), child: Text(project.name)))
                .toList(),
            obscureText: false,
            isPure: true,
            onChange: (value) {
              if (value != null) {
                canvasWallFormBloc.add(ProjectChange(value));

                return projectSelected.value;
              }
            },
          ),
          const SizedBox(height: 10),
          CustomDropdownFormField(
            iconData: Icons.collections,
            dropdownItems: categoriesOptions
                .map((category) => DropdownMenuItem<String>(
                    value: category.id.toString(), child: Text(category.name)))
                .toList(),
            obscureText: false,
            isPure: true,
            onChange: (value) {
              if (value != null) {
                canvasWallFormBloc.add(CategoryChange(value));
                return categorySelected.value;
              }
            },
          ),
          const SizedBox(height: 10),
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Card(
                  clipBehavior: Clip.hardEdge,
                  child: InkWell(
                    // splashColor: AppTheme().getTheme().primaryColor,
                    splashColor:
                        Theme.of(context).colorScheme.primary.withAlpha(30),
                    onTap: () async {
                      debugPrint('Card Tapped');
                      final photopath =
                          await CameraGalleryServiceImpl().takePhoto();
                      if (photopath == null) return;
                      EasyLoading.show(
                          status: 'Cargando',
                          maskType: EasyLoadingMaskType.black,
                          dismissOnTap: false);
                      canvasWallFormBloc.add(const LoadingCanvasAndWalls(true));
                      File file = File(photopath);
                      final photo = await CameraGalleryServiceImpl()
                          .compressAndGetFile(file);
                      canvasWallFormBloc.add(TakePhoto(photo!));
                      CameraGalleryServiceImpl().deleteCacheDir();
                      EasyLoading.dismiss();
                      photopath;
                    },
                    child: const SizedBox(
                      width: double.infinity,
                      child: Padding(
                        padding: EdgeInsets.symmetric(vertical: 10),
                        child: Column(
                          children: [
                            Icon(
                              Icons.photo_camera,
                              size: 50,
                            ),
                            Text(
                              'Tomar Fotografía',
                              style: TextStyle(
                                  fontWeight: FontWeight.bold, fontSize: 18),
                            )
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ),
              Expanded(
                child: Card(
                  clipBehavior: Clip.hardEdge,
                  child: InkWell(
                    // splashColor: AppTheme().getTheme().primaryColor,
                    splashColor:
                        Theme.of(context).colorScheme.primary.withAlpha(30),
                    onTap: () async {
                      debugPrint('Card Tapped');
                      final photopath =
                          await CameraGalleryServiceImpl().selectPhoto();

                      if (photopath == null) return;
                      EasyLoading.show(
                          status: 'Cargando',
                          maskType: EasyLoadingMaskType.black,
                          dismissOnTap: false);

                      canvasWallFormBloc.add(const LoadingCanvasAndWalls(true));
                      File file = File(photopath);
                      final photo = await CameraGalleryServiceImpl()
                          .compressAndGetFile(file);
                      canvasWallFormBloc.add(TakePhoto(photo!));
                      EasyLoading.dismiss();
                      photopath;
                    },
                    child: const SizedBox(
                      width: double.infinity,
                      child: Padding(
                        padding: EdgeInsets.symmetric(vertical: 10),
                        child: Column(
                          children: [
                            Icon(
                              Icons.photo,
                              size: 50,
                            ),
                            Text(
                              'Galería',
                              style: TextStyle(
                                  fontWeight: FontWeight.bold, fontSize: 18),
                            )
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(
            height: 10,
          ),
          canvasWallFormBloc.state.photo != null &&
                  canvasWallFormBloc.state.photo!.isNotEmpty
              ? ClipRRect(
                  borderRadius: const BorderRadius.all(Radius.circular(20)),
                  child: Image.memory(
                    canvasWallFormBloc.state.photo!,
                    fit: BoxFit.cover,
                  ),
                )
              : const SizedBox(
                  height: 10,
                ),
          const SizedBox(height: 25),
          FilledButton.icon(
              onPressed: canvasWallFormBloc.state.isValid
                  ? () async {
                      FocusManager.instance.primaryFocus?.unfocus();
                      _openConfirmDialog(
                          context, canvasWallBloc, canvasWallFormBloc);
                    }
                  : null,
              icon: const Icon(Icons.save),
              label: const Text('Guardar')),
        ],
      ),
    ))));
  }

  Future<String?> _openConfirmDialog(
    BuildContext context,
    CanvasWallBloc canvasWallBloc,
    CanvasWallFormBloc canvasWallFormBloc,
    //List<TextEditingController> textsEditing
  ) {
    return showDialog<String>(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        title: const Text('¿Estás seguro?'),
        actions: <Widget>[
          TextButton(
            onPressed: () {
              Navigator.pop(context, 'Cancelar');
            },
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () async {
              EasyLoading.show(
                  status: 'Cargando',
                  maskType: EasyLoadingMaskType.black,
                  dismissOnTap: false);
              final CanvasWall canvasWall =
                  await CanvasWallMapper.castToEntity(canvasWallFormBloc.state);
              canvasWallFormBloc.add(const SubmitForm());
              canvasWallBloc.add(SaveCanvasWall(canvasWall: canvasWall));
              EasyLoading.dismiss();
              if (context.mounted) Navigator.pop(context, 'Guardar');
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }
}
