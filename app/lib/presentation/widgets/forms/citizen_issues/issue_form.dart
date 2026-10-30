import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';

import 'package:electorama_app/config/theme/app_spacing.dart';
import 'package:electorama_app/domain/entities/citizen_issues/issue_catalogs.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/catalogs/issue_catalogs_cubit.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_form/issue_form_cubit.dart';
import 'package:electorama_app/presentation/blocs/citizen_issues/issue_list/issue_list_cubit.dart';
import 'package:electorama_app/presentation/widgets/shared/snackbars/custom_snackbar.dart';

class IssueForm extends StatelessWidget {
  const IssueForm({super.key});

  @override
  Widget build(BuildContext context) {
    final form = context.watch<IssueFormCubit>();
    final catalogs = context.watch<IssueCatalogsCubit>().state;
    final state = form.state;
    final textTheme = Theme.of(context).textTheme;

    if (!catalogs.loaded) {
      return const Padding(
        padding: EdgeInsets.all(AppSpacing.xxl),
        child: Center(child: CircularProgressIndicator()),
      );
    }

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: AppSpacing.lg),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Datos del ciudadano', style: textTheme.titleMedium),
          const SizedBox(height: AppSpacing.md),
          TextFormField(
            initialValue: state.fullName,
            textCapitalization: TextCapitalization.characters,
            inputFormatters: [_UpperCaseFormatter()],
            decoration: const InputDecoration(
              labelText: 'Nombre completo *',
              prefixIcon: Icon(Icons.person),
            ),
            onChanged: form.setFullName,
          ),
          const SizedBox(height: AppSpacing.md),
          TextFormField(
            initialValue: state.phone,
            keyboardType: TextInputType.phone,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(10),
            ],
            decoration: const InputDecoration(
              labelText: 'Teléfono *',
              prefixIcon: Icon(Icons.phone),
            ),
            onChanged: form.setPhone,
          ),
          const SizedBox(height: AppSpacing.md),
          DropdownButtonFormField<IssueMunicipality>(
            initialValue: state.municipality,
            isExpanded: true,
            decoration: const InputDecoration(
              labelText: 'Municipio *',
              prefixIcon: Icon(Icons.location_city),
            ),
            items: catalogs.municipalities
                .map((m) => DropdownMenuItem(value: m, child: Text(m.nomMun)))
                .toList(),
            onChanged: form.setMunicipality,
          ),
          const SizedBox(height: AppSpacing.md),
          TextFormField(
            initialValue: state.localityOther,
            textCapitalization: TextCapitalization.characters,
            inputFormatters: [_UpperCaseFormatter()],
            decoration: const InputDecoration(
              labelText: 'Localidad *',
              hintText: 'Escribe la localidad',
              prefixIcon: Icon(Icons.holiday_village_outlined),
            ),
            onChanged: form.setLocalityOther,
          ),
          const SizedBox(height: AppSpacing.xl),
          Text('Incidencias', style: textTheme.titleMedium),
          const SizedBox(height: AppSpacing.sm),
          for (var i = 0; i < state.details.length; i++)
            _DetailCard(index: i, form: form, catalogs: catalogs),
          const SizedBox(height: AppSpacing.sm),
          OutlinedButton.icon(
            onPressed: form.addDetail,
            icon: const Icon(Icons.add),
            label: const Text('Agregar incidencia'),
          ),
          const SizedBox(height: AppSpacing.lg),
          FilledButton.icon(
            style: FilledButton.styleFrom(
                minimumSize: const Size.fromHeight(50)),
            onPressed: form.isValid
                ? () {
                    FocusManager.instance.primaryFocus?.unfocus();
                    _confirm(context, form);
                  }
                : null,
            icon: const Icon(Icons.save),
            label: const Text('Guardar'),
          ),
          const SizedBox(height: AppSpacing.xxl),
        ],
      ),
    );
  }

  Future<void> _confirm(BuildContext context, IssueFormCubit form) {
    final listCubit = context.read<IssueListCubit>();
    return showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('¿Estás seguro?'),
        content: const Text('Se guardará la incidencia en este teléfono.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () async {
              await listCubit.save(form.buildIssue());
              form.reset();
              if (dialogContext.mounted) Navigator.pop(dialogContext);
              if (context.mounted) {
                CustomSnackbar.showSnackbar(context, TypeSnackbar.success,
                    'Incidencia guardada correctamente.');
              }
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }
}

class _DetailCard extends StatelessWidget {
  final int index;
  final IssueFormCubit form;
  final IssueCatalogsState catalogs;
  const _DetailCard(
      {required this.index, required this.form, required this.catalogs});

  @override
  Widget build(BuildContext context) {
    final d = form.state.details[index];
    final categoryName = d.category?.name;
    final showBeneficiary = IssueFormCubit.needsBeneficiary(categoryName);
    final showResource = IssueFormCubit.needsResource(categoryName);
    final showFile = IssueFormCubit.needsFile(categoryName);
    final resources =
        d.category == null ? <IssueResource>[] : catalogs.resources
            .where((r) => r.categoryId == d.category!.id)
            .toList();

    return Card(
      margin: const EdgeInsets.only(bottom: AppSpacing.md),
      child: Padding(
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text('Incidencia ${index + 1}',
                    style: Theme.of(context).textTheme.titleSmall),
                const Spacer(),
                if (form.state.details.length > 1)
                  IconButton(
                    onPressed: () => form.removeDetail(index),
                    icon: const Icon(Icons.delete_outline),
                    tooltip: 'Eliminar',
                  ),
              ],
            ),
            DropdownButtonFormField<CatItem>(
              key: ValueKey('type-${d.uuid}'),
              initialValue: d.type,
              isExpanded: true,
              decoration: const InputDecoration(
                  labelText: 'Tipo *', prefixIcon: Icon(Icons.category_outlined)),
              items: catalogs.types
                  .map((t) => DropdownMenuItem(value: t, child: Text(t.name)))
                  .toList(),
              onChanged: (v) => form.setType(index, v),
            ),
            const SizedBox(height: AppSpacing.md),
            DropdownButtonFormField<CatItem>(
              key: ValueKey('cat-${d.uuid}'),
              initialValue: d.category,
              isExpanded: true,
              decoration: const InputDecoration(
                  labelText: 'Categoría *', prefixIcon: Icon(Icons.label_outline)),
              items: catalogs.categories
                  .map((c) => DropdownMenuItem(value: c, child: Text(c.name)))
                  .toList(),
              onChanged: (v) => form.setCategory(index, v),
            ),
            const SizedBox(height: AppSpacing.md),
            DropdownButtonFormField<CatItem>(
              key: ValueKey('status-${d.uuid}'),
              initialValue: d.status,
              isExpanded: true,
              decoration: const InputDecoration(
                  labelText: 'Estatus *', prefixIcon: Icon(Icons.flag_outlined)),
              items: catalogs.statuses
                  .map((s) => DropdownMenuItem(value: s, child: Text(s.name)))
                  .toList(),
              onChanged: (v) => form.setStatus(index, v),
            ),
            const SizedBox(height: AppSpacing.md),
            TextFormField(
              key: ValueKey('comments-${d.uuid}'),
              initialValue: d.comments,
              minLines: 2,
              maxLines: 4,
              decoration: const InputDecoration(
                labelText: 'Comentarios *',
                alignLabelWithHint: true,
                prefixIcon: Icon(Icons.comment_outlined),
              ),
              onChanged: (v) => form.setComments(index, v),
            ),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              value: d.urgent,
              onChanged: (v) => form.setUrgent(index, v),
              title: const Text('Urgente'),
            ),
            if (showBeneficiary) ...[
              const Divider(),
              Text('Beneficiario',
                  style: Theme.of(context).textTheme.titleSmall),
              const SizedBox(height: AppSpacing.sm),
              TextFormField(
                key: ValueKey('name-${d.uuid}'),
                initialValue: d.name,
                textCapitalization: TextCapitalization.characters,
                inputFormatters: [_UpperCaseFormatter()],
                decoration: const InputDecoration(labelText: 'Nombre(s) *'),
                onChanged: (v) => form.setName(index, v),
              ),
              const SizedBox(height: AppSpacing.sm),
              TextFormField(
                key: ValueKey('last-${d.uuid}'),
                initialValue: d.lastName,
                textCapitalization: TextCapitalization.characters,
                inputFormatters: [_UpperCaseFormatter()],
                decoration:
                    const InputDecoration(labelText: 'Apellido paterno *'),
                onChanged: (v) => form.setLastName(index, v),
              ),
              const SizedBox(height: AppSpacing.sm),
              TextFormField(
                key: ValueKey('sur-${d.uuid}'),
                initialValue: d.surName,
                textCapitalization: TextCapitalization.characters,
                inputFormatters: [_UpperCaseFormatter()],
                decoration:
                    const InputDecoration(labelText: 'Apellido materno *'),
                onChanged: (v) => form.setSurName(index, v),
              ),
              const SizedBox(height: AppSpacing.sm),
              TextFormField(
                key: ValueKey('curp-${d.uuid}'),
                initialValue: d.curp,
                textCapitalization: TextCapitalization.characters,
                inputFormatters: [
                  _UpperCaseFormatter(),
                  LengthLimitingTextInputFormatter(18),
                ],
                decoration: const InputDecoration(
                    labelText: 'CURP', hintText: '18 caracteres'),
                onChanged: (v) => form.setCurp(index, v),
              ),
            ],
            if (showResource) ...[
              const Divider(),
              const SizedBox(height: AppSpacing.sm),
              DropdownButtonFormField<IssueResource>(
                key: ValueKey('res-${d.uuid}-${d.category?.id}'),
                initialValue: d.resource,
                isExpanded: true,
                decoration: const InputDecoration(
                    labelText: 'Recurso *',
                    prefixIcon: Icon(Icons.inventory_2_outlined)),
                items: resources
                    .map((r) =>
                        DropdownMenuItem(value: r, child: Text(r.name)))
                    .toList(),
                onChanged: (v) => form.setResource(index, v),
              ),
            ],
            if (showFile) ...[
              const Divider(),
              const SizedBox(height: AppSpacing.sm),
              _FilePicker(index: index, form: form, catalogs: catalogs),
            ],
          ],
        ),
      ),
    );
  }
}

class _FilePicker extends StatelessWidget {
  final int index;
  final IssueFormCubit form;
  final IssueCatalogsState catalogs;
  const _FilePicker(
      {required this.index, required this.form, required this.catalogs});

  @override
  Widget build(BuildContext context) {
    final d = form.state.details[index];
    if (d.localFilePath != null) {
      return Row(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: Image.file(File(d.localFilePath!),
                width: 64, height: 64, fit: BoxFit.cover),
          ),
          const SizedBox(width: AppSpacing.md),
          const Expanded(child: Text('Comprobante adjuntado')),
          IconButton(
            onPressed: () => form.setFile(index, null, null),
            icon: const Icon(Icons.close),
          ),
        ],
      );
    }
    return OutlinedButton.icon(
      onPressed: () => _pick(context),
      icon: const Icon(Icons.attach_file),
      label: const Text('Adjuntar comprobante (foto)'),
    );
  }

  Future<void> _pick(BuildContext context) async {
    final source = await showModalBottomSheet<ImageSource>(
      context: context,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.photo_camera),
              title: const Text('Tomar foto'),
              onTap: () => Navigator.pop(ctx, ImageSource.camera),
            ),
            ListTile(
              leading: const Icon(Icons.photo_library),
              title: const Text('Galería'),
              onTap: () => Navigator.pop(ctx, ImageSource.gallery),
            ),
          ],
        ),
      ),
    );
    if (source == null) return;
    final picker = ImagePicker();
    final x = await picker.pickImage(
        source: source, imageQuality: 60, maxWidth: 1600);
    if (x == null) return;
    final dir = await getApplicationDocumentsDirectory();
    final name = 'issue_${DateTime.now().millisecondsSinceEpoch}.jpg';
    final saved = await File(x.path).copy('${dir.path}/$name');
    final imagen = catalogs.docCategories.firstWhere(
      (c) => c.name == 'IMAGEN',
      orElse: () => catalogs.docCategories.isNotEmpty
          ? catalogs.docCategories.first
          : const CatItem(id: 1, name: 'IMAGEN'),
    );
    form.setFile(index, saved.path, imagen);
  }
}

class _UpperCaseFormatter extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
      TextEditingValue oldValue, TextEditingValue newValue) {
    return newValue.copyWith(text: newValue.text.toUpperCase());
  }
}
