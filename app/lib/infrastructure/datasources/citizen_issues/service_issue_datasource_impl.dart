import 'dart:io';

import 'package:dio/dio.dart';

import 'package:electorama_app/config/constants/environment.dart';
import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';
import 'package:electorama_app/infrastructure/models/citizen_issues/issue_model.dart';

/// Sincroniza incidencias contra el backend: `POST /issue` y luego sube los
/// archivos locales a `/dropbox/upload` casándolos por uuid del detalle.
class ServiceIssueDatasource {
  final String accessToken;
  final Dio _dio;

  ServiceIssueDatasource({required this.accessToken})
      : _dio = Dio(BaseOptions(
          baseUrl: Environment.urlCitizenIssues,
          headers: {'Authorization': 'Bearer $accessToken'},
          contentType: 'application/json',
        ));

  Future<void> sendIssue(Issue issue) async {
    final resp = await _dio.post('/issue', data: IssueModel.toJson(issue));
    final created = resp.data as Map<String, dynamic>;
    final createdDetails =
        (created['issueDetailEntities'] as List<dynamic>?) ?? const [];

    for (final d in issue.details) {
      final path = d.localFilePath;
      if (path == null) continue;
      final file = File(path);
      if (!file.existsSync()) continue;

      final match = createdDetails.cast<Map<String, dynamic>?>().firstWhere(
            (cd) => cd != null && cd['uuid'] == d.uuid,
            orElse: () => null,
          );
      if (match == null) continue;

      final form = FormData.fromMap({
        'file': await MultipartFile.fromFile(
          file.path,
          filename: 'comprobante${_ext(file.path)}',
        ),
        'IssueDetailId': '${match['id']}',
        'IssueSolutionDocumentsCategory': '${d.docCategoryId ?? ''}',
        'uuid': d.uuid,
      });
      await _dio.post('/dropbox/upload', data: form);
    }
  }

  String _ext(String path) {
    final i = path.lastIndexOf('.');
    return i >= 0 ? path.substring(i) : '.jpg';
  }
}
