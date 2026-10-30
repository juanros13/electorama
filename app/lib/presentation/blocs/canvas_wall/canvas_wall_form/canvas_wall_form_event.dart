part of 'canvas_wall_form_bloc.dart';

abstract class CanvasWallFormEvent extends Equatable {
  const CanvasWallFormEvent(); 

  @override
  List<Object> get props => [];
}

class LoadProjects extends CanvasWallFormEvent {
  const LoadProjects();
}

class LoadCategories extends CanvasWallFormEvent {
  const LoadCategories();
}

class LatitudeChange extends CanvasWallFormEvent {
  final String value;
  const LatitudeChange(this.value);
}

class LongitudeChange extends CanvasWallFormEvent {
  final String value;
  const LongitudeChange(this.value);
}

class ProjectChange extends CanvasWallFormEvent {
  final String value;
  const ProjectChange(this.value);
}

class CategoryChange extends CanvasWallFormEvent {
  final String value;
  const CategoryChange(this.value);
}

class DateChange extends CanvasWallFormEvent {
  final String value;
  const DateChange(this.value);
}

class SubmitForm extends CanvasWallFormEvent {
  const SubmitForm();
}

class CompleteSubmitChange extends CanvasWallFormEvent {
  final bool value;
  const CompleteSubmitChange(this.value);
}

class TakePhoto extends CanvasWallFormEvent {
  final Uint8List value;
  const TakePhoto(this.value);
}

class LoadingCanvasAndWalls extends CanvasWallFormEvent {
  final bool value;
  const LoadingCanvasAndWalls(this.value);
}