import 'package:drift/drift.dart';

import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';
import 'package:electorama_app/infrastructure/datasources/storage/app_database.dart';

/// Persistencia offline de incidencias ciudadanas en Drift (encabezado +
/// detalles). Usa el singleton `appDatabase`.
class IssueLocalDatasource {
  final AppDatabase _db = appDatabase;

  Future<void> saveIssue(Issue issue) async {
    await _db.transaction(() async {
      await _db.into(_db.issues).insert(IssuesCompanion.insert(
            uuid: issue.uuid,
            registrationDate: issue.registrationDate,
            fullName: issue.fullName,
            phone: issue.phone,
            municipalityId: issue.municipalityId,
            municipalityName: issue.municipalityName,
            localityId: Value(issue.localityId),
            localityName: Value(issue.localityName),
            localityOther: Value(issue.localityOther),
            created: Value(issue.created),
          ));
      for (final d in issue.details) {
        await _db.into(_db.issueDetails).insert(IssueDetailsCompanion.insert(
              uuid: d.uuid,
              issueUuid: issue.uuid,
              comments: d.comments,
              urgent: Value(d.urgent),
              issueTypeId: d.issueTypeId,
              issueTypeName: d.issueTypeName,
              issueCategoryId: d.issueCategoryId,
              issueCategoryName: d.issueCategoryName,
              issueStatusId: d.issueStatusId,
              issueStatusName: d.issueStatusName,
              name: Value(d.name),
              lastName: Value(d.lastName),
              surName: Value(d.surName),
              curp: Value(d.curp),
              resourceId: Value(d.resourceId),
              resourceName: Value(d.resourceName),
              docCategoryId: Value(d.docCategoryId),
              docCategoryName: Value(d.docCategoryName),
              localFilePath: Value(d.localFilePath),
            ));
      }
    });
  }

  Future<List<Issue>> loadIssues() async {
    final issueRows = await _db.select(_db.issues).get();
    final detailRows = await _db.select(_db.issueDetails).get();
    return issueRows.map((r) {
      final details = detailRows
          .where((d) => d.issueUuid == r.uuid)
          .map(_toDetail)
          .toList();
      return _toIssue(r, details);
    }).toList();
  }

  Future<void> markSynced(String uuid) async {
    await (_db.update(_db.issues)..where((t) => t.uuid.equals(uuid)))
        .write(const IssuesCompanion(isInSync: Value(true)));
  }

  Future<void> deleteIssue(String uuid) async {
    await _db.transaction(() async {
      await (_db.delete(_db.issueDetails)
            ..where((t) => t.issueUuid.equals(uuid)))
          .go();
      await (_db.delete(_db.issues)..where((t) => t.uuid.equals(uuid))).go();
    });
  }

  Issue _toIssue(IssueRow r, List<IssueDetail> details) => Issue(
        id: r.id,
        uuid: r.uuid,
        registrationDate: r.registrationDate,
        fullName: r.fullName,
        phone: r.phone,
        municipalityId: r.municipalityId,
        municipalityName: r.municipalityName,
        localityId: r.localityId,
        localityName: r.localityName,
        localityOther: r.localityOther,
        isInSync: r.isInSync,
        created: r.created,
        details: details,
      );

  IssueDetail _toDetail(IssueDetailRow d) => IssueDetail(
        id: d.id,
        uuid: d.uuid,
        issueUuid: d.issueUuid,
        comments: d.comments,
        urgent: d.urgent,
        issueTypeId: d.issueTypeId,
        issueTypeName: d.issueTypeName,
        issueCategoryId: d.issueCategoryId,
        issueCategoryName: d.issueCategoryName,
        issueStatusId: d.issueStatusId,
        issueStatusName: d.issueStatusName,
        name: d.name,
        lastName: d.lastName,
        surName: d.surName,
        curp: d.curp,
        resourceId: d.resourceId,
        resourceName: d.resourceName,
        docCategoryId: d.docCategoryId,
        docCategoryName: d.docCategoryName,
        localFilePath: d.localFilePath,
      );
}
