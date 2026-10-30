import 'dart:typed_data';

import 'package:electorama_app/domain/entities/canvas_wall/category.dart';
import 'package:electorama_app/domain/entities/canvas_wall/project.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:electorama_app/infrastructure/inputs/general_dropdown.dart';

part 'canvas_wall_form_event.dart';
part 'canvas_wall_form_state.dart';

class CanvasWallFormBloc extends Bloc<CanvasWallFormEvent, CanvasWallFormState> {
  
  CanvasWallFormBloc() : super(CanvasWallFormInitial()) {
    on<LoadProjects>(_onLoadProjects);
    on<LoadCategories>(_onLoadCategories);
    on<LatitudeChange>(_latitudeChange);
    on<LoadingCanvasAndWalls>(_loadingCanvasAndWalls);
    on<LongitudeChange>(_longitudeChange);
    on<ProjectChange>(_onProjectChange);
    on<CategoryChange>(_onCategoryChange);
    on<DateChange>(_onDateChange);
    on<CompleteSubmitChange>(_completeSubmitChange);
    on<TakePhoto>(_onTakePhoto);
    on<SubmitForm>((event, emit) => _onSubmit(emit),);
  }

  void _onLoadProjects(LoadProjects event, Emitter<CanvasWallFormState> emit) {
    final List<CanvasWallProject> projects = [
      CanvasWallProject(id: 1, name: 'JM'),
      CanvasWallProject(id: 2, name: 'Claudia'),
    ];
    
    emit(state.copyWith(typesProjects: projects));
  }

  void _onLoadCategories(LoadCategories event, Emitter<CanvasWallFormState> emit) {
    final List<CanvasWallCategory> categories = [
      CanvasWallCategory(id: 1, name: 'LONA'),
      CanvasWallCategory(id: 2, name: 'BARDA'),
    ];

    emit(state.copyWith(typesCategories: categories));
  }

  void _latitudeChange(LatitudeChange event, Emitter<CanvasWallFormState> emit) {
    emit(state.copyWith(
      latitude: event.value,
    ));
  }
  void _longitudeChange(LongitudeChange event, Emitter<CanvasWallFormState> emit) {
    emit(state.copyWith(
      longitude: event.value,
    ));
  }

  void _onProjectChange(ProjectChange event, Emitter<CanvasWallFormState> emit) {
    final projectSelected = GeneralDropdown.dirty(event.value);

    emit(state.copyWith(
      projectSelected: projectSelected,
      isValid: validateForm(project: projectSelected.value, category: state.categorySelected.value,photo: state.photo)
    ));
  }

  void _onCategoryChange(CategoryChange event, Emitter<CanvasWallFormState> emit) {
    final categorySelected = GeneralDropdown.dirty(event.value);

    emit(state.copyWith(
      categorySelected: categorySelected,
      isValid: validateForm(project: state.projectSelected.value, category: categorySelected.value, photo: state.photo)
    ));
  }

  void _onDateChange(DateChange event, Emitter<CanvasWallFormState> emit) {
    emit(state.copyWith(
      date: event.value
    ));
  }

  void _onTakePhoto(TakePhoto event, Emitter<CanvasWallFormState> emit) {
    emit(state.copyWith(
      photo: event.value,
      loading: false,
      isValid: validateForm(project: state.projectSelected.value, category: state.categorySelected.value, photo: event.value)
    ));
  }

  void _completeSubmitChange(CompleteSubmitChange event, Emitter<CanvasWallFormState> emit) {
    emit(state.copyWith(
      completeSubmit: event.value,
    ));
  }

  void _loadingCanvasAndWalls(LoadingCanvasAndWalls event, Emitter<CanvasWallFormState> emit) {
    emit(state.copyWith(
      loading: event.value,
    ));
  }

  void _onSubmit(Emitter<CanvasWallFormState> emit) {
    print('entre a submit');
    emit(state.copyWith(
      formStatus:  FormStatus.validating,
      latitude: state.latitude,
      longitude: state.longitude,
      photo: Uint8List(0),
      isValid: false,
    ));
  }

  // void _saveCanvasWall(SaveCanvasWall event, Emitter<CanvasWallFormState> emit) async {
    
  // }

  bool validateForm({
    String? project,
    String? category,
    Uint8List? photo,
  }) {

    project = (project == null) ? state.projectSelected.value : project;
    category = (category == null) ? state.categorySelected.value : category;
    photo = (photo == null) ? state.photo : photo;

    if(project != '' && category != '' && photo != null) {
      return true;
    }

    return false;
  }

}