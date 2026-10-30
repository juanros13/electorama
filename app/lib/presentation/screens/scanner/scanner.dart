import 'dart:io';

import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';

import 'package:electorama_app/presentation/blocs/follower_form/follower_form_bloc.dart';
import 'package:electorama_app/presentation/widgets/modals/scanner_preview_modal_bottom_sheet.dart';
import 'package:electorama_app/utils/flutter_camera_overlay/flutter_camera_overlay.dart';
import 'package:electorama_app/utils/flutter_camera_overlay/model.dart';

class Scanner extends StatefulWidget {
  static const name = 'scanner';
  const Scanner({super.key});

  @override
  State<Scanner> createState() => _ScannerState();
}

class _ScannerState extends State<Scanner> {
  final OverlayFormat format = OverlayFormat.cardID1;
  final textRecognizer = TextRecognizer();

  @override
  void dispose() {
    textRecognizer.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final followerFormBloc = context.read<FollowerFormBloc>();

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        // siempre hay forma de salir: si se abrió encima de la ficha se
        // regresa a ella; si se llegó directo, se va a la ficha
        leading: IconButton(
          tooltip: 'Regresar',
          icon: const Icon(Icons.arrow_back),
          onPressed: () =>
              context.canPop() ? context.pop() : context.go('/follower/1'),
        ),
        // el estilo de título del tema gana a foregroundColor: blanco explícito
        title: const Text('Escanear INE', style: TextStyle(color: Colors.white)),
        backgroundColor: Colors.black,
        foregroundColor: Colors.white,
      ),
      body: FutureBuilder<List<CameraDescription>?>(
        future: availableCameras(),
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(
                child: CircularProgressIndicator(color: Colors.white));
          }
          if (snapshot.data == null || snapshot.data!.isEmpty) {
            return const Center(
                child: Text('Cámara no encontrada',
                    style: TextStyle(color: Colors.white)));
          }
          return CameraOverlay(
            snapshot.data!.first,
            CardOverlay.byFormat(format),
            (XFile file) => _showPreviewDialog(context, file, followerFormBloc),
            info:
                'Posiciona la credencial dentro del rectángulo y asegúrate de que la imagen sea legible.',
            label: 'Escanea la INE',
          );
        },
      ),
    );
  }

  void _showPreviewDialog(
      BuildContext screenContext, XFile file, FollowerFormBloc followerFormBloc) {
    final colors = Theme.of(screenContext).colorScheme;
    showDialog<void>(
      context: screenContext,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Credencial para votar', textAlign: TextAlign.center),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: Image.file(File(file.path), fit: BoxFit.fitWidth),
            ),
            const SizedBox(height: 12),
            Text(
              'Tus fotografías no se almacenan en el sistema. Tu privacidad es prioridad.',
              style: TextStyle(color: colors.onSurfaceVariant, fontSize: 12),
              textAlign: TextAlign.center,
            ),
          ],
        ),
        actions: [
          TextButton.icon(
            onPressed: () {
              Navigator.pop(dialogContext);
              followerFormBloc.add(const LoadIneData(''));
            },
            icon: const Icon(Icons.replay),
            label: const Text('Repetir'),
          ),
          FilledButton.icon(
            onPressed: () async {
              final inputImage = InputImage.fromFile(File(file.path));
              final recognized =
                  await textRecognizer.processImage(inputImage);
              followerFormBloc.add(LoadIneData(recognized.text));
              if (dialogContext.mounted) Navigator.pop(dialogContext);
              if (screenContext.mounted) {
                showModalBottomSheet<void>(
                  context: screenContext,
                  isScrollControlled: true,
                  backgroundColor: Colors.transparent,
                  builder: (_) => const ScannerPreviewModalBottomSheet(),
                );
              }
            },
            icon: const Icon(Icons.document_scanner),
            label: const Text('Escanear'),
          ),
        ],
      ),
    );
  }
}
