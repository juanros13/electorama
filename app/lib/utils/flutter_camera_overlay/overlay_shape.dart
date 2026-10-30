import 'package:flutter/material.dart';
import 'package:electorama_app/utils/flutter_camera_overlay/model.dart';

class OverlayShape extends StatelessWidget {
  const OverlayShape(this.model, {super.key});

  final OverlayModel model;

  @override
  Widget build(BuildContext context) {
    final media = MediaQuery.of(context);
    final size = media.size;
    final double width = media.orientation == Orientation.portrait
        ? size.shortestSide * .9
        : size.longestSide * .5;

    final double ratio = model.ratio as double;
    final double radius =
        model.cornerRadius == null ? 0 : model.cornerRadius! * (width / ratio);

    return Stack(
      children: [
        Align(
          alignment: Alignment.center,
          child: Container(
            width: width,
            height: width / ratio,
            decoration: ShapeDecoration(
              color: Colors.transparent,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(radius),
                side: const BorderSide(width: 1, color: Colors.white),
              ),
            ),
          ),
        ),
        ColorFiltered(
          colorFilter: const ColorFilter.mode(Colors.black54, BlendMode.srcOut),
          child: Stack(
            children: [
              Container(
                decoration: const BoxDecoration(color: Colors.transparent),
                child: Align(
                  alignment: Alignment.center,
                  child: Container(
                    width: width,
                    height: width / ratio,
                    decoration: BoxDecoration(
                      color: Colors.black,
                      borderRadius: BorderRadius.circular(radius),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
