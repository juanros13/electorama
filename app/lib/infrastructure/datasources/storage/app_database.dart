import 'package:drift/drift.dart';
import 'package:drift_flutter/drift_flutter.dart';

part 'app_database.g.dart';

// Tabla de lonas/bardas. `@DataClassName` evita el choque de nombre con la
// entidad de dominio `CanvasWall`.
@DataClassName('CanvasWallRow')
class CanvasWalls extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get uuid => text()();
  TextColumn get deviceId => text()();
  TextColumn get latitude => text()();
  TextColumn get longitude => text()();
  TextColumn get projectId => text()();
  TextColumn get categoryId => text()();
  TextColumn get date => text()();
  BlobColumn get file => blob().nullable()();
  BoolColumn get isInSync => boolean().withDefault(const Constant(false))();
}

// Tabla de visitas a personas (módulo legacy).
@DataClassName('VisitPeopleRow')
class VisitPeoples extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get uuid => text()();
  TextColumn get deviceId => text()();
  BoolColumn get openTheDoor => boolean().withDefault(const Constant(false))();
  TextColumn get street => text()();
  TextColumn get externallNumber => text()();
  TextColumn get longitude => text()();
  TextColumn get latitude => text()();
  TextColumn get visitNumber => text()();
  BoolColumn get syncVisit => boolean().withDefault(const Constant(false))();
  TextColumn get age => text().nullable()();
  TextColumn get names => text().nullable()();
  TextColumn get lastName => text().nullable()();
  TextColumn get surName => text().nullable()();
  TextColumn get phone => text().nullable()();
  BoolColumn get acceptInformation => boolean().nullable()();
  TextColumn get sex => text().nullable()();
  TextColumn get hasSympathy => text().nullable()();
  DateTimeColumn get created => dateTime().nullable()();
}

// Tabla de followers (convencidos). El embedded `Sex` se aplana en
// catSexId/catSexNombre. No guarda foto (sin blob).
@DataClassName('FollowerRow')
class Followers extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get uuid => text()();
  TextColumn get deviceId => text()();
  TextColumn get latitude => text()();
  TextColumn get longitude => text()();
  TextColumn get sectionNumber => text()();
  TextColumn get street => text()();
  TextColumn get externalNumber => text()();
  TextColumn get internalNumber => text().nullable()();
  TextColumn get zipCode => text().nullable()();
  TextColumn get neighborhood => text().nullable()();
  TextColumn get sectionNumberIne => text().nullable()();
  IntColumn get residenceEntityId => integer().nullable()();
  IntColumn get residenceMunicipalityId => integer().nullable()();
  BoolColumn get isAdult => boolean().nullable()();
  TextColumn get names => text().nullable()();
  TextColumn get lastName => text().nullable()();
  TextColumn get surName => text().nullable()();
  TextColumn get curp => text().nullable()();
  TextColumn get ine => text().nullable()();
  TextColumn get validity => text().nullable()();
  TextColumn get birthdate => text().nullable()();
  TextColumn get cellPhoneNumber => text().nullable()();
  TextColumn get supportsWhatsapp => text().nullable()();
  TextColumn get cellPhoneNumberOther => text().nullable()();
  TextColumn get email => text().nullable()();
  IntColumn get catSexId => integer().nullable()();
  TextColumn get catSexNombre => text().nullable()();
  IntColumn get birthEntityId => integer().nullable()();
  BoolColumn get isInSync => boolean().withDefault(const Constant(false))();
  TextColumn get supportsMovement => text()();
  TextColumn get committeeParticipation => text().nullable()();
  TextColumn get pollinStationRepresentativeParticipation =>
      text().nullable()();
  TextColumn get invitingOthersToParticipate => text().nullable()();
  TextColumn get numberOfPeopleInvited => text().nullable()();
  DateTimeColumn get created => dateTime().nullable()();

  // v4 — captura contra msvc-structure (POST /memberships)
  /// El puesto donde queda (capture-targets); null = el de quien captura.
  IntColumn get positionId => integer().nullable()();
  /// PENDING, SYNCED, DUPLICATE (ya era convencido) o REJECTED (el servidor
  /// no lo aceptó: hay que corregirlo o borrarlo).
  TextColumn get syncStatus =>
      text().withDefault(const Constant('PENDING'))();
  /// El mensaje del servidor cuando no se aceptó o ya existía.
  TextColumn get syncError => text().nullable()();
  IntColumn get membershipId => integer().nullable()();
  /// Dónde quedó: "S-0233A1-11".
  TextColumn get positionLabel => text().nullable()();
  DateTimeColumn get syncedAt => dateTime().nullable()();
}

// Los puestos donde quien usa la app puede dejar convencidos
// (msvc-structure capture-targets), guardados para capturar sin red.
@DataClassName('CaptureTargetRow')
class CaptureTargets extends Table {
  IntColumn get positionId => integer()();
  TextColumn get level => text()();
  TextColumn get label => text()();
  TextColumn get holderName => text().nullable()();
  IntColumn get sectionGeoId => integer().nullable()();
  IntColumn get municipalityGeoId => integer().nullable()();
  BoolColumn get own => boolean().withDefault(const Constant(false))();
  DateTimeColumn get updated => dateTime()();

  @override
  Set<Column> get primaryKey => {positionId};
}

// El polígono de una sección (GeoJSON de geography), para validar el GPS sin red.
@DataClassName('SectionGeometryRow')
class SectionGeometries extends Table {
  IntColumn get sectionGeoId => integer()();
  TextColumn get geoJson => text()();
  DateTimeColumn get updated => dateTime()();

  @override
  Set<Column> get primaryKey => {sectionGeoId};
}

// Configuración del usuario: municipio aplanado + secciones como JSON.
@DataClassName('UserSettingsRow')
class UserSettingsTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get municipalityId => integer().nullable()();
  TextColumn get municipalityName => text().nullable()();
  TextColumn get sectionsSelected =>
      text().withDefault(const Constant('[]'))(); // JSON
}

// Encabezado de una incidencia ciudadana (citizen-issue). Localidad puede ser
// del catálogo o texto libre ("OTRA") para captura offline.
@DataClassName('IssueRow')
class Issues extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get uuid => text()();
  TextColumn get registrationDate => text()(); // yyyy-MM-dd
  TextColumn get fullName => text()();
  TextColumn get phone => text()();
  IntColumn get municipalityId => integer()();
  TextColumn get municipalityName => text()();
  IntColumn get localityId => integer().withDefault(const Constant(0))();
  TextColumn get localityName => text().nullable()();
  TextColumn get localityOther => text().nullable()();
  BoolColumn get isInSync => boolean().withDefault(const Constant(false))();
  DateTimeColumn get created => dateTime().nullable()();
}

// Detalle de incidencia (el FormArray del front). Catálogos aplanados a id+name;
// campos condicionales por categoría (beneficiario CURP / recurso / archivo).
@DataClassName('IssueDetailRow')
class IssueDetails extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get uuid => text()();
  TextColumn get issueUuid => text()(); // referencia a Issues.uuid
  TextColumn get comments => text()();
  BoolColumn get urgent => boolean().withDefault(const Constant(false))();
  IntColumn get issueTypeId => integer()();
  TextColumn get issueTypeName => text()();
  IntColumn get issueCategoryId => integer()();
  TextColumn get issueCategoryName => text()();
  IntColumn get issueStatusId => integer()();
  TextColumn get issueStatusName => text()();
  // beneficiario (categorías PROGRAMAS BIENESTAR / JCF-BECAS)
  TextColumn get name => text().nullable()();
  TextColumn get lastName => text().nullable()();
  TextColumn get surName => text().nullable()();
  TextColumn get curp => text().nullable()();
  // recurso (categorías INSUMOS MEDICOS / APOYO VARIOS / EDUCACIÓN)
  IntColumn get resourceId => integer().nullable()();
  TextColumn get resourceName => text().nullable()();
  // archivo (categorías con comprobante) — ruta local del archivo capturado
  IntColumn get docCategoryId => integer().nullable()();
  TextColumn get docCategoryName => text().nullable()();
  TextColumn get localFilePath => text().nullable()();
}

@DriftDatabase(
    tables: [
      CanvasWalls,
      VisitPeoples,
      Followers,
      UserSettingsTable,
      Issues,
      IssueDetails,
      CaptureTargets,
      SectionGeometries,
    ])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 4;

  @override
  MigrationStrategy get migration => MigrationStrategy(
        onCreate: (m) => m.createAll(),
        onUpgrade: (m, from, to) async {
          // v1→v2: añade followers y user_settings sin tocar las existentes.
          if (from < 2) {
            await m.createTable(followers);
            await m.createTable(userSettingsTable);
          }
          // v2→v3: añade incidencias ciudadanas.
          if (from < 3) {
            await m.createTable(issues);
            await m.createTable(issueDetails);
          }
          // v3→v4: captura contra msvc-structure. Los convencidos que ya
          // estaban sincronizados con el modelo viejo quedan como SYNCED.
          if (from < 4) {
            await m.addColumn(followers, followers.positionId);
            await m.addColumn(followers, followers.syncStatus);
            await m.addColumn(followers, followers.syncError);
            await m.addColumn(followers, followers.membershipId);
            await m.addColumn(followers, followers.positionLabel);
            await m.addColumn(followers, followers.syncedAt);
            await customStatement(
                "UPDATE followers SET sync_status = 'SYNCED' WHERE is_in_sync = 1");
            await m.createTable(captureTargets);
            await m.createTable(sectionGeometries);
          }
        },
      );
}

QueryExecutor _openConnection() {
  return driftDatabase(name: 'electorama_app');
}

/// Instancia única compartida. Múltiples instancias Drift sobre el mismo
/// archivo causan bloqueos, así que todos los datasources usan este singleton.
AppDatabase? _appDatabaseInstance;
AppDatabase get appDatabase => _appDatabaseInstance ??= AppDatabase();
