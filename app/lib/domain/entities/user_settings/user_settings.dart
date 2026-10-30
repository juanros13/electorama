class UserSettings {
  int? id;
  MunicipalitySelected municipality;
  List<SectionsSelected> sectionsSelected;

  UserSettings({
    required this.municipality,
    this.id,
    this.sectionsSelected = const [],
  });
}

class MunicipalitySelected {
  int? idMun;
  String? nomMun;
  MunicipalitySelected({this.idMun, this.nomMun});
}

class SectionsSelected {
  int? idSec;
  String? nomSec;
  SectionsSelected({this.idSec, this.nomSec});
}
