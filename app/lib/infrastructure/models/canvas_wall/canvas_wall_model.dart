import 'dart:io';
import 'package:dio/dio.dart';
import 'package:http_parser/http_parser.dart';


class CanvasWallModel {

  final int municipalityId;
  final int seccionId;
  final int localityId;
  final String coordinateZoneId;
  final String referenceAuthId;
  
  final String uuid;
  final String deviceId;
  final String dateOfPlacement;
  final int canvasAndWallsProyectId;
  final int canvasAndWallsCategoryId;
  final File file;
  final String latitude;
  final String longitude;


  CanvasWallModel({
    required this.municipalityId,
    required this.seccionId,
    required this.localityId,
    required this.coordinateZoneId,
    required this.referenceAuthId,
    required this.uuid,
    required this.deviceId,
    required this.dateOfPlacement,
    required this.canvasAndWallsCategoryId,
    required this.canvasAndWallsProyectId,
    required this.file,
    required this.latitude,
    required this.longitude,
  });


  factory CanvasWallModel.fromJson(Map<String, dynamic> json) => CanvasWallModel(
    municipalityId: json["municipalityId"], 
    seccionId: json["seccionId"], 
    localityId: json["localityId"], 
    coordinateZoneId: json["coordinateZoneId"], 
    referenceAuthId: json["referenceAuthId"],
    uuid: json["uuid"],
    deviceId: json["deviceId"],
    dateOfPlacement: json["dateOfPlacement"], 
    canvasAndWallsCategoryId: json["canvasAndWallsCategoryId"], 
    canvasAndWallsProyectId: json["canvasAndWallsProyectId"], 
    file: json["file"], 
    latitude: json["latitude"], 
    longitude: json["longitude"],
  );


  Map<String, dynamic> toJson() => {
    "municipalityId": municipalityId, 
    "seccionId": seccionId, 
    "localityId": localityId, 
    "coordinateZoneId": coordinateZoneId, 
    "referenceAuthId": referenceAuthId,
    "uuid": uuid,
    "deviceId": deviceId, 
    "dateOfPlacement": dateOfPlacement, 
    "canvasAndWallsCategoryId": canvasAndWallsCategoryId, 
    "canvasAndWallsProyectId": canvasAndWallsProyectId, 
    "file": file, 
    "latitude": latitude, 
    "longitude": longitude,
  };

  Future<FormData> toFormData() async => FormData.fromMap({
    "municipalityId": municipalityId, 
    "seccionId": seccionId, 
    "localityId": localityId, 
    "coordinateZoneId": coordinateZoneId, 
    "referenceAuthId": referenceAuthId,
    "uuid": uuid,
    "deviceId": deviceId,  
    "dateOfPlacement": dateOfPlacement, 
    "canvasAndWallsCategoryId": canvasAndWallsCategoryId, 
    "canvasAndWallsProyectId": canvasAndWallsProyectId, 
    "file": await MultipartFile.fromFile(file.path, filename: 'image.jpg', contentType: MediaType('image', 'jpg')), 
    "latitude": latitude, 
    "longitude": longitude,
  });

}