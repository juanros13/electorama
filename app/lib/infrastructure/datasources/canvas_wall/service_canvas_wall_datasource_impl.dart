import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';

import 'package:electorama_app/config/constants/environment.dart';
import 'package:electorama_app/domain/datasources/canvas_wall/service_canvas_wall_datasource.dart';

class ServiceCanvasWallDatasourceImpl implements ServiceCanvasWallDatasource {

  late final Dio dioCanvasWall;
  final String accessToken;

  ServiceCanvasWallDatasourceImpl({
    required this.accessToken
  }): dioCanvasWall = Dio(
    BaseOptions(
      baseUrl: Environment.urlCanvasWall,
      headers: {
        'Authorization': 'Bearer $accessToken',
        'Accept': 'application/json, text/plain, */*',
        "Content-Type": "multipart/form-data; boundary=----"
      }
    )
  );


  @override
  Future<bool> postCanvasWall(FormData canvasWallFormData, int isarId) async {
    
    try {

      final response = await dioCanvasWall.request('/canvasAndWalls',
        data: canvasWallFormData,
        options: Options(
          method: 'PUT',
          followRedirects: false,
          maxRedirects: 0,
          validateStatus: (status) { return status! < 500; },
        ),
      );

      if(response.statusCode == 200) {
        return true;
      } else {
        debugPrint('Request failed with status code: ${response.statusCode}');
        return false;
      }

    } catch (e) {
      debugPrint(e.toString());
      return false;
    }

  }


}