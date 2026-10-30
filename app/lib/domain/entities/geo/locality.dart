class Locality {
    int id;
    String municipio;
    String seccion;
    String localidad;
    int seccionId;
    dynamic deleted;

    Locality({
        required this.id,
        required this.municipio,
        required this.seccion,
        required this.localidad,
        required this.seccionId,
        this.deleted,
    });

}
