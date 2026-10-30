import 'dart:convert';

import 'package:electorama_app/config/config.dart';
import 'package:electorama_app/domain/datasources/visits/visited_datasource.dart';
import 'package:dio/dio.dart';
import 'package:electorama_app/domain/repositories/storage/local_storage_repository.dart';
import 'package:electorama_app/infrastructure/datasources/visits/local_storage_visit_datasource_impl.dart';
import 'package:electorama_app/infrastructure/repositories/storage/local_storage_repository_impl.dart';

class VisitedDataSourceImpl implements VisitedDataSource {
  
  LocalStorageRepository localStorageRepository = LocalStorageRepositoryImpl(LocalStorageVisitDataSourceImpl());
  // late final Dio dioUser;
  late final Dio dioFollowers;
  final String accessToken;

  VisitedDataSourceImpl({
    required this.accessToken
  }): dioFollowers = Dio(
    BaseOptions(
      baseUrl: Environment.urlFollowers,
      headers: {
        'Authorization': 'Bearer $accessToken'
      }
    )
  );
  
  @override
  Future<void> postVisited(Map<String, dynamic> visitedJson, int isarId) async {

    print(visitedJson);
    String jsonstringmap = json.encode(visitedJson);
    print(jsonstringmap);
    try {
      final response = await dioFollowers.put('/visited/createVisited',
        data: visitedJson,
        options: Options(contentType: Headers.jsonContentType)
      );
      if (response.statusCode == 200) {
        print(response);
        localStorageRepository.syncVisit(isarId);
        // return response;
      } else {
        print('Request failed with status code: ${response.statusCode}');
      }
    } catch (e) {
      print(e);
    }
  }


}