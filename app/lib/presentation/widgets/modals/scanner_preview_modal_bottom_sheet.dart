import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';

class ScannerPreviewModalBottomSheet extends StatelessWidget {
  const ScannerPreviewModalBottomSheet({super.key});

  bool _isNaN(String c) => int.tryParse(c) == null;

  String _firstThreeLines(String texto) =>
      texto.split('\n').take(3).join('\n');

  @override
  Widget build(BuildContext context) {
    final followerFormBloc = context.watch<FollowerFormBloc>();
    final colors = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;
    final deviceWidth = MediaQuery.of(context).size.width;

    return BlocBuilder<FollowerFormBloc, FollowerFormState>(
      builder: (context, state) {
        final nameRegex = RegExp(r'NOMBRE([\s\S]*?)DOMICILIO');
        final addressRegex = RegExp(r'DOMICILIO([\s\S]*?)CLAVE');
        final ineLineRegex = RegExp(r'CLAVE DE ELECTOR\s*([\s\S]{18})');
        final curpLineRegex = RegExp(r'CURP\s*([\s\S]{18})');
        final seccionRegex = RegExp(r'SECCIÓN\s*([\s\S]{4})');
        final curpRegex = RegExp(
            r'\b[A-Z][AEIOU][A-Z]{2}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}(?:[0-9]|[A-Z])[0-9]\b');
        final ineRegex = RegExp(
            r'\b(?:[A-Z][B-DF-HJ-NP-TV-Z]){3}[0-9]{2}(?:0[1-9]|1[0-2])(?:[1-2][0-9]|0[1-9]|3[0-1])(?:0[1-9]|[1-2][0-9]|3[0-2]|88|87)[HM][0-9]{3}\b');
        final zipRegex = RegExp(r'\b\d{5}\b');

        String fullName = '';
        String name = '';
        String lastName = '';
        String surName = '';
        String address = '';
        String ine = '';
        String curp = '';
        String seccion = '';
        String street = '';
        String zipCode = '';
        String neighborhood = '';
        String validity = '';
        const sc = 'SIN COINCIDENCIAS';

        if (state.ineData != '') {
          final data = state.ineData.toUpperCase();

          final nameM = nameRegex.firstMatch(data);
          final addressM = addressRegex.firstMatch(data);
          final ineM = ineLineRegex.firstMatch(data);
          final curpM = curpLineRegex.firstMatch(data);
          final seccionM = seccionRegex.firstMatch(data);

          if (nameM != null) {
            fullName = nameM.group(1)!.trim();
            if (fullName.contains('INSTITUTO') ||
                fullName.contains('NACIONAL') ||
                fullName.contains('ELECTORAL')) {
              fullName = _firstThreeLines(fullName);
            }
            final parts = fullName.split('\n');
            if (parts.length == 3) {
              lastName = parts[0];
              surName = parts[1];
              name = parts[2];
            }
          } else {
            fullName = sc;
          }

          if (addressM != null) {
            address = _firstThreeLines(addressM.group(1)!.trim());
            final lines = address.split('\n');
            if (lines.length == 2) {
              final zipM = zipRegex.firstMatch(lines[0]);
              if (zipM != null) {
                zipCode = zipM.group(0)!;
                neighborhood = lines[0].substring(0, lines[0].indexOf(zipCode));
              }
            }
            if (lines.length == 3) {
              street = lines[0];
              final zipM = zipRegex.firstMatch(lines[1]);
              if (zipM != null) {
                zipCode = zipM.group(0)!;
                neighborhood = lines[1].substring(0, lines[1].indexOf(zipCode));
              }
            }
          } else {
            address = sc;
          }

          if (ineM != null) {
            ine = ineM.group(1)!;
            ine = ine.substring(0, 6) +
                ine.characters.getRange(6, 12).string.replaceAll('O', '0') +
                ine.substring(12, 18);
            if (!ineRegex.hasMatch(ine)) ine = sc;
          } else {
            ine = sc;
          }

          if (curpM != null) {
            curp = curpM.group(1)!;
            curp = curp.substring(0, 4) +
                curp.characters.getRange(4, 10).string.replaceAll('O', '0') +
                curp.substring(10, 18);
            if (!curpRegex.hasMatch(curp)) curp = sc;
          } else {
            curp = sc;
          }

          if (seccionM != null) {
            seccion = seccionM.group(1)!;
            if (_isNaN(seccion)) seccion = sc;
          } else {
            seccion = sc;
          }

          if (data.contains('VIGENCIA')) {
            validity = data.substring(data.indexOf('VIGENCIA'), data.length);
            validity = validity.replaceAll('VIGENCIA', '').trimLeft();
            if (validity.length > 4) {
              validity = validity.substring(validity.length - 4);
            }
            if (_isNaN(validity)) validity = sc;
          } else {
            validity = sc;
          }
        }

        if (state.ineData == '') {
          return _sheet(
            colors: colors,
            width: deviceWidth,
            children: [
              Icon(Icons.warning_amber_rounded,
                  color: colors.tertiary, size: 96),
              const SizedBox(height: 16),
              Text('No se encontraron coincidencias',
                  style: textTheme.titleLarge, textAlign: TextAlign.center),
              const SizedBox(height: 16),
              FilledButton.icon(
                style: FilledButton.styleFrom(backgroundColor: colors.error),
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.replay),
                label: const Text('Regresar'),
              ),
            ],
          );
        }

        Widget field(String label, String value) {
          final isSc = value.contains('SIN COINCIDENCIAS');
          return Padding(
            padding: const EdgeInsets.only(bottom: 6),
            child: Column(
              children: [
                Text(label, style: textTheme.labelLarge),
                Text(value.replaceAll('\n', ' '),
                    textAlign: TextAlign.center,
                    style: textTheme.bodyMedium?.copyWith(
                        color: isSc ? colors.error : colors.onSurfaceVariant)),
              ],
            ),
          );
        }

        return _sheet(
          colors: colors,
          width: deviceWidth,
          children: [
            Text('Datos capturados',
                style: textTheme.titleLarge, textAlign: TextAlign.center),
            const SizedBox(height: 4),
            Text(
              'Si la información no es correcta, toma una nueva fotografía.',
              style: textTheme.bodySmall?.copyWith(color: colors.onSurfaceVariant),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            field('NOMBRE', fullName),
            field('DOMICILIO', address),
            field('CLAVE DE ELECTOR', ine),
            field('CURP', curp),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Expanded(child: field('SECCIÓN', seccion)),
                Expanded(child: field('VIGENCIA', validity)),
              ],
            ),
            const SizedBox(height: 12),
            FilledButton.icon(
              style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(50)),
              onPressed: () {
                followerFormBloc.add(NamesChange(name));
                followerFormBloc.add(LastNameChange(lastName));
                followerFormBloc.add(SurNameChange(surName));
                followerFormBloc.add(const SupportsMovementChange(true));
                followerFormBloc.add(SectionNumberIneChange(seccion));
                followerFormBloc.add(StreetChange(street));
                followerFormBloc.add(NeighborhoodChange(neighborhood));
                followerFormBloc.add(ZipCodeChange(zipCode));
                followerFormBloc.add(CurpChange(curp));
                followerFormBloc.add(IneChange(ine));
                followerFormBloc.add(ExpirationYearChange(validity));

                if (curp.isNotEmpty && curp != sc && curp.length >= 18) {
                  final birth = curp.substring(4, 10);
                  var anio = (_isNaN(curp.characters.characterAt(16).string)
                          ? '20'
                          : '19') +
                      birth.substring(0, 2);
                  final mes = birth.substring(2, 4);
                  final dia = birth.substring(4, 6);
                  if ((int.tryParse(anio) ?? 0) > 2023) {
                    anio = '19${anio.substring(2, 4)}';
                  }
                  followerFormBloc.add(BirthdayChange('$dia-$mes-$anio'));
                }

                // cierra la hoja y regresa a la ficha que abrió el escáner (el
                // router se toma antes: después de cerrar, el contexto se va)
                final router = GoRouter.of(context);
                Navigator.pop(context);
                _backToForm(router);
              },
              icon: const Icon(Icons.check),
              label: const Text('Continuar'),
            ),
          ],
        );
      },
    );
  }

  Widget _sheet(
      {required ColorScheme colors,
      required double width,
      required List<Widget> children}) {
    return Container(
      width: width,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colors.surface,
        borderRadius: const BorderRadius.only(
            topLeft: Radius.circular(28), topRight: Radius.circular(28)),
      ),
      child: SafeArea(
        top: false,
        child: Column(mainAxisSize: MainAxisSize.min, children: children),
      ),
    );
  }
}

/// Regresa a la ficha: si el escáner se abrió encima (push), basta con salir
/// de él; si se llegó directo a la ruta, se va a la ficha.
void _backToForm(GoRouter router) {
  if (router.canPop()) {
    router.pop();
  } else {
    router.go('/follower/1');
  }
}
