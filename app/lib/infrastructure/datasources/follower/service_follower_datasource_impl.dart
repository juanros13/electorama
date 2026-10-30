import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:electorama_app/config/constants/environment.dart';
import 'package:electorama_app/domain/datasources/follower/service_follower_datasource.dart';

class ServiceFollowerDatasourceImpl implements ServiceFollowerDatasource {
  late final Dio dioFollower;
  final String accessToken;

  ServiceFollowerDatasourceImpl({required this.accessToken})
      : dioFollower = Dio(BaseOptions(
          baseUrl: Environment.urlFollowers,
          headers: {'Authorization': 'Bearer $accessToken'},
        ));

  @override
  Future<bool> postFollower(
      Map<String, dynamic> followerFormData, int isarId) async {
    try {
      final response = await dioFollower.request(
        '/followers',
        data: followerFormData,
        options: Options(method: 'POST', contentType: Headers.jsonContentType),
      );
      if (response.statusCode == 200) {
        return true;
      }
      debugPrint('Follower request failed: ${response.statusCode}');
      return false;
    } catch (e) {
      debugPrint(e.toString());
      return false;
    }
  }
}
