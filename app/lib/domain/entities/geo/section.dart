class Section {
    final int id;
    final String userAuthId;
    final String seccionNumber;
    final int seccionGeoId;
    final int meta;
    final dynamic deleted;
    final int municipalityId;

    Section({
        required this.id,
        required this.userAuthId,
        required this.seccionNumber,
        required this.seccionGeoId,
        required this.meta,
        this.deleted,
        required this.municipalityId,
    });

}
