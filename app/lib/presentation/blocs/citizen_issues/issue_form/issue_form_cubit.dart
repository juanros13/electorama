import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:uuid/uuid.dart';

import 'package:electorama_app/domain/entities/citizen_issues/issue.dart';
import 'package:electorama_app/domain/entities/citizen_issues/issue_catalogs.dart';

const _uuid = Uuid();

final _curpRegex = RegExp(
    r'^[A-Z][AEIOU][A-Z]{2}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}(?:[0-9]|[A-Z])[0-9]$');

/// Borrador de un detalle de incidencia (un renglón del arreglo).
class IssueDetailDraft extends Equatable {
  final String uuid;
  final CatItem? type;
  final CatItem? status;
  final CatItem? category;
  final String comments;
  final bool urgent;
  final String? name;
  final String? lastName;
  final String? surName;
  final String? curp;
  final IssueResource? resource;
  final CatItem? docCategory;
  final String? localFilePath;

  IssueDetailDraft({
    String? uuid,
    this.type,
    this.status,
    this.category,
    this.comments = '',
    this.urgent = false,
    this.name,
    this.lastName,
    this.surName,
    this.curp,
    this.resource,
    this.docCategory,
    this.localFilePath,
  }) : uuid = uuid ?? _uuid.v4();

  IssueDetailDraft copyWith({
    CatItem? type,
    CatItem? status,
    CatItem? category,
    String? comments,
    bool? urgent,
    String? Function()? name,
    String? Function()? lastName,
    String? Function()? surName,
    String? Function()? curp,
    IssueResource? Function()? resource,
    CatItem? Function()? docCategory,
    String? Function()? localFilePath,
  }) =>
      IssueDetailDraft(
        uuid: uuid,
        type: type ?? this.type,
        status: status ?? this.status,
        category: category ?? this.category,
        comments: comments ?? this.comments,
        urgent: urgent ?? this.urgent,
        name: name != null ? name() : this.name,
        lastName: lastName != null ? lastName() : this.lastName,
        surName: surName != null ? surName() : this.surName,
        curp: curp != null ? curp() : this.curp,
        resource: resource != null ? resource() : this.resource,
        docCategory: docCategory != null ? docCategory() : this.docCategory,
        localFilePath:
            localFilePath != null ? localFilePath() : this.localFilePath,
      );

  @override
  List<Object?> get props => [
        uuid,
        type,
        status,
        category,
        comments,
        urgent,
        name,
        lastName,
        surName,
        curp,
        resource,
        docCategory,
        localFilePath,
      ];
}

class IssueFormState extends Equatable {
  final String fullName;
  final String phone;
  final IssueMunicipality? municipality;
  final String localityOther;
  final List<IssueDetailDraft> details;

  const IssueFormState({
    this.fullName = '',
    this.phone = '',
    this.municipality,
    this.localityOther = '',
    this.details = const [],
  });

  IssueFormState copyWith({
    String? fullName,
    String? phone,
    IssueMunicipality? Function()? municipality,
    String? localityOther,
    List<IssueDetailDraft>? details,
  }) =>
      IssueFormState(
        fullName: fullName ?? this.fullName,
        phone: phone ?? this.phone,
        municipality:
            municipality != null ? municipality() : this.municipality,
        localityOther: localityOther ?? this.localityOther,
        details: details ?? this.details,
      );

  @override
  List<Object?> get props =>
      [fullName, phone, municipality, localityOther, details];
}

/// Estado y lógica del formulario de incidencias. Maneja el encabezado y un
/// arreglo dinámico de detalles con campos condicionales por categoría.
class IssueFormCubit extends Cubit<IssueFormState> {
  // Sets de categorías que habilitan campos (espejo del front).
  static const _beneficiary = {'PROGRAMAS BIENESTAR', 'PROGRAMAS JCF/BECAS'};
  static const _resource = {
    'INSUMOS MEDICOS',
    'APOYO VARIOS (COMPRAS)',
    'EDUCACIÓN'
  };
  static const _file = {
    'INSUMOS MEDICOS',
    'GESTIONES MEDICAS',
    'APOYO JURIDICO',
    'MEDICAMENTO'
  };

  IssueFormCubit() : super(IssueFormState(details: [IssueDetailDraft()]));

  static bool needsBeneficiary(String? c) => c != null && _beneficiary.contains(c);
  static bool needsResource(String? c) => c != null && _resource.contains(c);
  static bool needsFile(String? c) => c != null && _file.contains(c);

  void setFullName(String v) => emit(state.copyWith(fullName: v));
  void setPhone(String v) => emit(state.copyWith(phone: v));
  void setMunicipality(IssueMunicipality? m) =>
      emit(state.copyWith(municipality: () => m));
  void setLocalityOther(String v) => emit(state.copyWith(localityOther: v));

  void addDetail() =>
      emit(state.copyWith(details: [...state.details, IssueDetailDraft()]));

  void removeDetail(int index) {
    if (state.details.length <= 1) return;
    final list = [...state.details]..removeAt(index);
    emit(state.copyWith(details: list));
  }

  void _update(int index, IssueDetailDraft draft) {
    final list = [...state.details];
    list[index] = draft;
    emit(state.copyWith(details: list));
  }

  void setType(int i, CatItem? v) => _update(i, state.details[i].copyWith(type: v));
  void setStatus(int i, CatItem? v) =>
      _update(i, state.details[i].copyWith(status: v));
  void setComments(int i, String v) =>
      _update(i, state.details[i].copyWith(comments: v));
  void setUrgent(int i, bool v) =>
      _update(i, state.details[i].copyWith(urgent: v));
  void setName(int i, String v) => _update(i, state.details[i].copyWith(name: () => v));
  void setLastName(int i, String v) =>
      _update(i, state.details[i].copyWith(lastName: () => v));
  void setSurName(int i, String v) =>
      _update(i, state.details[i].copyWith(surName: () => v));
  void setCurp(int i, String v) =>
      _update(i, state.details[i].copyWith(curp: () => v.toUpperCase()));
  void setResource(int i, IssueResource? v) =>
      _update(i, state.details[i].copyWith(resource: () => v));

  /// Al cambiar categoría se limpian los campos condicionales que ya no aplican.
  void setCategory(int i, CatItem? v) {
    final name = v?.name;
    _update(
      i,
      state.details[i].copyWith(
        category: v,
        name: needsBeneficiary(name) ? () => state.details[i].name : () => null,
        lastName:
            needsBeneficiary(name) ? () => state.details[i].lastName : () => null,
        surName:
            needsBeneficiary(name) ? () => state.details[i].surName : () => null,
        curp: needsBeneficiary(name) ? () => state.details[i].curp : () => null,
        resource: needsResource(name) ? () => state.details[i].resource : () => null,
        docCategory: needsFile(name) ? () => state.details[i].docCategory : () => null,
        localFilePath:
            needsFile(name) ? () => state.details[i].localFilePath : () => null,
      ),
    );
  }

  void setFile(int i, String? path, CatItem? docCategory) => _update(
        i,
        state.details[i].copyWith(
          localFilePath: () => path,
          docCategory: () => docCategory,
        ),
      );

  bool _detailValid(IssueDetailDraft d) {
    if (d.type == null || d.status == null || d.category == null) return false;
    if (d.comments.trim().isEmpty) return false;
    final c = d.category!.name;
    if (needsBeneficiary(c)) {
      if ((d.name ?? '').trim().isEmpty ||
          (d.lastName ?? '').trim().isEmpty ||
          (d.surName ?? '').trim().isEmpty) return false;
      if ((d.curp ?? '').isNotEmpty && !_curpRegex.hasMatch(d.curp!)) {
        return false;
      }
    }
    if (needsResource(c) && d.resource == null) return false;
    return true;
  }

  bool get isValid {
    if (state.fullName.trim().length < 2) return false;
    if (state.phone.trim().isEmpty) return false;
    if (state.municipality == null) return false;
    if (state.localityOther.trim().isEmpty) return false;
    if (state.details.isEmpty) return false;
    return state.details.every(_detailValid);
  }

  Issue buildIssue() {
    final now = DateTime.now();
    final issueUuid = _uuid.v4();
    final regDate =
        '${now.year.toString().padLeft(4, '0')}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
    return Issue(
      uuid: issueUuid,
      registrationDate: regDate,
      fullName: state.fullName.trim(),
      phone: state.phone.trim(),
      municipalityId: state.municipality!.id,
      municipalityName: state.municipality!.nomMun,
      localityId: 0,
      localityOther: state.localityOther.trim(),
      created: now,
      details: state.details
          .map((d) => IssueDetail(
                uuid: d.uuid,
                issueUuid: issueUuid,
                comments: d.comments.trim(),
                urgent: d.urgent,
                issueTypeId: d.type!.id,
                issueTypeName: d.type!.name,
                issueCategoryId: d.category!.id,
                issueCategoryName: d.category!.name,
                issueStatusId: d.status!.id,
                issueStatusName: d.status!.name,
                name: d.name,
                lastName: d.lastName,
                surName: d.surName,
                curp: d.curp,
                resourceId: d.resource?.id,
                resourceName: d.resource?.name,
                docCategoryId: d.docCategory?.id,
                docCategoryName: d.docCategory?.name,
                localFilePath: d.localFilePath,
              ))
          .toList(),
    );
  }

  void reset() => emit(IssueFormState(details: [IssueDetailDraft()]));
}
