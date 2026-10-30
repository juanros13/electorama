/// Incidencia ciudadana (encabezado) con su lista de detalles. Plana, sin Isar.
class Issue {
  final int? id;
  final String uuid;
  final String registrationDate; // yyyy-MM-dd
  final String fullName;
  final String phone;
  final int municipalityId;
  final String municipalityName;
  final int localityId;
  final String? localityName;
  final String? localityOther;
  final bool isInSync;
  final DateTime? created;
  final List<IssueDetail> details;

  const Issue({
    this.id,
    required this.uuid,
    required this.registrationDate,
    required this.fullName,
    required this.phone,
    required this.municipalityId,
    required this.municipalityName,
    this.localityId = 0,
    this.localityName,
    this.localityOther,
    this.isInSync = false,
    this.created,
    this.details = const [],
  });
}

/// Detalle de una incidencia (cada renglón del FormArray del front).
class IssueDetail {
  final int? id;
  final String uuid;
  final String issueUuid;
  final String comments;
  final bool urgent;
  final int issueTypeId;
  final String issueTypeName;
  final int issueCategoryId;
  final String issueCategoryName;
  final int issueStatusId;
  final String issueStatusName;
  // beneficiario (PROGRAMAS BIENESTAR / JCF-BECAS)
  final String? name;
  final String? lastName;
  final String? surName;
  final String? curp;
  // recurso (INSUMOS MEDICOS / APOYO VARIOS / EDUCACIÓN)
  final int? resourceId;
  final String? resourceName;
  // archivo (categorías con comprobante)
  final int? docCategoryId;
  final String? docCategoryName;
  final String? localFilePath;

  const IssueDetail({
    this.id,
    required this.uuid,
    required this.issueUuid,
    required this.comments,
    this.urgent = false,
    required this.issueTypeId,
    required this.issueTypeName,
    required this.issueCategoryId,
    required this.issueCategoryName,
    required this.issueStatusId,
    required this.issueStatusName,
    this.name,
    this.lastName,
    this.surName,
    this.curp,
    this.resourceId,
    this.resourceName,
    this.docCategoryId,
    this.docCategoryName,
    this.localFilePath,
  });
}
