part of 'canvas_wall_form_bloc.dart';

enum FormStatus { invalid, valid, validating, posting }

enum Projects { claudia, jm }

enum Categories { lona, barda }


class CanvasWallFormState extends Equatable {

  final bool isValid;
  final bool loading;
  final FormStatus formStatus;
  final List<CanvasWallProject> typesProjects;
  final List<CanvasWallCategory> typesCategories;
  final String latitude;
  final String longitude;
  final GeneralDropdown projectSelected;
  final GeneralDropdown categorySelected;
  final String date;
  final bool completeSubmit;
  final Uint8List? photo;


  const CanvasWallFormState({
    this.isValid = false,
    this.loading = false,
    this.formStatus = FormStatus.invalid,
    this.typesProjects = const [],
    this.typesCategories = const [],
    this.latitude = '',
    this.longitude = '',
    this.projectSelected = const GeneralDropdown.pure(),
    this.categorySelected = const GeneralDropdown.pure(),
    this.date = '',
    this.completeSubmit = false,
    this.photo
  });


  CanvasWallFormState copyWith({
    bool? isValid,
    FormStatus? formStatus,
    List<CanvasWallProject>? typesProjects,
    List<CanvasWallCategory>?  typesCategories,
    String? latitude,
    String? longitude,
    GeneralDropdown? projectSelected,
    GeneralDropdown? categorySelected,
    String? date,
    bool? completeSubmit,
    Uint8List? photo,
    bool? loading,
  }) => CanvasWallFormState(
    isValid: isValid ?? this.isValid,
    formStatus: formStatus ?? this.formStatus,
    typesProjects: typesProjects ?? this.typesProjects,
    typesCategories: typesCategories ?? this.typesCategories,
    latitude: latitude ?? this.latitude,
    longitude: longitude ?? this.longitude,
    projectSelected: projectSelected ?? this.projectSelected,
    categorySelected: categorySelected ?? this.categorySelected,
    date: date ?? this.date,
    completeSubmit: completeSubmit ?? this.completeSubmit,
    photo: photo ?? this.photo,
    loading: loading ?? this.loading
  );


  @override
  List<Object?> get props => [
    isValid,
    formStatus,
    typesProjects,
    typesCategories,
    latitude,
    longitude,
    projectSelected,
    categorySelected,
    date,
    completeSubmit,
    photo,
    loading
  ];
}

class CanvasWallFormInitial extends CanvasWallFormState {}