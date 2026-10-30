class VisitPeople{

  int? isarId;

  final String uuid;
  final String deviceId;
  final bool openTheDoor;
  final String street;
  final String externallNumber;
  final String longitude;
  final String latitude;
  final String visitNumber;
  bool syncVisit;
  final String? age;
  final String? names;
  final String? lastName;
  final String? surName;
  final String? phone;
  final bool? acceptInformation;
  final String? sex;
  final String? hasSympathy;
  final DateTime? created;
  //final bool? deliveredGazette;
  //final String? notes;
  //final String? adultsInHome;
  

  VisitPeople({
    required this.uuid,
    required this.deviceId,
    required this.openTheDoor,
    required this.street,
    required this.externallNumber,
    required this.longitude,
    required this.latitude,
    required this.visitNumber,
    required this.syncVisit,
    this.age,
    this.names,
    this.lastName,
    this.surName,
    this.phone,
    this.acceptInformation,
    this.sex,
    this.hasSympathy,
    this.created
    //this.adultsInHome,
    //this.deliveredGazette,
    // required this.notes,
  });
  
}