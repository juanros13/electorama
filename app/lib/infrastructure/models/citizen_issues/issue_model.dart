import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';

/// Serializa una [Issue] al `IssueDTO` que espera `POST /issue`.
class IssueModel {
  static Map<String, dynamic> toJson(Issue issue) => {
        'registrationDate': issue.registrationDate,
        'fullName': issue.fullName,
        'phone': issue.phone,
        'municipalityId': issue.municipalityId,
        'localityId': issue.localityId,
        'localityOther': issue.localityOther ?? '',
        'issueDetailEntities': issue.details.map(_detail).toList(),
      };

  static Map<String, dynamic> _detail(IssueDetail d) {
    final m = <String, dynamic>{
      'comments': d.comments,
      'issueType': {'id': d.issueTypeId, 'name': d.issueTypeName},
      'issueCategory': {'id': d.issueCategoryId, 'name': d.issueCategoryName},
      'issueStatus': {'id': d.issueStatusId, 'name': d.issueStatusName},
      'urgent': d.urgent,
      'uuid': d.uuid,
    };
    if (d.name != null) m['name'] = d.name;
    if (d.lastName != null) m['lastName'] = d.lastName;
    if (d.surName != null) m['surName'] = d.surName;
    if (d.curp != null) m['curp'] = d.curp;
    if (d.resourceId != null) {
      m['issueSolutionResourceTmp'] = {
        'id': d.resourceId,
        'name': d.resourceName,
        'issueCategory': {'id': d.issueCategoryId, 'name': d.issueCategoryName},
      };
    }
    if (d.docCategoryId != null) {
      m['issueSolutionDocumentsCategory'] = {
        'id': d.docCategoryId,
        'name': d.docCategoryName,
      };
    }
    return m;
  }
}
