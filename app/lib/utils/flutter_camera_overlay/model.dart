/// ISO Card formats — https://www.iso.org/standard/70483.html
enum OverlayFormat { cardID1, cardID2, cardID3, simID000 }

enum OverlayOrientation { landscape, portrait }

abstract class OverlayModel {
  double? ratio;
  double? cornerRadius;
  OverlayOrientation? orientation;
}

class CardOverlay implements OverlayModel {
  CardOverlay({
    this.ratio = 1.5,
    this.cornerRadius = 0.66,
    this.orientation = OverlayOrientation.landscape,
  });

  @override
  double? ratio;
  @override
  double? cornerRadius;
  @override
  OverlayOrientation? orientation;

  static CardOverlay byFormat(OverlayFormat format) {
    switch (format) {
      case OverlayFormat.cardID1:
        return CardOverlay(ratio: 1.59, cornerRadius: 0.064);
      case OverlayFormat.cardID2:
        return CardOverlay(ratio: 1.42, cornerRadius: 0.067);
      case OverlayFormat.cardID3:
        return CardOverlay(ratio: 1.42, cornerRadius: 0.057);
      case OverlayFormat.simID000:
        return CardOverlay(ratio: 1.66, cornerRadius: 0.073);
    }
  }
}
