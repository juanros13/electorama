/** Lona/barda en el listado. */
export interface CanvasWall {
  id: number;
  coordinateZone: string;
  reference: string;
  municipalityname: string;
  localityName: string;
  seccionNumber: string | number;
  pathEvidencePhoto: string;
  deleted: string | null;
  canvasAndWallsCategory: { name: string };
  canvasAndWallsProyect: { name: string };
}
