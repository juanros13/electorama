// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// ignore_for_file: type=lint
class $CanvasWallsTable extends CanvasWalls
    with TableInfo<$CanvasWallsTable, CanvasWallRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $CanvasWallsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _uuidMeta = const VerificationMeta('uuid');
  @override
  late final GeneratedColumn<String> uuid = GeneratedColumn<String>(
      'uuid', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _deviceIdMeta =
      const VerificationMeta('deviceId');
  @override
  late final GeneratedColumn<String> deviceId = GeneratedColumn<String>(
      'device_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _latitudeMeta =
      const VerificationMeta('latitude');
  @override
  late final GeneratedColumn<String> latitude = GeneratedColumn<String>(
      'latitude', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _longitudeMeta =
      const VerificationMeta('longitude');
  @override
  late final GeneratedColumn<String> longitude = GeneratedColumn<String>(
      'longitude', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _projectIdMeta =
      const VerificationMeta('projectId');
  @override
  late final GeneratedColumn<String> projectId = GeneratedColumn<String>(
      'project_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _categoryIdMeta =
      const VerificationMeta('categoryId');
  @override
  late final GeneratedColumn<String> categoryId = GeneratedColumn<String>(
      'category_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _dateMeta = const VerificationMeta('date');
  @override
  late final GeneratedColumn<String> date = GeneratedColumn<String>(
      'date', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _fileMeta = const VerificationMeta('file');
  @override
  late final GeneratedColumn<Uint8List> file = GeneratedColumn<Uint8List>(
      'file', aliasedName, true,
      type: DriftSqlType.blob, requiredDuringInsert: false);
  static const VerificationMeta _isInSyncMeta =
      const VerificationMeta('isInSync');
  @override
  late final GeneratedColumn<bool> isInSync = GeneratedColumn<bool>(
      'is_in_sync', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_in_sync" IN (0, 1))'),
      defaultValue: const Constant(false));
  @override
  List<GeneratedColumn> get $columns => [
        id,
        uuid,
        deviceId,
        latitude,
        longitude,
        projectId,
        categoryId,
        date,
        file,
        isInSync
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'canvas_walls';
  @override
  VerificationContext validateIntegrity(Insertable<CanvasWallRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('uuid')) {
      context.handle(
          _uuidMeta, uuid.isAcceptableOrUnknown(data['uuid']!, _uuidMeta));
    } else if (isInserting) {
      context.missing(_uuidMeta);
    }
    if (data.containsKey('device_id')) {
      context.handle(_deviceIdMeta,
          deviceId.isAcceptableOrUnknown(data['device_id']!, _deviceIdMeta));
    } else if (isInserting) {
      context.missing(_deviceIdMeta);
    }
    if (data.containsKey('latitude')) {
      context.handle(_latitudeMeta,
          latitude.isAcceptableOrUnknown(data['latitude']!, _latitudeMeta));
    } else if (isInserting) {
      context.missing(_latitudeMeta);
    }
    if (data.containsKey('longitude')) {
      context.handle(_longitudeMeta,
          longitude.isAcceptableOrUnknown(data['longitude']!, _longitudeMeta));
    } else if (isInserting) {
      context.missing(_longitudeMeta);
    }
    if (data.containsKey('project_id')) {
      context.handle(_projectIdMeta,
          projectId.isAcceptableOrUnknown(data['project_id']!, _projectIdMeta));
    } else if (isInserting) {
      context.missing(_projectIdMeta);
    }
    if (data.containsKey('category_id')) {
      context.handle(
          _categoryIdMeta,
          categoryId.isAcceptableOrUnknown(
              data['category_id']!, _categoryIdMeta));
    } else if (isInserting) {
      context.missing(_categoryIdMeta);
    }
    if (data.containsKey('date')) {
      context.handle(
          _dateMeta, date.isAcceptableOrUnknown(data['date']!, _dateMeta));
    } else if (isInserting) {
      context.missing(_dateMeta);
    }
    if (data.containsKey('file')) {
      context.handle(
          _fileMeta, file.isAcceptableOrUnknown(data['file']!, _fileMeta));
    }
    if (data.containsKey('is_in_sync')) {
      context.handle(_isInSyncMeta,
          isInSync.isAcceptableOrUnknown(data['is_in_sync']!, _isInSyncMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  CanvasWallRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return CanvasWallRow(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      uuid: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}uuid'])!,
      deviceId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}device_id'])!,
      latitude: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}latitude'])!,
      longitude: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}longitude'])!,
      projectId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}project_id'])!,
      categoryId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}category_id'])!,
      date: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}date'])!,
      file: attachedDatabase.typeMapping
          .read(DriftSqlType.blob, data['${effectivePrefix}file']),
      isInSync: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_in_sync'])!,
    );
  }

  @override
  $CanvasWallsTable createAlias(String alias) {
    return $CanvasWallsTable(attachedDatabase, alias);
  }
}

class CanvasWallRow extends DataClass implements Insertable<CanvasWallRow> {
  final int id;
  final String uuid;
  final String deviceId;
  final String latitude;
  final String longitude;
  final String projectId;
  final String categoryId;
  final String date;
  final Uint8List? file;
  final bool isInSync;
  const CanvasWallRow(
      {required this.id,
      required this.uuid,
      required this.deviceId,
      required this.latitude,
      required this.longitude,
      required this.projectId,
      required this.categoryId,
      required this.date,
      this.file,
      required this.isInSync});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['uuid'] = Variable<String>(uuid);
    map['device_id'] = Variable<String>(deviceId);
    map['latitude'] = Variable<String>(latitude);
    map['longitude'] = Variable<String>(longitude);
    map['project_id'] = Variable<String>(projectId);
    map['category_id'] = Variable<String>(categoryId);
    map['date'] = Variable<String>(date);
    if (!nullToAbsent || file != null) {
      map['file'] = Variable<Uint8List>(file);
    }
    map['is_in_sync'] = Variable<bool>(isInSync);
    return map;
  }

  CanvasWallsCompanion toCompanion(bool nullToAbsent) {
    return CanvasWallsCompanion(
      id: Value(id),
      uuid: Value(uuid),
      deviceId: Value(deviceId),
      latitude: Value(latitude),
      longitude: Value(longitude),
      projectId: Value(projectId),
      categoryId: Value(categoryId),
      date: Value(date),
      file: file == null && nullToAbsent ? const Value.absent() : Value(file),
      isInSync: Value(isInSync),
    );
  }

  factory CanvasWallRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return CanvasWallRow(
      id: serializer.fromJson<int>(json['id']),
      uuid: serializer.fromJson<String>(json['uuid']),
      deviceId: serializer.fromJson<String>(json['deviceId']),
      latitude: serializer.fromJson<String>(json['latitude']),
      longitude: serializer.fromJson<String>(json['longitude']),
      projectId: serializer.fromJson<String>(json['projectId']),
      categoryId: serializer.fromJson<String>(json['categoryId']),
      date: serializer.fromJson<String>(json['date']),
      file: serializer.fromJson<Uint8List?>(json['file']),
      isInSync: serializer.fromJson<bool>(json['isInSync']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'uuid': serializer.toJson<String>(uuid),
      'deviceId': serializer.toJson<String>(deviceId),
      'latitude': serializer.toJson<String>(latitude),
      'longitude': serializer.toJson<String>(longitude),
      'projectId': serializer.toJson<String>(projectId),
      'categoryId': serializer.toJson<String>(categoryId),
      'date': serializer.toJson<String>(date),
      'file': serializer.toJson<Uint8List?>(file),
      'isInSync': serializer.toJson<bool>(isInSync),
    };
  }

  CanvasWallRow copyWith(
          {int? id,
          String? uuid,
          String? deviceId,
          String? latitude,
          String? longitude,
          String? projectId,
          String? categoryId,
          String? date,
          Value<Uint8List?> file = const Value.absent(),
          bool? isInSync}) =>
      CanvasWallRow(
        id: id ?? this.id,
        uuid: uuid ?? this.uuid,
        deviceId: deviceId ?? this.deviceId,
        latitude: latitude ?? this.latitude,
        longitude: longitude ?? this.longitude,
        projectId: projectId ?? this.projectId,
        categoryId: categoryId ?? this.categoryId,
        date: date ?? this.date,
        file: file.present ? file.value : this.file,
        isInSync: isInSync ?? this.isInSync,
      );
  CanvasWallRow copyWithCompanion(CanvasWallsCompanion data) {
    return CanvasWallRow(
      id: data.id.present ? data.id.value : this.id,
      uuid: data.uuid.present ? data.uuid.value : this.uuid,
      deviceId: data.deviceId.present ? data.deviceId.value : this.deviceId,
      latitude: data.latitude.present ? data.latitude.value : this.latitude,
      longitude: data.longitude.present ? data.longitude.value : this.longitude,
      projectId: data.projectId.present ? data.projectId.value : this.projectId,
      categoryId:
          data.categoryId.present ? data.categoryId.value : this.categoryId,
      date: data.date.present ? data.date.value : this.date,
      file: data.file.present ? data.file.value : this.file,
      isInSync: data.isInSync.present ? data.isInSync.value : this.isInSync,
    );
  }

  @override
  String toString() {
    return (StringBuffer('CanvasWallRow(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('deviceId: $deviceId, ')
          ..write('latitude: $latitude, ')
          ..write('longitude: $longitude, ')
          ..write('projectId: $projectId, ')
          ..write('categoryId: $categoryId, ')
          ..write('date: $date, ')
          ..write('file: $file, ')
          ..write('isInSync: $isInSync')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, uuid, deviceId, latitude, longitude,
      projectId, categoryId, date, $driftBlobEquality.hash(file), isInSync);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is CanvasWallRow &&
          other.id == this.id &&
          other.uuid == this.uuid &&
          other.deviceId == this.deviceId &&
          other.latitude == this.latitude &&
          other.longitude == this.longitude &&
          other.projectId == this.projectId &&
          other.categoryId == this.categoryId &&
          other.date == this.date &&
          $driftBlobEquality.equals(other.file, this.file) &&
          other.isInSync == this.isInSync);
}

class CanvasWallsCompanion extends UpdateCompanion<CanvasWallRow> {
  final Value<int> id;
  final Value<String> uuid;
  final Value<String> deviceId;
  final Value<String> latitude;
  final Value<String> longitude;
  final Value<String> projectId;
  final Value<String> categoryId;
  final Value<String> date;
  final Value<Uint8List?> file;
  final Value<bool> isInSync;
  const CanvasWallsCompanion({
    this.id = const Value.absent(),
    this.uuid = const Value.absent(),
    this.deviceId = const Value.absent(),
    this.latitude = const Value.absent(),
    this.longitude = const Value.absent(),
    this.projectId = const Value.absent(),
    this.categoryId = const Value.absent(),
    this.date = const Value.absent(),
    this.file = const Value.absent(),
    this.isInSync = const Value.absent(),
  });
  CanvasWallsCompanion.insert({
    this.id = const Value.absent(),
    required String uuid,
    required String deviceId,
    required String latitude,
    required String longitude,
    required String projectId,
    required String categoryId,
    required String date,
    this.file = const Value.absent(),
    this.isInSync = const Value.absent(),
  })  : uuid = Value(uuid),
        deviceId = Value(deviceId),
        latitude = Value(latitude),
        longitude = Value(longitude),
        projectId = Value(projectId),
        categoryId = Value(categoryId),
        date = Value(date);
  static Insertable<CanvasWallRow> custom({
    Expression<int>? id,
    Expression<String>? uuid,
    Expression<String>? deviceId,
    Expression<String>? latitude,
    Expression<String>? longitude,
    Expression<String>? projectId,
    Expression<String>? categoryId,
    Expression<String>? date,
    Expression<Uint8List>? file,
    Expression<bool>? isInSync,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (uuid != null) 'uuid': uuid,
      if (deviceId != null) 'device_id': deviceId,
      if (latitude != null) 'latitude': latitude,
      if (longitude != null) 'longitude': longitude,
      if (projectId != null) 'project_id': projectId,
      if (categoryId != null) 'category_id': categoryId,
      if (date != null) 'date': date,
      if (file != null) 'file': file,
      if (isInSync != null) 'is_in_sync': isInSync,
    });
  }

  CanvasWallsCompanion copyWith(
      {Value<int>? id,
      Value<String>? uuid,
      Value<String>? deviceId,
      Value<String>? latitude,
      Value<String>? longitude,
      Value<String>? projectId,
      Value<String>? categoryId,
      Value<String>? date,
      Value<Uint8List?>? file,
      Value<bool>? isInSync}) {
    return CanvasWallsCompanion(
      id: id ?? this.id,
      uuid: uuid ?? this.uuid,
      deviceId: deviceId ?? this.deviceId,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      projectId: projectId ?? this.projectId,
      categoryId: categoryId ?? this.categoryId,
      date: date ?? this.date,
      file: file ?? this.file,
      isInSync: isInSync ?? this.isInSync,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (uuid.present) {
      map['uuid'] = Variable<String>(uuid.value);
    }
    if (deviceId.present) {
      map['device_id'] = Variable<String>(deviceId.value);
    }
    if (latitude.present) {
      map['latitude'] = Variable<String>(latitude.value);
    }
    if (longitude.present) {
      map['longitude'] = Variable<String>(longitude.value);
    }
    if (projectId.present) {
      map['project_id'] = Variable<String>(projectId.value);
    }
    if (categoryId.present) {
      map['category_id'] = Variable<String>(categoryId.value);
    }
    if (date.present) {
      map['date'] = Variable<String>(date.value);
    }
    if (file.present) {
      map['file'] = Variable<Uint8List>(file.value);
    }
    if (isInSync.present) {
      map['is_in_sync'] = Variable<bool>(isInSync.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('CanvasWallsCompanion(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('deviceId: $deviceId, ')
          ..write('latitude: $latitude, ')
          ..write('longitude: $longitude, ')
          ..write('projectId: $projectId, ')
          ..write('categoryId: $categoryId, ')
          ..write('date: $date, ')
          ..write('file: $file, ')
          ..write('isInSync: $isInSync')
          ..write(')'))
        .toString();
  }
}

class $VisitPeoplesTable extends VisitPeoples
    with TableInfo<$VisitPeoplesTable, VisitPeopleRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $VisitPeoplesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _uuidMeta = const VerificationMeta('uuid');
  @override
  late final GeneratedColumn<String> uuid = GeneratedColumn<String>(
      'uuid', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _deviceIdMeta =
      const VerificationMeta('deviceId');
  @override
  late final GeneratedColumn<String> deviceId = GeneratedColumn<String>(
      'device_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _openTheDoorMeta =
      const VerificationMeta('openTheDoor');
  @override
  late final GeneratedColumn<bool> openTheDoor = GeneratedColumn<bool>(
      'open_the_door', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'CHECK ("open_the_door" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _streetMeta = const VerificationMeta('street');
  @override
  late final GeneratedColumn<String> street = GeneratedColumn<String>(
      'street', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _externallNumberMeta =
      const VerificationMeta('externallNumber');
  @override
  late final GeneratedColumn<String> externallNumber = GeneratedColumn<String>(
      'externall_number', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _longitudeMeta =
      const VerificationMeta('longitude');
  @override
  late final GeneratedColumn<String> longitude = GeneratedColumn<String>(
      'longitude', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _latitudeMeta =
      const VerificationMeta('latitude');
  @override
  late final GeneratedColumn<String> latitude = GeneratedColumn<String>(
      'latitude', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _visitNumberMeta =
      const VerificationMeta('visitNumber');
  @override
  late final GeneratedColumn<String> visitNumber = GeneratedColumn<String>(
      'visit_number', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _syncVisitMeta =
      const VerificationMeta('syncVisit');
  @override
  late final GeneratedColumn<bool> syncVisit = GeneratedColumn<bool>(
      'sync_visit', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("sync_visit" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _ageMeta = const VerificationMeta('age');
  @override
  late final GeneratedColumn<String> age = GeneratedColumn<String>(
      'age', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _namesMeta = const VerificationMeta('names');
  @override
  late final GeneratedColumn<String> names = GeneratedColumn<String>(
      'names', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _lastNameMeta =
      const VerificationMeta('lastName');
  @override
  late final GeneratedColumn<String> lastName = GeneratedColumn<String>(
      'last_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _surNameMeta =
      const VerificationMeta('surName');
  @override
  late final GeneratedColumn<String> surName = GeneratedColumn<String>(
      'sur_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _phoneMeta = const VerificationMeta('phone');
  @override
  late final GeneratedColumn<String> phone = GeneratedColumn<String>(
      'phone', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _acceptInformationMeta =
      const VerificationMeta('acceptInformation');
  @override
  late final GeneratedColumn<bool> acceptInformation = GeneratedColumn<bool>(
      'accept_information', aliasedName, true,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints: GeneratedColumn.constraintIsAlways(
          'CHECK ("accept_information" IN (0, 1))'));
  static const VerificationMeta _sexMeta = const VerificationMeta('sex');
  @override
  late final GeneratedColumn<String> sex = GeneratedColumn<String>(
      'sex', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _hasSympathyMeta =
      const VerificationMeta('hasSympathy');
  @override
  late final GeneratedColumn<String> hasSympathy = GeneratedColumn<String>(
      'has_sympathy', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _createdMeta =
      const VerificationMeta('created');
  @override
  late final GeneratedColumn<DateTime> created = GeneratedColumn<DateTime>(
      'created', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        uuid,
        deviceId,
        openTheDoor,
        street,
        externallNumber,
        longitude,
        latitude,
        visitNumber,
        syncVisit,
        age,
        names,
        lastName,
        surName,
        phone,
        acceptInformation,
        sex,
        hasSympathy,
        created
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'visit_peoples';
  @override
  VerificationContext validateIntegrity(Insertable<VisitPeopleRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('uuid')) {
      context.handle(
          _uuidMeta, uuid.isAcceptableOrUnknown(data['uuid']!, _uuidMeta));
    } else if (isInserting) {
      context.missing(_uuidMeta);
    }
    if (data.containsKey('device_id')) {
      context.handle(_deviceIdMeta,
          deviceId.isAcceptableOrUnknown(data['device_id']!, _deviceIdMeta));
    } else if (isInserting) {
      context.missing(_deviceIdMeta);
    }
    if (data.containsKey('open_the_door')) {
      context.handle(
          _openTheDoorMeta,
          openTheDoor.isAcceptableOrUnknown(
              data['open_the_door']!, _openTheDoorMeta));
    }
    if (data.containsKey('street')) {
      context.handle(_streetMeta,
          street.isAcceptableOrUnknown(data['street']!, _streetMeta));
    } else if (isInserting) {
      context.missing(_streetMeta);
    }
    if (data.containsKey('externall_number')) {
      context.handle(
          _externallNumberMeta,
          externallNumber.isAcceptableOrUnknown(
              data['externall_number']!, _externallNumberMeta));
    } else if (isInserting) {
      context.missing(_externallNumberMeta);
    }
    if (data.containsKey('longitude')) {
      context.handle(_longitudeMeta,
          longitude.isAcceptableOrUnknown(data['longitude']!, _longitudeMeta));
    } else if (isInserting) {
      context.missing(_longitudeMeta);
    }
    if (data.containsKey('latitude')) {
      context.handle(_latitudeMeta,
          latitude.isAcceptableOrUnknown(data['latitude']!, _latitudeMeta));
    } else if (isInserting) {
      context.missing(_latitudeMeta);
    }
    if (data.containsKey('visit_number')) {
      context.handle(
          _visitNumberMeta,
          visitNumber.isAcceptableOrUnknown(
              data['visit_number']!, _visitNumberMeta));
    } else if (isInserting) {
      context.missing(_visitNumberMeta);
    }
    if (data.containsKey('sync_visit')) {
      context.handle(_syncVisitMeta,
          syncVisit.isAcceptableOrUnknown(data['sync_visit']!, _syncVisitMeta));
    }
    if (data.containsKey('age')) {
      context.handle(
          _ageMeta, age.isAcceptableOrUnknown(data['age']!, _ageMeta));
    }
    if (data.containsKey('names')) {
      context.handle(
          _namesMeta, names.isAcceptableOrUnknown(data['names']!, _namesMeta));
    }
    if (data.containsKey('last_name')) {
      context.handle(_lastNameMeta,
          lastName.isAcceptableOrUnknown(data['last_name']!, _lastNameMeta));
    }
    if (data.containsKey('sur_name')) {
      context.handle(_surNameMeta,
          surName.isAcceptableOrUnknown(data['sur_name']!, _surNameMeta));
    }
    if (data.containsKey('phone')) {
      context.handle(
          _phoneMeta, phone.isAcceptableOrUnknown(data['phone']!, _phoneMeta));
    }
    if (data.containsKey('accept_information')) {
      context.handle(
          _acceptInformationMeta,
          acceptInformation.isAcceptableOrUnknown(
              data['accept_information']!, _acceptInformationMeta));
    }
    if (data.containsKey('sex')) {
      context.handle(
          _sexMeta, sex.isAcceptableOrUnknown(data['sex']!, _sexMeta));
    }
    if (data.containsKey('has_sympathy')) {
      context.handle(
          _hasSympathyMeta,
          hasSympathy.isAcceptableOrUnknown(
              data['has_sympathy']!, _hasSympathyMeta));
    }
    if (data.containsKey('created')) {
      context.handle(_createdMeta,
          created.isAcceptableOrUnknown(data['created']!, _createdMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  VisitPeopleRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return VisitPeopleRow(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      uuid: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}uuid'])!,
      deviceId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}device_id'])!,
      openTheDoor: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}open_the_door'])!,
      street: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}street'])!,
      externallNumber: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}externall_number'])!,
      longitude: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}longitude'])!,
      latitude: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}latitude'])!,
      visitNumber: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}visit_number'])!,
      syncVisit: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}sync_visit'])!,
      age: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}age']),
      names: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}names']),
      lastName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}last_name']),
      surName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sur_name']),
      phone: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}phone']),
      acceptInformation: attachedDatabase.typeMapping.read(
          DriftSqlType.bool, data['${effectivePrefix}accept_information']),
      sex: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sex']),
      hasSympathy: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}has_sympathy']),
      created: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created']),
    );
  }

  @override
  $VisitPeoplesTable createAlias(String alias) {
    return $VisitPeoplesTable(attachedDatabase, alias);
  }
}

class VisitPeopleRow extends DataClass implements Insertable<VisitPeopleRow> {
  final int id;
  final String uuid;
  final String deviceId;
  final bool openTheDoor;
  final String street;
  final String externallNumber;
  final String longitude;
  final String latitude;
  final String visitNumber;
  final bool syncVisit;
  final String? age;
  final String? names;
  final String? lastName;
  final String? surName;
  final String? phone;
  final bool? acceptInformation;
  final String? sex;
  final String? hasSympathy;
  final DateTime? created;
  const VisitPeopleRow(
      {required this.id,
      required this.uuid,
      required this.deviceId,
      required this.openTheDoor,
      required this.street,
      required this.externallNumber,
      required this.longitude,
      required this.latitude,
      required this.visitNumber,
      required this.syncVisit,
      this.age,
      this.names,
      this.lastName,
      this.surName,
      this.phone,
      this.acceptInformation,
      this.sex,
      this.hasSympathy,
      this.created});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['uuid'] = Variable<String>(uuid);
    map['device_id'] = Variable<String>(deviceId);
    map['open_the_door'] = Variable<bool>(openTheDoor);
    map['street'] = Variable<String>(street);
    map['externall_number'] = Variable<String>(externallNumber);
    map['longitude'] = Variable<String>(longitude);
    map['latitude'] = Variable<String>(latitude);
    map['visit_number'] = Variable<String>(visitNumber);
    map['sync_visit'] = Variable<bool>(syncVisit);
    if (!nullToAbsent || age != null) {
      map['age'] = Variable<String>(age);
    }
    if (!nullToAbsent || names != null) {
      map['names'] = Variable<String>(names);
    }
    if (!nullToAbsent || lastName != null) {
      map['last_name'] = Variable<String>(lastName);
    }
    if (!nullToAbsent || surName != null) {
      map['sur_name'] = Variable<String>(surName);
    }
    if (!nullToAbsent || phone != null) {
      map['phone'] = Variable<String>(phone);
    }
    if (!nullToAbsent || acceptInformation != null) {
      map['accept_information'] = Variable<bool>(acceptInformation);
    }
    if (!nullToAbsent || sex != null) {
      map['sex'] = Variable<String>(sex);
    }
    if (!nullToAbsent || hasSympathy != null) {
      map['has_sympathy'] = Variable<String>(hasSympathy);
    }
    if (!nullToAbsent || created != null) {
      map['created'] = Variable<DateTime>(created);
    }
    return map;
  }

  VisitPeoplesCompanion toCompanion(bool nullToAbsent) {
    return VisitPeoplesCompanion(
      id: Value(id),
      uuid: Value(uuid),
      deviceId: Value(deviceId),
      openTheDoor: Value(openTheDoor),
      street: Value(street),
      externallNumber: Value(externallNumber),
      longitude: Value(longitude),
      latitude: Value(latitude),
      visitNumber: Value(visitNumber),
      syncVisit: Value(syncVisit),
      age: age == null && nullToAbsent ? const Value.absent() : Value(age),
      names:
          names == null && nullToAbsent ? const Value.absent() : Value(names),
      lastName: lastName == null && nullToAbsent
          ? const Value.absent()
          : Value(lastName),
      surName: surName == null && nullToAbsent
          ? const Value.absent()
          : Value(surName),
      phone:
          phone == null && nullToAbsent ? const Value.absent() : Value(phone),
      acceptInformation: acceptInformation == null && nullToAbsent
          ? const Value.absent()
          : Value(acceptInformation),
      sex: sex == null && nullToAbsent ? const Value.absent() : Value(sex),
      hasSympathy: hasSympathy == null && nullToAbsent
          ? const Value.absent()
          : Value(hasSympathy),
      created: created == null && nullToAbsent
          ? const Value.absent()
          : Value(created),
    );
  }

  factory VisitPeopleRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return VisitPeopleRow(
      id: serializer.fromJson<int>(json['id']),
      uuid: serializer.fromJson<String>(json['uuid']),
      deviceId: serializer.fromJson<String>(json['deviceId']),
      openTheDoor: serializer.fromJson<bool>(json['openTheDoor']),
      street: serializer.fromJson<String>(json['street']),
      externallNumber: serializer.fromJson<String>(json['externallNumber']),
      longitude: serializer.fromJson<String>(json['longitude']),
      latitude: serializer.fromJson<String>(json['latitude']),
      visitNumber: serializer.fromJson<String>(json['visitNumber']),
      syncVisit: serializer.fromJson<bool>(json['syncVisit']),
      age: serializer.fromJson<String?>(json['age']),
      names: serializer.fromJson<String?>(json['names']),
      lastName: serializer.fromJson<String?>(json['lastName']),
      surName: serializer.fromJson<String?>(json['surName']),
      phone: serializer.fromJson<String?>(json['phone']),
      acceptInformation: serializer.fromJson<bool?>(json['acceptInformation']),
      sex: serializer.fromJson<String?>(json['sex']),
      hasSympathy: serializer.fromJson<String?>(json['hasSympathy']),
      created: serializer.fromJson<DateTime?>(json['created']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'uuid': serializer.toJson<String>(uuid),
      'deviceId': serializer.toJson<String>(deviceId),
      'openTheDoor': serializer.toJson<bool>(openTheDoor),
      'street': serializer.toJson<String>(street),
      'externallNumber': serializer.toJson<String>(externallNumber),
      'longitude': serializer.toJson<String>(longitude),
      'latitude': serializer.toJson<String>(latitude),
      'visitNumber': serializer.toJson<String>(visitNumber),
      'syncVisit': serializer.toJson<bool>(syncVisit),
      'age': serializer.toJson<String?>(age),
      'names': serializer.toJson<String?>(names),
      'lastName': serializer.toJson<String?>(lastName),
      'surName': serializer.toJson<String?>(surName),
      'phone': serializer.toJson<String?>(phone),
      'acceptInformation': serializer.toJson<bool?>(acceptInformation),
      'sex': serializer.toJson<String?>(sex),
      'hasSympathy': serializer.toJson<String?>(hasSympathy),
      'created': serializer.toJson<DateTime?>(created),
    };
  }

  VisitPeopleRow copyWith(
          {int? id,
          String? uuid,
          String? deviceId,
          bool? openTheDoor,
          String? street,
          String? externallNumber,
          String? longitude,
          String? latitude,
          String? visitNumber,
          bool? syncVisit,
          Value<String?> age = const Value.absent(),
          Value<String?> names = const Value.absent(),
          Value<String?> lastName = const Value.absent(),
          Value<String?> surName = const Value.absent(),
          Value<String?> phone = const Value.absent(),
          Value<bool?> acceptInformation = const Value.absent(),
          Value<String?> sex = const Value.absent(),
          Value<String?> hasSympathy = const Value.absent(),
          Value<DateTime?> created = const Value.absent()}) =>
      VisitPeopleRow(
        id: id ?? this.id,
        uuid: uuid ?? this.uuid,
        deviceId: deviceId ?? this.deviceId,
        openTheDoor: openTheDoor ?? this.openTheDoor,
        street: street ?? this.street,
        externallNumber: externallNumber ?? this.externallNumber,
        longitude: longitude ?? this.longitude,
        latitude: latitude ?? this.latitude,
        visitNumber: visitNumber ?? this.visitNumber,
        syncVisit: syncVisit ?? this.syncVisit,
        age: age.present ? age.value : this.age,
        names: names.present ? names.value : this.names,
        lastName: lastName.present ? lastName.value : this.lastName,
        surName: surName.present ? surName.value : this.surName,
        phone: phone.present ? phone.value : this.phone,
        acceptInformation: acceptInformation.present
            ? acceptInformation.value
            : this.acceptInformation,
        sex: sex.present ? sex.value : this.sex,
        hasSympathy: hasSympathy.present ? hasSympathy.value : this.hasSympathy,
        created: created.present ? created.value : this.created,
      );
  VisitPeopleRow copyWithCompanion(VisitPeoplesCompanion data) {
    return VisitPeopleRow(
      id: data.id.present ? data.id.value : this.id,
      uuid: data.uuid.present ? data.uuid.value : this.uuid,
      deviceId: data.deviceId.present ? data.deviceId.value : this.deviceId,
      openTheDoor:
          data.openTheDoor.present ? data.openTheDoor.value : this.openTheDoor,
      street: data.street.present ? data.street.value : this.street,
      externallNumber: data.externallNumber.present
          ? data.externallNumber.value
          : this.externallNumber,
      longitude: data.longitude.present ? data.longitude.value : this.longitude,
      latitude: data.latitude.present ? data.latitude.value : this.latitude,
      visitNumber:
          data.visitNumber.present ? data.visitNumber.value : this.visitNumber,
      syncVisit: data.syncVisit.present ? data.syncVisit.value : this.syncVisit,
      age: data.age.present ? data.age.value : this.age,
      names: data.names.present ? data.names.value : this.names,
      lastName: data.lastName.present ? data.lastName.value : this.lastName,
      surName: data.surName.present ? data.surName.value : this.surName,
      phone: data.phone.present ? data.phone.value : this.phone,
      acceptInformation: data.acceptInformation.present
          ? data.acceptInformation.value
          : this.acceptInformation,
      sex: data.sex.present ? data.sex.value : this.sex,
      hasSympathy:
          data.hasSympathy.present ? data.hasSympathy.value : this.hasSympathy,
      created: data.created.present ? data.created.value : this.created,
    );
  }

  @override
  String toString() {
    return (StringBuffer('VisitPeopleRow(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('deviceId: $deviceId, ')
          ..write('openTheDoor: $openTheDoor, ')
          ..write('street: $street, ')
          ..write('externallNumber: $externallNumber, ')
          ..write('longitude: $longitude, ')
          ..write('latitude: $latitude, ')
          ..write('visitNumber: $visitNumber, ')
          ..write('syncVisit: $syncVisit, ')
          ..write('age: $age, ')
          ..write('names: $names, ')
          ..write('lastName: $lastName, ')
          ..write('surName: $surName, ')
          ..write('phone: $phone, ')
          ..write('acceptInformation: $acceptInformation, ')
          ..write('sex: $sex, ')
          ..write('hasSympathy: $hasSympathy, ')
          ..write('created: $created')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id,
      uuid,
      deviceId,
      openTheDoor,
      street,
      externallNumber,
      longitude,
      latitude,
      visitNumber,
      syncVisit,
      age,
      names,
      lastName,
      surName,
      phone,
      acceptInformation,
      sex,
      hasSympathy,
      created);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is VisitPeopleRow &&
          other.id == this.id &&
          other.uuid == this.uuid &&
          other.deviceId == this.deviceId &&
          other.openTheDoor == this.openTheDoor &&
          other.street == this.street &&
          other.externallNumber == this.externallNumber &&
          other.longitude == this.longitude &&
          other.latitude == this.latitude &&
          other.visitNumber == this.visitNumber &&
          other.syncVisit == this.syncVisit &&
          other.age == this.age &&
          other.names == this.names &&
          other.lastName == this.lastName &&
          other.surName == this.surName &&
          other.phone == this.phone &&
          other.acceptInformation == this.acceptInformation &&
          other.sex == this.sex &&
          other.hasSympathy == this.hasSympathy &&
          other.created == this.created);
}

class VisitPeoplesCompanion extends UpdateCompanion<VisitPeopleRow> {
  final Value<int> id;
  final Value<String> uuid;
  final Value<String> deviceId;
  final Value<bool> openTheDoor;
  final Value<String> street;
  final Value<String> externallNumber;
  final Value<String> longitude;
  final Value<String> latitude;
  final Value<String> visitNumber;
  final Value<bool> syncVisit;
  final Value<String?> age;
  final Value<String?> names;
  final Value<String?> lastName;
  final Value<String?> surName;
  final Value<String?> phone;
  final Value<bool?> acceptInformation;
  final Value<String?> sex;
  final Value<String?> hasSympathy;
  final Value<DateTime?> created;
  const VisitPeoplesCompanion({
    this.id = const Value.absent(),
    this.uuid = const Value.absent(),
    this.deviceId = const Value.absent(),
    this.openTheDoor = const Value.absent(),
    this.street = const Value.absent(),
    this.externallNumber = const Value.absent(),
    this.longitude = const Value.absent(),
    this.latitude = const Value.absent(),
    this.visitNumber = const Value.absent(),
    this.syncVisit = const Value.absent(),
    this.age = const Value.absent(),
    this.names = const Value.absent(),
    this.lastName = const Value.absent(),
    this.surName = const Value.absent(),
    this.phone = const Value.absent(),
    this.acceptInformation = const Value.absent(),
    this.sex = const Value.absent(),
    this.hasSympathy = const Value.absent(),
    this.created = const Value.absent(),
  });
  VisitPeoplesCompanion.insert({
    this.id = const Value.absent(),
    required String uuid,
    required String deviceId,
    this.openTheDoor = const Value.absent(),
    required String street,
    required String externallNumber,
    required String longitude,
    required String latitude,
    required String visitNumber,
    this.syncVisit = const Value.absent(),
    this.age = const Value.absent(),
    this.names = const Value.absent(),
    this.lastName = const Value.absent(),
    this.surName = const Value.absent(),
    this.phone = const Value.absent(),
    this.acceptInformation = const Value.absent(),
    this.sex = const Value.absent(),
    this.hasSympathy = const Value.absent(),
    this.created = const Value.absent(),
  })  : uuid = Value(uuid),
        deviceId = Value(deviceId),
        street = Value(street),
        externallNumber = Value(externallNumber),
        longitude = Value(longitude),
        latitude = Value(latitude),
        visitNumber = Value(visitNumber);
  static Insertable<VisitPeopleRow> custom({
    Expression<int>? id,
    Expression<String>? uuid,
    Expression<String>? deviceId,
    Expression<bool>? openTheDoor,
    Expression<String>? street,
    Expression<String>? externallNumber,
    Expression<String>? longitude,
    Expression<String>? latitude,
    Expression<String>? visitNumber,
    Expression<bool>? syncVisit,
    Expression<String>? age,
    Expression<String>? names,
    Expression<String>? lastName,
    Expression<String>? surName,
    Expression<String>? phone,
    Expression<bool>? acceptInformation,
    Expression<String>? sex,
    Expression<String>? hasSympathy,
    Expression<DateTime>? created,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (uuid != null) 'uuid': uuid,
      if (deviceId != null) 'device_id': deviceId,
      if (openTheDoor != null) 'open_the_door': openTheDoor,
      if (street != null) 'street': street,
      if (externallNumber != null) 'externall_number': externallNumber,
      if (longitude != null) 'longitude': longitude,
      if (latitude != null) 'latitude': latitude,
      if (visitNumber != null) 'visit_number': visitNumber,
      if (syncVisit != null) 'sync_visit': syncVisit,
      if (age != null) 'age': age,
      if (names != null) 'names': names,
      if (lastName != null) 'last_name': lastName,
      if (surName != null) 'sur_name': surName,
      if (phone != null) 'phone': phone,
      if (acceptInformation != null) 'accept_information': acceptInformation,
      if (sex != null) 'sex': sex,
      if (hasSympathy != null) 'has_sympathy': hasSympathy,
      if (created != null) 'created': created,
    });
  }

  VisitPeoplesCompanion copyWith(
      {Value<int>? id,
      Value<String>? uuid,
      Value<String>? deviceId,
      Value<bool>? openTheDoor,
      Value<String>? street,
      Value<String>? externallNumber,
      Value<String>? longitude,
      Value<String>? latitude,
      Value<String>? visitNumber,
      Value<bool>? syncVisit,
      Value<String?>? age,
      Value<String?>? names,
      Value<String?>? lastName,
      Value<String?>? surName,
      Value<String?>? phone,
      Value<bool?>? acceptInformation,
      Value<String?>? sex,
      Value<String?>? hasSympathy,
      Value<DateTime?>? created}) {
    return VisitPeoplesCompanion(
      id: id ?? this.id,
      uuid: uuid ?? this.uuid,
      deviceId: deviceId ?? this.deviceId,
      openTheDoor: openTheDoor ?? this.openTheDoor,
      street: street ?? this.street,
      externallNumber: externallNumber ?? this.externallNumber,
      longitude: longitude ?? this.longitude,
      latitude: latitude ?? this.latitude,
      visitNumber: visitNumber ?? this.visitNumber,
      syncVisit: syncVisit ?? this.syncVisit,
      age: age ?? this.age,
      names: names ?? this.names,
      lastName: lastName ?? this.lastName,
      surName: surName ?? this.surName,
      phone: phone ?? this.phone,
      acceptInformation: acceptInformation ?? this.acceptInformation,
      sex: sex ?? this.sex,
      hasSympathy: hasSympathy ?? this.hasSympathy,
      created: created ?? this.created,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (uuid.present) {
      map['uuid'] = Variable<String>(uuid.value);
    }
    if (deviceId.present) {
      map['device_id'] = Variable<String>(deviceId.value);
    }
    if (openTheDoor.present) {
      map['open_the_door'] = Variable<bool>(openTheDoor.value);
    }
    if (street.present) {
      map['street'] = Variable<String>(street.value);
    }
    if (externallNumber.present) {
      map['externall_number'] = Variable<String>(externallNumber.value);
    }
    if (longitude.present) {
      map['longitude'] = Variable<String>(longitude.value);
    }
    if (latitude.present) {
      map['latitude'] = Variable<String>(latitude.value);
    }
    if (visitNumber.present) {
      map['visit_number'] = Variable<String>(visitNumber.value);
    }
    if (syncVisit.present) {
      map['sync_visit'] = Variable<bool>(syncVisit.value);
    }
    if (age.present) {
      map['age'] = Variable<String>(age.value);
    }
    if (names.present) {
      map['names'] = Variable<String>(names.value);
    }
    if (lastName.present) {
      map['last_name'] = Variable<String>(lastName.value);
    }
    if (surName.present) {
      map['sur_name'] = Variable<String>(surName.value);
    }
    if (phone.present) {
      map['phone'] = Variable<String>(phone.value);
    }
    if (acceptInformation.present) {
      map['accept_information'] = Variable<bool>(acceptInformation.value);
    }
    if (sex.present) {
      map['sex'] = Variable<String>(sex.value);
    }
    if (hasSympathy.present) {
      map['has_sympathy'] = Variable<String>(hasSympathy.value);
    }
    if (created.present) {
      map['created'] = Variable<DateTime>(created.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('VisitPeoplesCompanion(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('deviceId: $deviceId, ')
          ..write('openTheDoor: $openTheDoor, ')
          ..write('street: $street, ')
          ..write('externallNumber: $externallNumber, ')
          ..write('longitude: $longitude, ')
          ..write('latitude: $latitude, ')
          ..write('visitNumber: $visitNumber, ')
          ..write('syncVisit: $syncVisit, ')
          ..write('age: $age, ')
          ..write('names: $names, ')
          ..write('lastName: $lastName, ')
          ..write('surName: $surName, ')
          ..write('phone: $phone, ')
          ..write('acceptInformation: $acceptInformation, ')
          ..write('sex: $sex, ')
          ..write('hasSympathy: $hasSympathy, ')
          ..write('created: $created')
          ..write(')'))
        .toString();
  }
}

class $FollowersTable extends Followers
    with TableInfo<$FollowersTable, FollowerRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $FollowersTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _uuidMeta = const VerificationMeta('uuid');
  @override
  late final GeneratedColumn<String> uuid = GeneratedColumn<String>(
      'uuid', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _deviceIdMeta =
      const VerificationMeta('deviceId');
  @override
  late final GeneratedColumn<String> deviceId = GeneratedColumn<String>(
      'device_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _latitudeMeta =
      const VerificationMeta('latitude');
  @override
  late final GeneratedColumn<String> latitude = GeneratedColumn<String>(
      'latitude', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _longitudeMeta =
      const VerificationMeta('longitude');
  @override
  late final GeneratedColumn<String> longitude = GeneratedColumn<String>(
      'longitude', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _sectionNumberMeta =
      const VerificationMeta('sectionNumber');
  @override
  late final GeneratedColumn<String> sectionNumber = GeneratedColumn<String>(
      'section_number', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _streetMeta = const VerificationMeta('street');
  @override
  late final GeneratedColumn<String> street = GeneratedColumn<String>(
      'street', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _externalNumberMeta =
      const VerificationMeta('externalNumber');
  @override
  late final GeneratedColumn<String> externalNumber = GeneratedColumn<String>(
      'external_number', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _internalNumberMeta =
      const VerificationMeta('internalNumber');
  @override
  late final GeneratedColumn<String> internalNumber = GeneratedColumn<String>(
      'internal_number', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _zipCodeMeta =
      const VerificationMeta('zipCode');
  @override
  late final GeneratedColumn<String> zipCode = GeneratedColumn<String>(
      'zip_code', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _neighborhoodMeta =
      const VerificationMeta('neighborhood');
  @override
  late final GeneratedColumn<String> neighborhood = GeneratedColumn<String>(
      'neighborhood', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _sectionNumberIneMeta =
      const VerificationMeta('sectionNumberIne');
  @override
  late final GeneratedColumn<String> sectionNumberIne = GeneratedColumn<String>(
      'section_number_ine', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _residenceEntityIdMeta =
      const VerificationMeta('residenceEntityId');
  @override
  late final GeneratedColumn<int> residenceEntityId = GeneratedColumn<int>(
      'residence_entity_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _residenceMunicipalityIdMeta =
      const VerificationMeta('residenceMunicipalityId');
  @override
  late final GeneratedColumn<int> residenceMunicipalityId =
      GeneratedColumn<int>('residence_municipality_id', aliasedName, true,
          type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _isAdultMeta =
      const VerificationMeta('isAdult');
  @override
  late final GeneratedColumn<bool> isAdult = GeneratedColumn<bool>(
      'is_adult', aliasedName, true,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_adult" IN (0, 1))'));
  static const VerificationMeta _namesMeta = const VerificationMeta('names');
  @override
  late final GeneratedColumn<String> names = GeneratedColumn<String>(
      'names', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _lastNameMeta =
      const VerificationMeta('lastName');
  @override
  late final GeneratedColumn<String> lastName = GeneratedColumn<String>(
      'last_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _surNameMeta =
      const VerificationMeta('surName');
  @override
  late final GeneratedColumn<String> surName = GeneratedColumn<String>(
      'sur_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _curpMeta = const VerificationMeta('curp');
  @override
  late final GeneratedColumn<String> curp = GeneratedColumn<String>(
      'curp', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _ineMeta = const VerificationMeta('ine');
  @override
  late final GeneratedColumn<String> ine = GeneratedColumn<String>(
      'ine', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _validityMeta =
      const VerificationMeta('validity');
  @override
  late final GeneratedColumn<String> validity = GeneratedColumn<String>(
      'validity', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _birthdateMeta =
      const VerificationMeta('birthdate');
  @override
  late final GeneratedColumn<String> birthdate = GeneratedColumn<String>(
      'birthdate', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _cellPhoneNumberMeta =
      const VerificationMeta('cellPhoneNumber');
  @override
  late final GeneratedColumn<String> cellPhoneNumber = GeneratedColumn<String>(
      'cell_phone_number', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _supportsWhatsappMeta =
      const VerificationMeta('supportsWhatsapp');
  @override
  late final GeneratedColumn<String> supportsWhatsapp = GeneratedColumn<String>(
      'supports_whatsapp', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _cellPhoneNumberOtherMeta =
      const VerificationMeta('cellPhoneNumberOther');
  @override
  late final GeneratedColumn<String> cellPhoneNumberOther =
      GeneratedColumn<String>('cell_phone_number_other', aliasedName, true,
          type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _emailMeta = const VerificationMeta('email');
  @override
  late final GeneratedColumn<String> email = GeneratedColumn<String>(
      'email', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _catSexIdMeta =
      const VerificationMeta('catSexId');
  @override
  late final GeneratedColumn<int> catSexId = GeneratedColumn<int>(
      'cat_sex_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _catSexNombreMeta =
      const VerificationMeta('catSexNombre');
  @override
  late final GeneratedColumn<String> catSexNombre = GeneratedColumn<String>(
      'cat_sex_nombre', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _birthEntityIdMeta =
      const VerificationMeta('birthEntityId');
  @override
  late final GeneratedColumn<int> birthEntityId = GeneratedColumn<int>(
      'birth_entity_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _isInSyncMeta =
      const VerificationMeta('isInSync');
  @override
  late final GeneratedColumn<bool> isInSync = GeneratedColumn<bool>(
      'is_in_sync', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_in_sync" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _supportsMovementMeta =
      const VerificationMeta('supportsMovement');
  @override
  late final GeneratedColumn<String> supportsMovement = GeneratedColumn<String>(
      'supports_movement', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _committeeParticipationMeta =
      const VerificationMeta('committeeParticipation');
  @override
  late final GeneratedColumn<String> committeeParticipation =
      GeneratedColumn<String>('committee_participation', aliasedName, true,
          type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _pollinStationRepresentativeParticipationMeta =
      const VerificationMeta('pollinStationRepresentativeParticipation');
  @override
  late final GeneratedColumn<String> pollinStationRepresentativeParticipation =
      GeneratedColumn<String>(
          'pollin_station_representative_participation', aliasedName, true,
          type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _invitingOthersToParticipateMeta =
      const VerificationMeta('invitingOthersToParticipate');
  @override
  late final GeneratedColumn<String> invitingOthersToParticipate =
      GeneratedColumn<String>(
          'inviting_others_to_participate', aliasedName, true,
          type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _numberOfPeopleInvitedMeta =
      const VerificationMeta('numberOfPeopleInvited');
  @override
  late final GeneratedColumn<String> numberOfPeopleInvited =
      GeneratedColumn<String>('number_of_people_invited', aliasedName, true,
          type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _createdMeta =
      const VerificationMeta('created');
  @override
  late final GeneratedColumn<DateTime> created = GeneratedColumn<DateTime>(
      'created', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  static const VerificationMeta _positionIdMeta =
      const VerificationMeta('positionId');
  @override
  late final GeneratedColumn<int> positionId = GeneratedColumn<int>(
      'position_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _syncStatusMeta =
      const VerificationMeta('syncStatus');
  @override
  late final GeneratedColumn<String> syncStatus = GeneratedColumn<String>(
      'sync_status', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('PENDING'));
  static const VerificationMeta _syncErrorMeta =
      const VerificationMeta('syncError');
  @override
  late final GeneratedColumn<String> syncError = GeneratedColumn<String>(
      'sync_error', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _membershipIdMeta =
      const VerificationMeta('membershipId');
  @override
  late final GeneratedColumn<int> membershipId = GeneratedColumn<int>(
      'membership_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _positionLabelMeta =
      const VerificationMeta('positionLabel');
  @override
  late final GeneratedColumn<String> positionLabel = GeneratedColumn<String>(
      'position_label', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _syncedAtMeta =
      const VerificationMeta('syncedAt');
  @override
  late final GeneratedColumn<DateTime> syncedAt = GeneratedColumn<DateTime>(
      'synced_at', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        uuid,
        deviceId,
        latitude,
        longitude,
        sectionNumber,
        street,
        externalNumber,
        internalNumber,
        zipCode,
        neighborhood,
        sectionNumberIne,
        residenceEntityId,
        residenceMunicipalityId,
        isAdult,
        names,
        lastName,
        surName,
        curp,
        ine,
        validity,
        birthdate,
        cellPhoneNumber,
        supportsWhatsapp,
        cellPhoneNumberOther,
        email,
        catSexId,
        catSexNombre,
        birthEntityId,
        isInSync,
        supportsMovement,
        committeeParticipation,
        pollinStationRepresentativeParticipation,
        invitingOthersToParticipate,
        numberOfPeopleInvited,
        created,
        positionId,
        syncStatus,
        syncError,
        membershipId,
        positionLabel,
        syncedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'followers';
  @override
  VerificationContext validateIntegrity(Insertable<FollowerRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('uuid')) {
      context.handle(
          _uuidMeta, uuid.isAcceptableOrUnknown(data['uuid']!, _uuidMeta));
    } else if (isInserting) {
      context.missing(_uuidMeta);
    }
    if (data.containsKey('device_id')) {
      context.handle(_deviceIdMeta,
          deviceId.isAcceptableOrUnknown(data['device_id']!, _deviceIdMeta));
    } else if (isInserting) {
      context.missing(_deviceIdMeta);
    }
    if (data.containsKey('latitude')) {
      context.handle(_latitudeMeta,
          latitude.isAcceptableOrUnknown(data['latitude']!, _latitudeMeta));
    } else if (isInserting) {
      context.missing(_latitudeMeta);
    }
    if (data.containsKey('longitude')) {
      context.handle(_longitudeMeta,
          longitude.isAcceptableOrUnknown(data['longitude']!, _longitudeMeta));
    } else if (isInserting) {
      context.missing(_longitudeMeta);
    }
    if (data.containsKey('section_number')) {
      context.handle(
          _sectionNumberMeta,
          sectionNumber.isAcceptableOrUnknown(
              data['section_number']!, _sectionNumberMeta));
    } else if (isInserting) {
      context.missing(_sectionNumberMeta);
    }
    if (data.containsKey('street')) {
      context.handle(_streetMeta,
          street.isAcceptableOrUnknown(data['street']!, _streetMeta));
    } else if (isInserting) {
      context.missing(_streetMeta);
    }
    if (data.containsKey('external_number')) {
      context.handle(
          _externalNumberMeta,
          externalNumber.isAcceptableOrUnknown(
              data['external_number']!, _externalNumberMeta));
    } else if (isInserting) {
      context.missing(_externalNumberMeta);
    }
    if (data.containsKey('internal_number')) {
      context.handle(
          _internalNumberMeta,
          internalNumber.isAcceptableOrUnknown(
              data['internal_number']!, _internalNumberMeta));
    }
    if (data.containsKey('zip_code')) {
      context.handle(_zipCodeMeta,
          zipCode.isAcceptableOrUnknown(data['zip_code']!, _zipCodeMeta));
    }
    if (data.containsKey('neighborhood')) {
      context.handle(
          _neighborhoodMeta,
          neighborhood.isAcceptableOrUnknown(
              data['neighborhood']!, _neighborhoodMeta));
    }
    if (data.containsKey('section_number_ine')) {
      context.handle(
          _sectionNumberIneMeta,
          sectionNumberIne.isAcceptableOrUnknown(
              data['section_number_ine']!, _sectionNumberIneMeta));
    }
    if (data.containsKey('residence_entity_id')) {
      context.handle(
          _residenceEntityIdMeta,
          residenceEntityId.isAcceptableOrUnknown(
              data['residence_entity_id']!, _residenceEntityIdMeta));
    }
    if (data.containsKey('residence_municipality_id')) {
      context.handle(
          _residenceMunicipalityIdMeta,
          residenceMunicipalityId.isAcceptableOrUnknown(
              data['residence_municipality_id']!,
              _residenceMunicipalityIdMeta));
    }
    if (data.containsKey('is_adult')) {
      context.handle(_isAdultMeta,
          isAdult.isAcceptableOrUnknown(data['is_adult']!, _isAdultMeta));
    }
    if (data.containsKey('names')) {
      context.handle(
          _namesMeta, names.isAcceptableOrUnknown(data['names']!, _namesMeta));
    }
    if (data.containsKey('last_name')) {
      context.handle(_lastNameMeta,
          lastName.isAcceptableOrUnknown(data['last_name']!, _lastNameMeta));
    }
    if (data.containsKey('sur_name')) {
      context.handle(_surNameMeta,
          surName.isAcceptableOrUnknown(data['sur_name']!, _surNameMeta));
    }
    if (data.containsKey('curp')) {
      context.handle(
          _curpMeta, curp.isAcceptableOrUnknown(data['curp']!, _curpMeta));
    }
    if (data.containsKey('ine')) {
      context.handle(
          _ineMeta, ine.isAcceptableOrUnknown(data['ine']!, _ineMeta));
    }
    if (data.containsKey('validity')) {
      context.handle(_validityMeta,
          validity.isAcceptableOrUnknown(data['validity']!, _validityMeta));
    }
    if (data.containsKey('birthdate')) {
      context.handle(_birthdateMeta,
          birthdate.isAcceptableOrUnknown(data['birthdate']!, _birthdateMeta));
    }
    if (data.containsKey('cell_phone_number')) {
      context.handle(
          _cellPhoneNumberMeta,
          cellPhoneNumber.isAcceptableOrUnknown(
              data['cell_phone_number']!, _cellPhoneNumberMeta));
    }
    if (data.containsKey('supports_whatsapp')) {
      context.handle(
          _supportsWhatsappMeta,
          supportsWhatsapp.isAcceptableOrUnknown(
              data['supports_whatsapp']!, _supportsWhatsappMeta));
    }
    if (data.containsKey('cell_phone_number_other')) {
      context.handle(
          _cellPhoneNumberOtherMeta,
          cellPhoneNumberOther.isAcceptableOrUnknown(
              data['cell_phone_number_other']!, _cellPhoneNumberOtherMeta));
    }
    if (data.containsKey('email')) {
      context.handle(
          _emailMeta, email.isAcceptableOrUnknown(data['email']!, _emailMeta));
    }
    if (data.containsKey('cat_sex_id')) {
      context.handle(_catSexIdMeta,
          catSexId.isAcceptableOrUnknown(data['cat_sex_id']!, _catSexIdMeta));
    }
    if (data.containsKey('cat_sex_nombre')) {
      context.handle(
          _catSexNombreMeta,
          catSexNombre.isAcceptableOrUnknown(
              data['cat_sex_nombre']!, _catSexNombreMeta));
    }
    if (data.containsKey('birth_entity_id')) {
      context.handle(
          _birthEntityIdMeta,
          birthEntityId.isAcceptableOrUnknown(
              data['birth_entity_id']!, _birthEntityIdMeta));
    }
    if (data.containsKey('is_in_sync')) {
      context.handle(_isInSyncMeta,
          isInSync.isAcceptableOrUnknown(data['is_in_sync']!, _isInSyncMeta));
    }
    if (data.containsKey('supports_movement')) {
      context.handle(
          _supportsMovementMeta,
          supportsMovement.isAcceptableOrUnknown(
              data['supports_movement']!, _supportsMovementMeta));
    } else if (isInserting) {
      context.missing(_supportsMovementMeta);
    }
    if (data.containsKey('committee_participation')) {
      context.handle(
          _committeeParticipationMeta,
          committeeParticipation.isAcceptableOrUnknown(
              data['committee_participation']!, _committeeParticipationMeta));
    }
    if (data.containsKey('pollin_station_representative_participation')) {
      context.handle(
          _pollinStationRepresentativeParticipationMeta,
          pollinStationRepresentativeParticipation.isAcceptableOrUnknown(
              data['pollin_station_representative_participation']!,
              _pollinStationRepresentativeParticipationMeta));
    }
    if (data.containsKey('inviting_others_to_participate')) {
      context.handle(
          _invitingOthersToParticipateMeta,
          invitingOthersToParticipate.isAcceptableOrUnknown(
              data['inviting_others_to_participate']!,
              _invitingOthersToParticipateMeta));
    }
    if (data.containsKey('number_of_people_invited')) {
      context.handle(
          _numberOfPeopleInvitedMeta,
          numberOfPeopleInvited.isAcceptableOrUnknown(
              data['number_of_people_invited']!, _numberOfPeopleInvitedMeta));
    }
    if (data.containsKey('created')) {
      context.handle(_createdMeta,
          created.isAcceptableOrUnknown(data['created']!, _createdMeta));
    }
    if (data.containsKey('position_id')) {
      context.handle(
          _positionIdMeta,
          positionId.isAcceptableOrUnknown(
              data['position_id']!, _positionIdMeta));
    }
    if (data.containsKey('sync_status')) {
      context.handle(
          _syncStatusMeta,
          syncStatus.isAcceptableOrUnknown(
              data['sync_status']!, _syncStatusMeta));
    }
    if (data.containsKey('sync_error')) {
      context.handle(_syncErrorMeta,
          syncError.isAcceptableOrUnknown(data['sync_error']!, _syncErrorMeta));
    }
    if (data.containsKey('membership_id')) {
      context.handle(
          _membershipIdMeta,
          membershipId.isAcceptableOrUnknown(
              data['membership_id']!, _membershipIdMeta));
    }
    if (data.containsKey('position_label')) {
      context.handle(
          _positionLabelMeta,
          positionLabel.isAcceptableOrUnknown(
              data['position_label']!, _positionLabelMeta));
    }
    if (data.containsKey('synced_at')) {
      context.handle(_syncedAtMeta,
          syncedAt.isAcceptableOrUnknown(data['synced_at']!, _syncedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  FollowerRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return FollowerRow(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      uuid: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}uuid'])!,
      deviceId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}device_id'])!,
      latitude: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}latitude'])!,
      longitude: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}longitude'])!,
      sectionNumber: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}section_number'])!,
      street: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}street'])!,
      externalNumber: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}external_number'])!,
      internalNumber: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}internal_number']),
      zipCode: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}zip_code']),
      neighborhood: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}neighborhood']),
      sectionNumberIne: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}section_number_ine']),
      residenceEntityId: attachedDatabase.typeMapping.read(
          DriftSqlType.int, data['${effectivePrefix}residence_entity_id']),
      residenceMunicipalityId: attachedDatabase.typeMapping.read(
          DriftSqlType.int,
          data['${effectivePrefix}residence_municipality_id']),
      isAdult: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_adult']),
      names: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}names']),
      lastName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}last_name']),
      surName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sur_name']),
      curp: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}curp']),
      ine: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}ine']),
      validity: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}validity']),
      birthdate: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}birthdate']),
      cellPhoneNumber: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}cell_phone_number']),
      supportsWhatsapp: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}supports_whatsapp']),
      cellPhoneNumberOther: attachedDatabase.typeMapping.read(
          DriftSqlType.string,
          data['${effectivePrefix}cell_phone_number_other']),
      email: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}email']),
      catSexId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}cat_sex_id']),
      catSexNombre: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}cat_sex_nombre']),
      birthEntityId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}birth_entity_id']),
      isInSync: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_in_sync'])!,
      supportsMovement: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}supports_movement'])!,
      committeeParticipation: attachedDatabase.typeMapping.read(
          DriftSqlType.string,
          data['${effectivePrefix}committee_participation']),
      pollinStationRepresentativeParticipation: attachedDatabase.typeMapping.read(
          DriftSqlType.string,
          data[
              '${effectivePrefix}pollin_station_representative_participation']),
      invitingOthersToParticipate: attachedDatabase.typeMapping.read(
          DriftSqlType.string,
          data['${effectivePrefix}inviting_others_to_participate']),
      numberOfPeopleInvited: attachedDatabase.typeMapping.read(
          DriftSqlType.string,
          data['${effectivePrefix}number_of_people_invited']),
      created: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created']),
      positionId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}position_id']),
      syncStatus: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sync_status'])!,
      syncError: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sync_error']),
      membershipId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}membership_id']),
      positionLabel: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}position_label']),
      syncedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}synced_at']),
    );
  }

  @override
  $FollowersTable createAlias(String alias) {
    return $FollowersTable(attachedDatabase, alias);
  }
}

class FollowerRow extends DataClass implements Insertable<FollowerRow> {
  final int id;
  final String uuid;
  final String deviceId;
  final String latitude;
  final String longitude;
  final String sectionNumber;
  final String street;
  final String externalNumber;
  final String? internalNumber;
  final String? zipCode;
  final String? neighborhood;
  final String? sectionNumberIne;
  final int? residenceEntityId;
  final int? residenceMunicipalityId;
  final bool? isAdult;
  final String? names;
  final String? lastName;
  final String? surName;
  final String? curp;
  final String? ine;
  final String? validity;
  final String? birthdate;
  final String? cellPhoneNumber;
  final String? supportsWhatsapp;
  final String? cellPhoneNumberOther;
  final String? email;
  final int? catSexId;
  final String? catSexNombre;
  final int? birthEntityId;
  final bool isInSync;
  final String supportsMovement;
  final String? committeeParticipation;
  final String? pollinStationRepresentativeParticipation;
  final String? invitingOthersToParticipate;
  final String? numberOfPeopleInvited;
  final DateTime? created;

  /// El puesto donde queda (capture-targets); null = el de quien captura.
  final int? positionId;

  /// PENDING, SYNCED, DUPLICATE (ya era convencido) o REJECTED (el servidor
  /// no lo aceptó: hay que corregirlo o borrarlo).
  final String syncStatus;

  /// El mensaje del servidor cuando no se aceptó o ya existía.
  final String? syncError;
  final int? membershipId;

  /// Dónde quedó: "S-0233A1-11".
  final String? positionLabel;
  final DateTime? syncedAt;
  const FollowerRow(
      {required this.id,
      required this.uuid,
      required this.deviceId,
      required this.latitude,
      required this.longitude,
      required this.sectionNumber,
      required this.street,
      required this.externalNumber,
      this.internalNumber,
      this.zipCode,
      this.neighborhood,
      this.sectionNumberIne,
      this.residenceEntityId,
      this.residenceMunicipalityId,
      this.isAdult,
      this.names,
      this.lastName,
      this.surName,
      this.curp,
      this.ine,
      this.validity,
      this.birthdate,
      this.cellPhoneNumber,
      this.supportsWhatsapp,
      this.cellPhoneNumberOther,
      this.email,
      this.catSexId,
      this.catSexNombre,
      this.birthEntityId,
      required this.isInSync,
      required this.supportsMovement,
      this.committeeParticipation,
      this.pollinStationRepresentativeParticipation,
      this.invitingOthersToParticipate,
      this.numberOfPeopleInvited,
      this.created,
      this.positionId,
      required this.syncStatus,
      this.syncError,
      this.membershipId,
      this.positionLabel,
      this.syncedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['uuid'] = Variable<String>(uuid);
    map['device_id'] = Variable<String>(deviceId);
    map['latitude'] = Variable<String>(latitude);
    map['longitude'] = Variable<String>(longitude);
    map['section_number'] = Variable<String>(sectionNumber);
    map['street'] = Variable<String>(street);
    map['external_number'] = Variable<String>(externalNumber);
    if (!nullToAbsent || internalNumber != null) {
      map['internal_number'] = Variable<String>(internalNumber);
    }
    if (!nullToAbsent || zipCode != null) {
      map['zip_code'] = Variable<String>(zipCode);
    }
    if (!nullToAbsent || neighborhood != null) {
      map['neighborhood'] = Variable<String>(neighborhood);
    }
    if (!nullToAbsent || sectionNumberIne != null) {
      map['section_number_ine'] = Variable<String>(sectionNumberIne);
    }
    if (!nullToAbsent || residenceEntityId != null) {
      map['residence_entity_id'] = Variable<int>(residenceEntityId);
    }
    if (!nullToAbsent || residenceMunicipalityId != null) {
      map['residence_municipality_id'] = Variable<int>(residenceMunicipalityId);
    }
    if (!nullToAbsent || isAdult != null) {
      map['is_adult'] = Variable<bool>(isAdult);
    }
    if (!nullToAbsent || names != null) {
      map['names'] = Variable<String>(names);
    }
    if (!nullToAbsent || lastName != null) {
      map['last_name'] = Variable<String>(lastName);
    }
    if (!nullToAbsent || surName != null) {
      map['sur_name'] = Variable<String>(surName);
    }
    if (!nullToAbsent || curp != null) {
      map['curp'] = Variable<String>(curp);
    }
    if (!nullToAbsent || ine != null) {
      map['ine'] = Variable<String>(ine);
    }
    if (!nullToAbsent || validity != null) {
      map['validity'] = Variable<String>(validity);
    }
    if (!nullToAbsent || birthdate != null) {
      map['birthdate'] = Variable<String>(birthdate);
    }
    if (!nullToAbsent || cellPhoneNumber != null) {
      map['cell_phone_number'] = Variable<String>(cellPhoneNumber);
    }
    if (!nullToAbsent || supportsWhatsapp != null) {
      map['supports_whatsapp'] = Variable<String>(supportsWhatsapp);
    }
    if (!nullToAbsent || cellPhoneNumberOther != null) {
      map['cell_phone_number_other'] = Variable<String>(cellPhoneNumberOther);
    }
    if (!nullToAbsent || email != null) {
      map['email'] = Variable<String>(email);
    }
    if (!nullToAbsent || catSexId != null) {
      map['cat_sex_id'] = Variable<int>(catSexId);
    }
    if (!nullToAbsent || catSexNombre != null) {
      map['cat_sex_nombre'] = Variable<String>(catSexNombre);
    }
    if (!nullToAbsent || birthEntityId != null) {
      map['birth_entity_id'] = Variable<int>(birthEntityId);
    }
    map['is_in_sync'] = Variable<bool>(isInSync);
    map['supports_movement'] = Variable<String>(supportsMovement);
    if (!nullToAbsent || committeeParticipation != null) {
      map['committee_participation'] = Variable<String>(committeeParticipation);
    }
    if (!nullToAbsent || pollinStationRepresentativeParticipation != null) {
      map['pollin_station_representative_participation'] =
          Variable<String>(pollinStationRepresentativeParticipation);
    }
    if (!nullToAbsent || invitingOthersToParticipate != null) {
      map['inviting_others_to_participate'] =
          Variable<String>(invitingOthersToParticipate);
    }
    if (!nullToAbsent || numberOfPeopleInvited != null) {
      map['number_of_people_invited'] = Variable<String>(numberOfPeopleInvited);
    }
    if (!nullToAbsent || created != null) {
      map['created'] = Variable<DateTime>(created);
    }
    if (!nullToAbsent || positionId != null) {
      map['position_id'] = Variable<int>(positionId);
    }
    map['sync_status'] = Variable<String>(syncStatus);
    if (!nullToAbsent || syncError != null) {
      map['sync_error'] = Variable<String>(syncError);
    }
    if (!nullToAbsent || membershipId != null) {
      map['membership_id'] = Variable<int>(membershipId);
    }
    if (!nullToAbsent || positionLabel != null) {
      map['position_label'] = Variable<String>(positionLabel);
    }
    if (!nullToAbsent || syncedAt != null) {
      map['synced_at'] = Variable<DateTime>(syncedAt);
    }
    return map;
  }

  FollowersCompanion toCompanion(bool nullToAbsent) {
    return FollowersCompanion(
      id: Value(id),
      uuid: Value(uuid),
      deviceId: Value(deviceId),
      latitude: Value(latitude),
      longitude: Value(longitude),
      sectionNumber: Value(sectionNumber),
      street: Value(street),
      externalNumber: Value(externalNumber),
      internalNumber: internalNumber == null && nullToAbsent
          ? const Value.absent()
          : Value(internalNumber),
      zipCode: zipCode == null && nullToAbsent
          ? const Value.absent()
          : Value(zipCode),
      neighborhood: neighborhood == null && nullToAbsent
          ? const Value.absent()
          : Value(neighborhood),
      sectionNumberIne: sectionNumberIne == null && nullToAbsent
          ? const Value.absent()
          : Value(sectionNumberIne),
      residenceEntityId: residenceEntityId == null && nullToAbsent
          ? const Value.absent()
          : Value(residenceEntityId),
      residenceMunicipalityId: residenceMunicipalityId == null && nullToAbsent
          ? const Value.absent()
          : Value(residenceMunicipalityId),
      isAdult: isAdult == null && nullToAbsent
          ? const Value.absent()
          : Value(isAdult),
      names:
          names == null && nullToAbsent ? const Value.absent() : Value(names),
      lastName: lastName == null && nullToAbsent
          ? const Value.absent()
          : Value(lastName),
      surName: surName == null && nullToAbsent
          ? const Value.absent()
          : Value(surName),
      curp: curp == null && nullToAbsent ? const Value.absent() : Value(curp),
      ine: ine == null && nullToAbsent ? const Value.absent() : Value(ine),
      validity: validity == null && nullToAbsent
          ? const Value.absent()
          : Value(validity),
      birthdate: birthdate == null && nullToAbsent
          ? const Value.absent()
          : Value(birthdate),
      cellPhoneNumber: cellPhoneNumber == null && nullToAbsent
          ? const Value.absent()
          : Value(cellPhoneNumber),
      supportsWhatsapp: supportsWhatsapp == null && nullToAbsent
          ? const Value.absent()
          : Value(supportsWhatsapp),
      cellPhoneNumberOther: cellPhoneNumberOther == null && nullToAbsent
          ? const Value.absent()
          : Value(cellPhoneNumberOther),
      email:
          email == null && nullToAbsent ? const Value.absent() : Value(email),
      catSexId: catSexId == null && nullToAbsent
          ? const Value.absent()
          : Value(catSexId),
      catSexNombre: catSexNombre == null && nullToAbsent
          ? const Value.absent()
          : Value(catSexNombre),
      birthEntityId: birthEntityId == null && nullToAbsent
          ? const Value.absent()
          : Value(birthEntityId),
      isInSync: Value(isInSync),
      supportsMovement: Value(supportsMovement),
      committeeParticipation: committeeParticipation == null && nullToAbsent
          ? const Value.absent()
          : Value(committeeParticipation),
      pollinStationRepresentativeParticipation:
          pollinStationRepresentativeParticipation == null && nullToAbsent
              ? const Value.absent()
              : Value(pollinStationRepresentativeParticipation),
      invitingOthersToParticipate:
          invitingOthersToParticipate == null && nullToAbsent
              ? const Value.absent()
              : Value(invitingOthersToParticipate),
      numberOfPeopleInvited: numberOfPeopleInvited == null && nullToAbsent
          ? const Value.absent()
          : Value(numberOfPeopleInvited),
      created: created == null && nullToAbsent
          ? const Value.absent()
          : Value(created),
      positionId: positionId == null && nullToAbsent
          ? const Value.absent()
          : Value(positionId),
      syncStatus: Value(syncStatus),
      syncError: syncError == null && nullToAbsent
          ? const Value.absent()
          : Value(syncError),
      membershipId: membershipId == null && nullToAbsent
          ? const Value.absent()
          : Value(membershipId),
      positionLabel: positionLabel == null && nullToAbsent
          ? const Value.absent()
          : Value(positionLabel),
      syncedAt: syncedAt == null && nullToAbsent
          ? const Value.absent()
          : Value(syncedAt),
    );
  }

  factory FollowerRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return FollowerRow(
      id: serializer.fromJson<int>(json['id']),
      uuid: serializer.fromJson<String>(json['uuid']),
      deviceId: serializer.fromJson<String>(json['deviceId']),
      latitude: serializer.fromJson<String>(json['latitude']),
      longitude: serializer.fromJson<String>(json['longitude']),
      sectionNumber: serializer.fromJson<String>(json['sectionNumber']),
      street: serializer.fromJson<String>(json['street']),
      externalNumber: serializer.fromJson<String>(json['externalNumber']),
      internalNumber: serializer.fromJson<String?>(json['internalNumber']),
      zipCode: serializer.fromJson<String?>(json['zipCode']),
      neighborhood: serializer.fromJson<String?>(json['neighborhood']),
      sectionNumberIne: serializer.fromJson<String?>(json['sectionNumberIne']),
      residenceEntityId: serializer.fromJson<int?>(json['residenceEntityId']),
      residenceMunicipalityId:
          serializer.fromJson<int?>(json['residenceMunicipalityId']),
      isAdult: serializer.fromJson<bool?>(json['isAdult']),
      names: serializer.fromJson<String?>(json['names']),
      lastName: serializer.fromJson<String?>(json['lastName']),
      surName: serializer.fromJson<String?>(json['surName']),
      curp: serializer.fromJson<String?>(json['curp']),
      ine: serializer.fromJson<String?>(json['ine']),
      validity: serializer.fromJson<String?>(json['validity']),
      birthdate: serializer.fromJson<String?>(json['birthdate']),
      cellPhoneNumber: serializer.fromJson<String?>(json['cellPhoneNumber']),
      supportsWhatsapp: serializer.fromJson<String?>(json['supportsWhatsapp']),
      cellPhoneNumberOther:
          serializer.fromJson<String?>(json['cellPhoneNumberOther']),
      email: serializer.fromJson<String?>(json['email']),
      catSexId: serializer.fromJson<int?>(json['catSexId']),
      catSexNombre: serializer.fromJson<String?>(json['catSexNombre']),
      birthEntityId: serializer.fromJson<int?>(json['birthEntityId']),
      isInSync: serializer.fromJson<bool>(json['isInSync']),
      supportsMovement: serializer.fromJson<String>(json['supportsMovement']),
      committeeParticipation:
          serializer.fromJson<String?>(json['committeeParticipation']),
      pollinStationRepresentativeParticipation: serializer
          .fromJson<String?>(json['pollinStationRepresentativeParticipation']),
      invitingOthersToParticipate:
          serializer.fromJson<String?>(json['invitingOthersToParticipate']),
      numberOfPeopleInvited:
          serializer.fromJson<String?>(json['numberOfPeopleInvited']),
      created: serializer.fromJson<DateTime?>(json['created']),
      positionId: serializer.fromJson<int?>(json['positionId']),
      syncStatus: serializer.fromJson<String>(json['syncStatus']),
      syncError: serializer.fromJson<String?>(json['syncError']),
      membershipId: serializer.fromJson<int?>(json['membershipId']),
      positionLabel: serializer.fromJson<String?>(json['positionLabel']),
      syncedAt: serializer.fromJson<DateTime?>(json['syncedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'uuid': serializer.toJson<String>(uuid),
      'deviceId': serializer.toJson<String>(deviceId),
      'latitude': serializer.toJson<String>(latitude),
      'longitude': serializer.toJson<String>(longitude),
      'sectionNumber': serializer.toJson<String>(sectionNumber),
      'street': serializer.toJson<String>(street),
      'externalNumber': serializer.toJson<String>(externalNumber),
      'internalNumber': serializer.toJson<String?>(internalNumber),
      'zipCode': serializer.toJson<String?>(zipCode),
      'neighborhood': serializer.toJson<String?>(neighborhood),
      'sectionNumberIne': serializer.toJson<String?>(sectionNumberIne),
      'residenceEntityId': serializer.toJson<int?>(residenceEntityId),
      'residenceMunicipalityId':
          serializer.toJson<int?>(residenceMunicipalityId),
      'isAdult': serializer.toJson<bool?>(isAdult),
      'names': serializer.toJson<String?>(names),
      'lastName': serializer.toJson<String?>(lastName),
      'surName': serializer.toJson<String?>(surName),
      'curp': serializer.toJson<String?>(curp),
      'ine': serializer.toJson<String?>(ine),
      'validity': serializer.toJson<String?>(validity),
      'birthdate': serializer.toJson<String?>(birthdate),
      'cellPhoneNumber': serializer.toJson<String?>(cellPhoneNumber),
      'supportsWhatsapp': serializer.toJson<String?>(supportsWhatsapp),
      'cellPhoneNumberOther': serializer.toJson<String?>(cellPhoneNumberOther),
      'email': serializer.toJson<String?>(email),
      'catSexId': serializer.toJson<int?>(catSexId),
      'catSexNombre': serializer.toJson<String?>(catSexNombre),
      'birthEntityId': serializer.toJson<int?>(birthEntityId),
      'isInSync': serializer.toJson<bool>(isInSync),
      'supportsMovement': serializer.toJson<String>(supportsMovement),
      'committeeParticipation':
          serializer.toJson<String?>(committeeParticipation),
      'pollinStationRepresentativeParticipation':
          serializer.toJson<String?>(pollinStationRepresentativeParticipation),
      'invitingOthersToParticipate':
          serializer.toJson<String?>(invitingOthersToParticipate),
      'numberOfPeopleInvited':
          serializer.toJson<String?>(numberOfPeopleInvited),
      'created': serializer.toJson<DateTime?>(created),
      'positionId': serializer.toJson<int?>(positionId),
      'syncStatus': serializer.toJson<String>(syncStatus),
      'syncError': serializer.toJson<String?>(syncError),
      'membershipId': serializer.toJson<int?>(membershipId),
      'positionLabel': serializer.toJson<String?>(positionLabel),
      'syncedAt': serializer.toJson<DateTime?>(syncedAt),
    };
  }

  FollowerRow copyWith(
          {int? id,
          String? uuid,
          String? deviceId,
          String? latitude,
          String? longitude,
          String? sectionNumber,
          String? street,
          String? externalNumber,
          Value<String?> internalNumber = const Value.absent(),
          Value<String?> zipCode = const Value.absent(),
          Value<String?> neighborhood = const Value.absent(),
          Value<String?> sectionNumberIne = const Value.absent(),
          Value<int?> residenceEntityId = const Value.absent(),
          Value<int?> residenceMunicipalityId = const Value.absent(),
          Value<bool?> isAdult = const Value.absent(),
          Value<String?> names = const Value.absent(),
          Value<String?> lastName = const Value.absent(),
          Value<String?> surName = const Value.absent(),
          Value<String?> curp = const Value.absent(),
          Value<String?> ine = const Value.absent(),
          Value<String?> validity = const Value.absent(),
          Value<String?> birthdate = const Value.absent(),
          Value<String?> cellPhoneNumber = const Value.absent(),
          Value<String?> supportsWhatsapp = const Value.absent(),
          Value<String?> cellPhoneNumberOther = const Value.absent(),
          Value<String?> email = const Value.absent(),
          Value<int?> catSexId = const Value.absent(),
          Value<String?> catSexNombre = const Value.absent(),
          Value<int?> birthEntityId = const Value.absent(),
          bool? isInSync,
          String? supportsMovement,
          Value<String?> committeeParticipation = const Value.absent(),
          Value<String?> pollinStationRepresentativeParticipation =
              const Value.absent(),
          Value<String?> invitingOthersToParticipate = const Value.absent(),
          Value<String?> numberOfPeopleInvited = const Value.absent(),
          Value<DateTime?> created = const Value.absent(),
          Value<int?> positionId = const Value.absent(),
          String? syncStatus,
          Value<String?> syncError = const Value.absent(),
          Value<int?> membershipId = const Value.absent(),
          Value<String?> positionLabel = const Value.absent(),
          Value<DateTime?> syncedAt = const Value.absent()}) =>
      FollowerRow(
        id: id ?? this.id,
        uuid: uuid ?? this.uuid,
        deviceId: deviceId ?? this.deviceId,
        latitude: latitude ?? this.latitude,
        longitude: longitude ?? this.longitude,
        sectionNumber: sectionNumber ?? this.sectionNumber,
        street: street ?? this.street,
        externalNumber: externalNumber ?? this.externalNumber,
        internalNumber:
            internalNumber.present ? internalNumber.value : this.internalNumber,
        zipCode: zipCode.present ? zipCode.value : this.zipCode,
        neighborhood:
            neighborhood.present ? neighborhood.value : this.neighborhood,
        sectionNumberIne: sectionNumberIne.present
            ? sectionNumberIne.value
            : this.sectionNumberIne,
        residenceEntityId: residenceEntityId.present
            ? residenceEntityId.value
            : this.residenceEntityId,
        residenceMunicipalityId: residenceMunicipalityId.present
            ? residenceMunicipalityId.value
            : this.residenceMunicipalityId,
        isAdult: isAdult.present ? isAdult.value : this.isAdult,
        names: names.present ? names.value : this.names,
        lastName: lastName.present ? lastName.value : this.lastName,
        surName: surName.present ? surName.value : this.surName,
        curp: curp.present ? curp.value : this.curp,
        ine: ine.present ? ine.value : this.ine,
        validity: validity.present ? validity.value : this.validity,
        birthdate: birthdate.present ? birthdate.value : this.birthdate,
        cellPhoneNumber: cellPhoneNumber.present
            ? cellPhoneNumber.value
            : this.cellPhoneNumber,
        supportsWhatsapp: supportsWhatsapp.present
            ? supportsWhatsapp.value
            : this.supportsWhatsapp,
        cellPhoneNumberOther: cellPhoneNumberOther.present
            ? cellPhoneNumberOther.value
            : this.cellPhoneNumberOther,
        email: email.present ? email.value : this.email,
        catSexId: catSexId.present ? catSexId.value : this.catSexId,
        catSexNombre:
            catSexNombre.present ? catSexNombre.value : this.catSexNombre,
        birthEntityId:
            birthEntityId.present ? birthEntityId.value : this.birthEntityId,
        isInSync: isInSync ?? this.isInSync,
        supportsMovement: supportsMovement ?? this.supportsMovement,
        committeeParticipation: committeeParticipation.present
            ? committeeParticipation.value
            : this.committeeParticipation,
        pollinStationRepresentativeParticipation:
            pollinStationRepresentativeParticipation.present
                ? pollinStationRepresentativeParticipation.value
                : this.pollinStationRepresentativeParticipation,
        invitingOthersToParticipate: invitingOthersToParticipate.present
            ? invitingOthersToParticipate.value
            : this.invitingOthersToParticipate,
        numberOfPeopleInvited: numberOfPeopleInvited.present
            ? numberOfPeopleInvited.value
            : this.numberOfPeopleInvited,
        created: created.present ? created.value : this.created,
        positionId: positionId.present ? positionId.value : this.positionId,
        syncStatus: syncStatus ?? this.syncStatus,
        syncError: syncError.present ? syncError.value : this.syncError,
        membershipId:
            membershipId.present ? membershipId.value : this.membershipId,
        positionLabel:
            positionLabel.present ? positionLabel.value : this.positionLabel,
        syncedAt: syncedAt.present ? syncedAt.value : this.syncedAt,
      );
  FollowerRow copyWithCompanion(FollowersCompanion data) {
    return FollowerRow(
      id: data.id.present ? data.id.value : this.id,
      uuid: data.uuid.present ? data.uuid.value : this.uuid,
      deviceId: data.deviceId.present ? data.deviceId.value : this.deviceId,
      latitude: data.latitude.present ? data.latitude.value : this.latitude,
      longitude: data.longitude.present ? data.longitude.value : this.longitude,
      sectionNumber: data.sectionNumber.present
          ? data.sectionNumber.value
          : this.sectionNumber,
      street: data.street.present ? data.street.value : this.street,
      externalNumber: data.externalNumber.present
          ? data.externalNumber.value
          : this.externalNumber,
      internalNumber: data.internalNumber.present
          ? data.internalNumber.value
          : this.internalNumber,
      zipCode: data.zipCode.present ? data.zipCode.value : this.zipCode,
      neighborhood: data.neighborhood.present
          ? data.neighborhood.value
          : this.neighborhood,
      sectionNumberIne: data.sectionNumberIne.present
          ? data.sectionNumberIne.value
          : this.sectionNumberIne,
      residenceEntityId: data.residenceEntityId.present
          ? data.residenceEntityId.value
          : this.residenceEntityId,
      residenceMunicipalityId: data.residenceMunicipalityId.present
          ? data.residenceMunicipalityId.value
          : this.residenceMunicipalityId,
      isAdult: data.isAdult.present ? data.isAdult.value : this.isAdult,
      names: data.names.present ? data.names.value : this.names,
      lastName: data.lastName.present ? data.lastName.value : this.lastName,
      surName: data.surName.present ? data.surName.value : this.surName,
      curp: data.curp.present ? data.curp.value : this.curp,
      ine: data.ine.present ? data.ine.value : this.ine,
      validity: data.validity.present ? data.validity.value : this.validity,
      birthdate: data.birthdate.present ? data.birthdate.value : this.birthdate,
      cellPhoneNumber: data.cellPhoneNumber.present
          ? data.cellPhoneNumber.value
          : this.cellPhoneNumber,
      supportsWhatsapp: data.supportsWhatsapp.present
          ? data.supportsWhatsapp.value
          : this.supportsWhatsapp,
      cellPhoneNumberOther: data.cellPhoneNumberOther.present
          ? data.cellPhoneNumberOther.value
          : this.cellPhoneNumberOther,
      email: data.email.present ? data.email.value : this.email,
      catSexId: data.catSexId.present ? data.catSexId.value : this.catSexId,
      catSexNombre: data.catSexNombre.present
          ? data.catSexNombre.value
          : this.catSexNombre,
      birthEntityId: data.birthEntityId.present
          ? data.birthEntityId.value
          : this.birthEntityId,
      isInSync: data.isInSync.present ? data.isInSync.value : this.isInSync,
      supportsMovement: data.supportsMovement.present
          ? data.supportsMovement.value
          : this.supportsMovement,
      committeeParticipation: data.committeeParticipation.present
          ? data.committeeParticipation.value
          : this.committeeParticipation,
      pollinStationRepresentativeParticipation:
          data.pollinStationRepresentativeParticipation.present
              ? data.pollinStationRepresentativeParticipation.value
              : this.pollinStationRepresentativeParticipation,
      invitingOthersToParticipate: data.invitingOthersToParticipate.present
          ? data.invitingOthersToParticipate.value
          : this.invitingOthersToParticipate,
      numberOfPeopleInvited: data.numberOfPeopleInvited.present
          ? data.numberOfPeopleInvited.value
          : this.numberOfPeopleInvited,
      created: data.created.present ? data.created.value : this.created,
      positionId:
          data.positionId.present ? data.positionId.value : this.positionId,
      syncStatus:
          data.syncStatus.present ? data.syncStatus.value : this.syncStatus,
      syncError: data.syncError.present ? data.syncError.value : this.syncError,
      membershipId: data.membershipId.present
          ? data.membershipId.value
          : this.membershipId,
      positionLabel: data.positionLabel.present
          ? data.positionLabel.value
          : this.positionLabel,
      syncedAt: data.syncedAt.present ? data.syncedAt.value : this.syncedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('FollowerRow(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('deviceId: $deviceId, ')
          ..write('latitude: $latitude, ')
          ..write('longitude: $longitude, ')
          ..write('sectionNumber: $sectionNumber, ')
          ..write('street: $street, ')
          ..write('externalNumber: $externalNumber, ')
          ..write('internalNumber: $internalNumber, ')
          ..write('zipCode: $zipCode, ')
          ..write('neighborhood: $neighborhood, ')
          ..write('sectionNumberIne: $sectionNumberIne, ')
          ..write('residenceEntityId: $residenceEntityId, ')
          ..write('residenceMunicipalityId: $residenceMunicipalityId, ')
          ..write('isAdult: $isAdult, ')
          ..write('names: $names, ')
          ..write('lastName: $lastName, ')
          ..write('surName: $surName, ')
          ..write('curp: $curp, ')
          ..write('ine: $ine, ')
          ..write('validity: $validity, ')
          ..write('birthdate: $birthdate, ')
          ..write('cellPhoneNumber: $cellPhoneNumber, ')
          ..write('supportsWhatsapp: $supportsWhatsapp, ')
          ..write('cellPhoneNumberOther: $cellPhoneNumberOther, ')
          ..write('email: $email, ')
          ..write('catSexId: $catSexId, ')
          ..write('catSexNombre: $catSexNombre, ')
          ..write('birthEntityId: $birthEntityId, ')
          ..write('isInSync: $isInSync, ')
          ..write('supportsMovement: $supportsMovement, ')
          ..write('committeeParticipation: $committeeParticipation, ')
          ..write(
              'pollinStationRepresentativeParticipation: $pollinStationRepresentativeParticipation, ')
          ..write('invitingOthersToParticipate: $invitingOthersToParticipate, ')
          ..write('numberOfPeopleInvited: $numberOfPeopleInvited, ')
          ..write('created: $created, ')
          ..write('positionId: $positionId, ')
          ..write('syncStatus: $syncStatus, ')
          ..write('syncError: $syncError, ')
          ..write('membershipId: $membershipId, ')
          ..write('positionLabel: $positionLabel, ')
          ..write('syncedAt: $syncedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hashAll([
        id,
        uuid,
        deviceId,
        latitude,
        longitude,
        sectionNumber,
        street,
        externalNumber,
        internalNumber,
        zipCode,
        neighborhood,
        sectionNumberIne,
        residenceEntityId,
        residenceMunicipalityId,
        isAdult,
        names,
        lastName,
        surName,
        curp,
        ine,
        validity,
        birthdate,
        cellPhoneNumber,
        supportsWhatsapp,
        cellPhoneNumberOther,
        email,
        catSexId,
        catSexNombre,
        birthEntityId,
        isInSync,
        supportsMovement,
        committeeParticipation,
        pollinStationRepresentativeParticipation,
        invitingOthersToParticipate,
        numberOfPeopleInvited,
        created,
        positionId,
        syncStatus,
        syncError,
        membershipId,
        positionLabel,
        syncedAt
      ]);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is FollowerRow &&
          other.id == this.id &&
          other.uuid == this.uuid &&
          other.deviceId == this.deviceId &&
          other.latitude == this.latitude &&
          other.longitude == this.longitude &&
          other.sectionNumber == this.sectionNumber &&
          other.street == this.street &&
          other.externalNumber == this.externalNumber &&
          other.internalNumber == this.internalNumber &&
          other.zipCode == this.zipCode &&
          other.neighborhood == this.neighborhood &&
          other.sectionNumberIne == this.sectionNumberIne &&
          other.residenceEntityId == this.residenceEntityId &&
          other.residenceMunicipalityId == this.residenceMunicipalityId &&
          other.isAdult == this.isAdult &&
          other.names == this.names &&
          other.lastName == this.lastName &&
          other.surName == this.surName &&
          other.curp == this.curp &&
          other.ine == this.ine &&
          other.validity == this.validity &&
          other.birthdate == this.birthdate &&
          other.cellPhoneNumber == this.cellPhoneNumber &&
          other.supportsWhatsapp == this.supportsWhatsapp &&
          other.cellPhoneNumberOther == this.cellPhoneNumberOther &&
          other.email == this.email &&
          other.catSexId == this.catSexId &&
          other.catSexNombre == this.catSexNombre &&
          other.birthEntityId == this.birthEntityId &&
          other.isInSync == this.isInSync &&
          other.supportsMovement == this.supportsMovement &&
          other.committeeParticipation == this.committeeParticipation &&
          other.pollinStationRepresentativeParticipation ==
              this.pollinStationRepresentativeParticipation &&
          other.invitingOthersToParticipate ==
              this.invitingOthersToParticipate &&
          other.numberOfPeopleInvited == this.numberOfPeopleInvited &&
          other.created == this.created &&
          other.positionId == this.positionId &&
          other.syncStatus == this.syncStatus &&
          other.syncError == this.syncError &&
          other.membershipId == this.membershipId &&
          other.positionLabel == this.positionLabel &&
          other.syncedAt == this.syncedAt);
}

class FollowersCompanion extends UpdateCompanion<FollowerRow> {
  final Value<int> id;
  final Value<String> uuid;
  final Value<String> deviceId;
  final Value<String> latitude;
  final Value<String> longitude;
  final Value<String> sectionNumber;
  final Value<String> street;
  final Value<String> externalNumber;
  final Value<String?> internalNumber;
  final Value<String?> zipCode;
  final Value<String?> neighborhood;
  final Value<String?> sectionNumberIne;
  final Value<int?> residenceEntityId;
  final Value<int?> residenceMunicipalityId;
  final Value<bool?> isAdult;
  final Value<String?> names;
  final Value<String?> lastName;
  final Value<String?> surName;
  final Value<String?> curp;
  final Value<String?> ine;
  final Value<String?> validity;
  final Value<String?> birthdate;
  final Value<String?> cellPhoneNumber;
  final Value<String?> supportsWhatsapp;
  final Value<String?> cellPhoneNumberOther;
  final Value<String?> email;
  final Value<int?> catSexId;
  final Value<String?> catSexNombre;
  final Value<int?> birthEntityId;
  final Value<bool> isInSync;
  final Value<String> supportsMovement;
  final Value<String?> committeeParticipation;
  final Value<String?> pollinStationRepresentativeParticipation;
  final Value<String?> invitingOthersToParticipate;
  final Value<String?> numberOfPeopleInvited;
  final Value<DateTime?> created;
  final Value<int?> positionId;
  final Value<String> syncStatus;
  final Value<String?> syncError;
  final Value<int?> membershipId;
  final Value<String?> positionLabel;
  final Value<DateTime?> syncedAt;
  const FollowersCompanion({
    this.id = const Value.absent(),
    this.uuid = const Value.absent(),
    this.deviceId = const Value.absent(),
    this.latitude = const Value.absent(),
    this.longitude = const Value.absent(),
    this.sectionNumber = const Value.absent(),
    this.street = const Value.absent(),
    this.externalNumber = const Value.absent(),
    this.internalNumber = const Value.absent(),
    this.zipCode = const Value.absent(),
    this.neighborhood = const Value.absent(),
    this.sectionNumberIne = const Value.absent(),
    this.residenceEntityId = const Value.absent(),
    this.residenceMunicipalityId = const Value.absent(),
    this.isAdult = const Value.absent(),
    this.names = const Value.absent(),
    this.lastName = const Value.absent(),
    this.surName = const Value.absent(),
    this.curp = const Value.absent(),
    this.ine = const Value.absent(),
    this.validity = const Value.absent(),
    this.birthdate = const Value.absent(),
    this.cellPhoneNumber = const Value.absent(),
    this.supportsWhatsapp = const Value.absent(),
    this.cellPhoneNumberOther = const Value.absent(),
    this.email = const Value.absent(),
    this.catSexId = const Value.absent(),
    this.catSexNombre = const Value.absent(),
    this.birthEntityId = const Value.absent(),
    this.isInSync = const Value.absent(),
    this.supportsMovement = const Value.absent(),
    this.committeeParticipation = const Value.absent(),
    this.pollinStationRepresentativeParticipation = const Value.absent(),
    this.invitingOthersToParticipate = const Value.absent(),
    this.numberOfPeopleInvited = const Value.absent(),
    this.created = const Value.absent(),
    this.positionId = const Value.absent(),
    this.syncStatus = const Value.absent(),
    this.syncError = const Value.absent(),
    this.membershipId = const Value.absent(),
    this.positionLabel = const Value.absent(),
    this.syncedAt = const Value.absent(),
  });
  FollowersCompanion.insert({
    this.id = const Value.absent(),
    required String uuid,
    required String deviceId,
    required String latitude,
    required String longitude,
    required String sectionNumber,
    required String street,
    required String externalNumber,
    this.internalNumber = const Value.absent(),
    this.zipCode = const Value.absent(),
    this.neighborhood = const Value.absent(),
    this.sectionNumberIne = const Value.absent(),
    this.residenceEntityId = const Value.absent(),
    this.residenceMunicipalityId = const Value.absent(),
    this.isAdult = const Value.absent(),
    this.names = const Value.absent(),
    this.lastName = const Value.absent(),
    this.surName = const Value.absent(),
    this.curp = const Value.absent(),
    this.ine = const Value.absent(),
    this.validity = const Value.absent(),
    this.birthdate = const Value.absent(),
    this.cellPhoneNumber = const Value.absent(),
    this.supportsWhatsapp = const Value.absent(),
    this.cellPhoneNumberOther = const Value.absent(),
    this.email = const Value.absent(),
    this.catSexId = const Value.absent(),
    this.catSexNombre = const Value.absent(),
    this.birthEntityId = const Value.absent(),
    this.isInSync = const Value.absent(),
    required String supportsMovement,
    this.committeeParticipation = const Value.absent(),
    this.pollinStationRepresentativeParticipation = const Value.absent(),
    this.invitingOthersToParticipate = const Value.absent(),
    this.numberOfPeopleInvited = const Value.absent(),
    this.created = const Value.absent(),
    this.positionId = const Value.absent(),
    this.syncStatus = const Value.absent(),
    this.syncError = const Value.absent(),
    this.membershipId = const Value.absent(),
    this.positionLabel = const Value.absent(),
    this.syncedAt = const Value.absent(),
  })  : uuid = Value(uuid),
        deviceId = Value(deviceId),
        latitude = Value(latitude),
        longitude = Value(longitude),
        sectionNumber = Value(sectionNumber),
        street = Value(street),
        externalNumber = Value(externalNumber),
        supportsMovement = Value(supportsMovement);
  static Insertable<FollowerRow> custom({
    Expression<int>? id,
    Expression<String>? uuid,
    Expression<String>? deviceId,
    Expression<String>? latitude,
    Expression<String>? longitude,
    Expression<String>? sectionNumber,
    Expression<String>? street,
    Expression<String>? externalNumber,
    Expression<String>? internalNumber,
    Expression<String>? zipCode,
    Expression<String>? neighborhood,
    Expression<String>? sectionNumberIne,
    Expression<int>? residenceEntityId,
    Expression<int>? residenceMunicipalityId,
    Expression<bool>? isAdult,
    Expression<String>? names,
    Expression<String>? lastName,
    Expression<String>? surName,
    Expression<String>? curp,
    Expression<String>? ine,
    Expression<String>? validity,
    Expression<String>? birthdate,
    Expression<String>? cellPhoneNumber,
    Expression<String>? supportsWhatsapp,
    Expression<String>? cellPhoneNumberOther,
    Expression<String>? email,
    Expression<int>? catSexId,
    Expression<String>? catSexNombre,
    Expression<int>? birthEntityId,
    Expression<bool>? isInSync,
    Expression<String>? supportsMovement,
    Expression<String>? committeeParticipation,
    Expression<String>? pollinStationRepresentativeParticipation,
    Expression<String>? invitingOthersToParticipate,
    Expression<String>? numberOfPeopleInvited,
    Expression<DateTime>? created,
    Expression<int>? positionId,
    Expression<String>? syncStatus,
    Expression<String>? syncError,
    Expression<int>? membershipId,
    Expression<String>? positionLabel,
    Expression<DateTime>? syncedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (uuid != null) 'uuid': uuid,
      if (deviceId != null) 'device_id': deviceId,
      if (latitude != null) 'latitude': latitude,
      if (longitude != null) 'longitude': longitude,
      if (sectionNumber != null) 'section_number': sectionNumber,
      if (street != null) 'street': street,
      if (externalNumber != null) 'external_number': externalNumber,
      if (internalNumber != null) 'internal_number': internalNumber,
      if (zipCode != null) 'zip_code': zipCode,
      if (neighborhood != null) 'neighborhood': neighborhood,
      if (sectionNumberIne != null) 'section_number_ine': sectionNumberIne,
      if (residenceEntityId != null) 'residence_entity_id': residenceEntityId,
      if (residenceMunicipalityId != null)
        'residence_municipality_id': residenceMunicipalityId,
      if (isAdult != null) 'is_adult': isAdult,
      if (names != null) 'names': names,
      if (lastName != null) 'last_name': lastName,
      if (surName != null) 'sur_name': surName,
      if (curp != null) 'curp': curp,
      if (ine != null) 'ine': ine,
      if (validity != null) 'validity': validity,
      if (birthdate != null) 'birthdate': birthdate,
      if (cellPhoneNumber != null) 'cell_phone_number': cellPhoneNumber,
      if (supportsWhatsapp != null) 'supports_whatsapp': supportsWhatsapp,
      if (cellPhoneNumberOther != null)
        'cell_phone_number_other': cellPhoneNumberOther,
      if (email != null) 'email': email,
      if (catSexId != null) 'cat_sex_id': catSexId,
      if (catSexNombre != null) 'cat_sex_nombre': catSexNombre,
      if (birthEntityId != null) 'birth_entity_id': birthEntityId,
      if (isInSync != null) 'is_in_sync': isInSync,
      if (supportsMovement != null) 'supports_movement': supportsMovement,
      if (committeeParticipation != null)
        'committee_participation': committeeParticipation,
      if (pollinStationRepresentativeParticipation != null)
        'pollin_station_representative_participation':
            pollinStationRepresentativeParticipation,
      if (invitingOthersToParticipate != null)
        'inviting_others_to_participate': invitingOthersToParticipate,
      if (numberOfPeopleInvited != null)
        'number_of_people_invited': numberOfPeopleInvited,
      if (created != null) 'created': created,
      if (positionId != null) 'position_id': positionId,
      if (syncStatus != null) 'sync_status': syncStatus,
      if (syncError != null) 'sync_error': syncError,
      if (membershipId != null) 'membership_id': membershipId,
      if (positionLabel != null) 'position_label': positionLabel,
      if (syncedAt != null) 'synced_at': syncedAt,
    });
  }

  FollowersCompanion copyWith(
      {Value<int>? id,
      Value<String>? uuid,
      Value<String>? deviceId,
      Value<String>? latitude,
      Value<String>? longitude,
      Value<String>? sectionNumber,
      Value<String>? street,
      Value<String>? externalNumber,
      Value<String?>? internalNumber,
      Value<String?>? zipCode,
      Value<String?>? neighborhood,
      Value<String?>? sectionNumberIne,
      Value<int?>? residenceEntityId,
      Value<int?>? residenceMunicipalityId,
      Value<bool?>? isAdult,
      Value<String?>? names,
      Value<String?>? lastName,
      Value<String?>? surName,
      Value<String?>? curp,
      Value<String?>? ine,
      Value<String?>? validity,
      Value<String?>? birthdate,
      Value<String?>? cellPhoneNumber,
      Value<String?>? supportsWhatsapp,
      Value<String?>? cellPhoneNumberOther,
      Value<String?>? email,
      Value<int?>? catSexId,
      Value<String?>? catSexNombre,
      Value<int?>? birthEntityId,
      Value<bool>? isInSync,
      Value<String>? supportsMovement,
      Value<String?>? committeeParticipation,
      Value<String?>? pollinStationRepresentativeParticipation,
      Value<String?>? invitingOthersToParticipate,
      Value<String?>? numberOfPeopleInvited,
      Value<DateTime?>? created,
      Value<int?>? positionId,
      Value<String>? syncStatus,
      Value<String?>? syncError,
      Value<int?>? membershipId,
      Value<String?>? positionLabel,
      Value<DateTime?>? syncedAt}) {
    return FollowersCompanion(
      id: id ?? this.id,
      uuid: uuid ?? this.uuid,
      deviceId: deviceId ?? this.deviceId,
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      sectionNumber: sectionNumber ?? this.sectionNumber,
      street: street ?? this.street,
      externalNumber: externalNumber ?? this.externalNumber,
      internalNumber: internalNumber ?? this.internalNumber,
      zipCode: zipCode ?? this.zipCode,
      neighborhood: neighborhood ?? this.neighborhood,
      sectionNumberIne: sectionNumberIne ?? this.sectionNumberIne,
      residenceEntityId: residenceEntityId ?? this.residenceEntityId,
      residenceMunicipalityId:
          residenceMunicipalityId ?? this.residenceMunicipalityId,
      isAdult: isAdult ?? this.isAdult,
      names: names ?? this.names,
      lastName: lastName ?? this.lastName,
      surName: surName ?? this.surName,
      curp: curp ?? this.curp,
      ine: ine ?? this.ine,
      validity: validity ?? this.validity,
      birthdate: birthdate ?? this.birthdate,
      cellPhoneNumber: cellPhoneNumber ?? this.cellPhoneNumber,
      supportsWhatsapp: supportsWhatsapp ?? this.supportsWhatsapp,
      cellPhoneNumberOther: cellPhoneNumberOther ?? this.cellPhoneNumberOther,
      email: email ?? this.email,
      catSexId: catSexId ?? this.catSexId,
      catSexNombre: catSexNombre ?? this.catSexNombre,
      birthEntityId: birthEntityId ?? this.birthEntityId,
      isInSync: isInSync ?? this.isInSync,
      supportsMovement: supportsMovement ?? this.supportsMovement,
      committeeParticipation:
          committeeParticipation ?? this.committeeParticipation,
      pollinStationRepresentativeParticipation:
          pollinStationRepresentativeParticipation ??
              this.pollinStationRepresentativeParticipation,
      invitingOthersToParticipate:
          invitingOthersToParticipate ?? this.invitingOthersToParticipate,
      numberOfPeopleInvited:
          numberOfPeopleInvited ?? this.numberOfPeopleInvited,
      created: created ?? this.created,
      positionId: positionId ?? this.positionId,
      syncStatus: syncStatus ?? this.syncStatus,
      syncError: syncError ?? this.syncError,
      membershipId: membershipId ?? this.membershipId,
      positionLabel: positionLabel ?? this.positionLabel,
      syncedAt: syncedAt ?? this.syncedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (uuid.present) {
      map['uuid'] = Variable<String>(uuid.value);
    }
    if (deviceId.present) {
      map['device_id'] = Variable<String>(deviceId.value);
    }
    if (latitude.present) {
      map['latitude'] = Variable<String>(latitude.value);
    }
    if (longitude.present) {
      map['longitude'] = Variable<String>(longitude.value);
    }
    if (sectionNumber.present) {
      map['section_number'] = Variable<String>(sectionNumber.value);
    }
    if (street.present) {
      map['street'] = Variable<String>(street.value);
    }
    if (externalNumber.present) {
      map['external_number'] = Variable<String>(externalNumber.value);
    }
    if (internalNumber.present) {
      map['internal_number'] = Variable<String>(internalNumber.value);
    }
    if (zipCode.present) {
      map['zip_code'] = Variable<String>(zipCode.value);
    }
    if (neighborhood.present) {
      map['neighborhood'] = Variable<String>(neighborhood.value);
    }
    if (sectionNumberIne.present) {
      map['section_number_ine'] = Variable<String>(sectionNumberIne.value);
    }
    if (residenceEntityId.present) {
      map['residence_entity_id'] = Variable<int>(residenceEntityId.value);
    }
    if (residenceMunicipalityId.present) {
      map['residence_municipality_id'] =
          Variable<int>(residenceMunicipalityId.value);
    }
    if (isAdult.present) {
      map['is_adult'] = Variable<bool>(isAdult.value);
    }
    if (names.present) {
      map['names'] = Variable<String>(names.value);
    }
    if (lastName.present) {
      map['last_name'] = Variable<String>(lastName.value);
    }
    if (surName.present) {
      map['sur_name'] = Variable<String>(surName.value);
    }
    if (curp.present) {
      map['curp'] = Variable<String>(curp.value);
    }
    if (ine.present) {
      map['ine'] = Variable<String>(ine.value);
    }
    if (validity.present) {
      map['validity'] = Variable<String>(validity.value);
    }
    if (birthdate.present) {
      map['birthdate'] = Variable<String>(birthdate.value);
    }
    if (cellPhoneNumber.present) {
      map['cell_phone_number'] = Variable<String>(cellPhoneNumber.value);
    }
    if (supportsWhatsapp.present) {
      map['supports_whatsapp'] = Variable<String>(supportsWhatsapp.value);
    }
    if (cellPhoneNumberOther.present) {
      map['cell_phone_number_other'] =
          Variable<String>(cellPhoneNumberOther.value);
    }
    if (email.present) {
      map['email'] = Variable<String>(email.value);
    }
    if (catSexId.present) {
      map['cat_sex_id'] = Variable<int>(catSexId.value);
    }
    if (catSexNombre.present) {
      map['cat_sex_nombre'] = Variable<String>(catSexNombre.value);
    }
    if (birthEntityId.present) {
      map['birth_entity_id'] = Variable<int>(birthEntityId.value);
    }
    if (isInSync.present) {
      map['is_in_sync'] = Variable<bool>(isInSync.value);
    }
    if (supportsMovement.present) {
      map['supports_movement'] = Variable<String>(supportsMovement.value);
    }
    if (committeeParticipation.present) {
      map['committee_participation'] =
          Variable<String>(committeeParticipation.value);
    }
    if (pollinStationRepresentativeParticipation.present) {
      map['pollin_station_representative_participation'] =
          Variable<String>(pollinStationRepresentativeParticipation.value);
    }
    if (invitingOthersToParticipate.present) {
      map['inviting_others_to_participate'] =
          Variable<String>(invitingOthersToParticipate.value);
    }
    if (numberOfPeopleInvited.present) {
      map['number_of_people_invited'] =
          Variable<String>(numberOfPeopleInvited.value);
    }
    if (created.present) {
      map['created'] = Variable<DateTime>(created.value);
    }
    if (positionId.present) {
      map['position_id'] = Variable<int>(positionId.value);
    }
    if (syncStatus.present) {
      map['sync_status'] = Variable<String>(syncStatus.value);
    }
    if (syncError.present) {
      map['sync_error'] = Variable<String>(syncError.value);
    }
    if (membershipId.present) {
      map['membership_id'] = Variable<int>(membershipId.value);
    }
    if (positionLabel.present) {
      map['position_label'] = Variable<String>(positionLabel.value);
    }
    if (syncedAt.present) {
      map['synced_at'] = Variable<DateTime>(syncedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('FollowersCompanion(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('deviceId: $deviceId, ')
          ..write('latitude: $latitude, ')
          ..write('longitude: $longitude, ')
          ..write('sectionNumber: $sectionNumber, ')
          ..write('street: $street, ')
          ..write('externalNumber: $externalNumber, ')
          ..write('internalNumber: $internalNumber, ')
          ..write('zipCode: $zipCode, ')
          ..write('neighborhood: $neighborhood, ')
          ..write('sectionNumberIne: $sectionNumberIne, ')
          ..write('residenceEntityId: $residenceEntityId, ')
          ..write('residenceMunicipalityId: $residenceMunicipalityId, ')
          ..write('isAdult: $isAdult, ')
          ..write('names: $names, ')
          ..write('lastName: $lastName, ')
          ..write('surName: $surName, ')
          ..write('curp: $curp, ')
          ..write('ine: $ine, ')
          ..write('validity: $validity, ')
          ..write('birthdate: $birthdate, ')
          ..write('cellPhoneNumber: $cellPhoneNumber, ')
          ..write('supportsWhatsapp: $supportsWhatsapp, ')
          ..write('cellPhoneNumberOther: $cellPhoneNumberOther, ')
          ..write('email: $email, ')
          ..write('catSexId: $catSexId, ')
          ..write('catSexNombre: $catSexNombre, ')
          ..write('birthEntityId: $birthEntityId, ')
          ..write('isInSync: $isInSync, ')
          ..write('supportsMovement: $supportsMovement, ')
          ..write('committeeParticipation: $committeeParticipation, ')
          ..write(
              'pollinStationRepresentativeParticipation: $pollinStationRepresentativeParticipation, ')
          ..write('invitingOthersToParticipate: $invitingOthersToParticipate, ')
          ..write('numberOfPeopleInvited: $numberOfPeopleInvited, ')
          ..write('created: $created, ')
          ..write('positionId: $positionId, ')
          ..write('syncStatus: $syncStatus, ')
          ..write('syncError: $syncError, ')
          ..write('membershipId: $membershipId, ')
          ..write('positionLabel: $positionLabel, ')
          ..write('syncedAt: $syncedAt')
          ..write(')'))
        .toString();
  }
}

class $UserSettingsTableTable extends UserSettingsTable
    with TableInfo<$UserSettingsTableTable, UserSettingsRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $UserSettingsTableTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _municipalityIdMeta =
      const VerificationMeta('municipalityId');
  @override
  late final GeneratedColumn<int> municipalityId = GeneratedColumn<int>(
      'municipality_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _municipalityNameMeta =
      const VerificationMeta('municipalityName');
  @override
  late final GeneratedColumn<String> municipalityName = GeneratedColumn<String>(
      'municipality_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _sectionsSelectedMeta =
      const VerificationMeta('sectionsSelected');
  @override
  late final GeneratedColumn<String> sectionsSelected = GeneratedColumn<String>(
      'sections_selected', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('[]'));
  @override
  List<GeneratedColumn> get $columns =>
      [id, municipalityId, municipalityName, sectionsSelected];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'user_settings_table';
  @override
  VerificationContext validateIntegrity(Insertable<UserSettingsRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('municipality_id')) {
      context.handle(
          _municipalityIdMeta,
          municipalityId.isAcceptableOrUnknown(
              data['municipality_id']!, _municipalityIdMeta));
    }
    if (data.containsKey('municipality_name')) {
      context.handle(
          _municipalityNameMeta,
          municipalityName.isAcceptableOrUnknown(
              data['municipality_name']!, _municipalityNameMeta));
    }
    if (data.containsKey('sections_selected')) {
      context.handle(
          _sectionsSelectedMeta,
          sectionsSelected.isAcceptableOrUnknown(
              data['sections_selected']!, _sectionsSelectedMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  UserSettingsRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return UserSettingsRow(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      municipalityId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}municipality_id']),
      municipalityName: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}municipality_name']),
      sectionsSelected: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}sections_selected'])!,
    );
  }

  @override
  $UserSettingsTableTable createAlias(String alias) {
    return $UserSettingsTableTable(attachedDatabase, alias);
  }
}

class UserSettingsRow extends DataClass implements Insertable<UserSettingsRow> {
  final int id;
  final int? municipalityId;
  final String? municipalityName;
  final String sectionsSelected;
  const UserSettingsRow(
      {required this.id,
      this.municipalityId,
      this.municipalityName,
      required this.sectionsSelected});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    if (!nullToAbsent || municipalityId != null) {
      map['municipality_id'] = Variable<int>(municipalityId);
    }
    if (!nullToAbsent || municipalityName != null) {
      map['municipality_name'] = Variable<String>(municipalityName);
    }
    map['sections_selected'] = Variable<String>(sectionsSelected);
    return map;
  }

  UserSettingsTableCompanion toCompanion(bool nullToAbsent) {
    return UserSettingsTableCompanion(
      id: Value(id),
      municipalityId: municipalityId == null && nullToAbsent
          ? const Value.absent()
          : Value(municipalityId),
      municipalityName: municipalityName == null && nullToAbsent
          ? const Value.absent()
          : Value(municipalityName),
      sectionsSelected: Value(sectionsSelected),
    );
  }

  factory UserSettingsRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return UserSettingsRow(
      id: serializer.fromJson<int>(json['id']),
      municipalityId: serializer.fromJson<int?>(json['municipalityId']),
      municipalityName: serializer.fromJson<String?>(json['municipalityName']),
      sectionsSelected: serializer.fromJson<String>(json['sectionsSelected']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'municipalityId': serializer.toJson<int?>(municipalityId),
      'municipalityName': serializer.toJson<String?>(municipalityName),
      'sectionsSelected': serializer.toJson<String>(sectionsSelected),
    };
  }

  UserSettingsRow copyWith(
          {int? id,
          Value<int?> municipalityId = const Value.absent(),
          Value<String?> municipalityName = const Value.absent(),
          String? sectionsSelected}) =>
      UserSettingsRow(
        id: id ?? this.id,
        municipalityId:
            municipalityId.present ? municipalityId.value : this.municipalityId,
        municipalityName: municipalityName.present
            ? municipalityName.value
            : this.municipalityName,
        sectionsSelected: sectionsSelected ?? this.sectionsSelected,
      );
  UserSettingsRow copyWithCompanion(UserSettingsTableCompanion data) {
    return UserSettingsRow(
      id: data.id.present ? data.id.value : this.id,
      municipalityId: data.municipalityId.present
          ? data.municipalityId.value
          : this.municipalityId,
      municipalityName: data.municipalityName.present
          ? data.municipalityName.value
          : this.municipalityName,
      sectionsSelected: data.sectionsSelected.present
          ? data.sectionsSelected.value
          : this.sectionsSelected,
    );
  }

  @override
  String toString() {
    return (StringBuffer('UserSettingsRow(')
          ..write('id: $id, ')
          ..write('municipalityId: $municipalityId, ')
          ..write('municipalityName: $municipalityName, ')
          ..write('sectionsSelected: $sectionsSelected')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, municipalityId, municipalityName, sectionsSelected);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is UserSettingsRow &&
          other.id == this.id &&
          other.municipalityId == this.municipalityId &&
          other.municipalityName == this.municipalityName &&
          other.sectionsSelected == this.sectionsSelected);
}

class UserSettingsTableCompanion extends UpdateCompanion<UserSettingsRow> {
  final Value<int> id;
  final Value<int?> municipalityId;
  final Value<String?> municipalityName;
  final Value<String> sectionsSelected;
  const UserSettingsTableCompanion({
    this.id = const Value.absent(),
    this.municipalityId = const Value.absent(),
    this.municipalityName = const Value.absent(),
    this.sectionsSelected = const Value.absent(),
  });
  UserSettingsTableCompanion.insert({
    this.id = const Value.absent(),
    this.municipalityId = const Value.absent(),
    this.municipalityName = const Value.absent(),
    this.sectionsSelected = const Value.absent(),
  });
  static Insertable<UserSettingsRow> custom({
    Expression<int>? id,
    Expression<int>? municipalityId,
    Expression<String>? municipalityName,
    Expression<String>? sectionsSelected,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (municipalityId != null) 'municipality_id': municipalityId,
      if (municipalityName != null) 'municipality_name': municipalityName,
      if (sectionsSelected != null) 'sections_selected': sectionsSelected,
    });
  }

  UserSettingsTableCompanion copyWith(
      {Value<int>? id,
      Value<int?>? municipalityId,
      Value<String?>? municipalityName,
      Value<String>? sectionsSelected}) {
    return UserSettingsTableCompanion(
      id: id ?? this.id,
      municipalityId: municipalityId ?? this.municipalityId,
      municipalityName: municipalityName ?? this.municipalityName,
      sectionsSelected: sectionsSelected ?? this.sectionsSelected,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (municipalityId.present) {
      map['municipality_id'] = Variable<int>(municipalityId.value);
    }
    if (municipalityName.present) {
      map['municipality_name'] = Variable<String>(municipalityName.value);
    }
    if (sectionsSelected.present) {
      map['sections_selected'] = Variable<String>(sectionsSelected.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('UserSettingsTableCompanion(')
          ..write('id: $id, ')
          ..write('municipalityId: $municipalityId, ')
          ..write('municipalityName: $municipalityName, ')
          ..write('sectionsSelected: $sectionsSelected')
          ..write(')'))
        .toString();
  }
}

class $IssuesTable extends Issues with TableInfo<$IssuesTable, IssueRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $IssuesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _uuidMeta = const VerificationMeta('uuid');
  @override
  late final GeneratedColumn<String> uuid = GeneratedColumn<String>(
      'uuid', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _registrationDateMeta =
      const VerificationMeta('registrationDate');
  @override
  late final GeneratedColumn<String> registrationDate = GeneratedColumn<String>(
      'registration_date', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _fullNameMeta =
      const VerificationMeta('fullName');
  @override
  late final GeneratedColumn<String> fullName = GeneratedColumn<String>(
      'full_name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _phoneMeta = const VerificationMeta('phone');
  @override
  late final GeneratedColumn<String> phone = GeneratedColumn<String>(
      'phone', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _municipalityIdMeta =
      const VerificationMeta('municipalityId');
  @override
  late final GeneratedColumn<int> municipalityId = GeneratedColumn<int>(
      'municipality_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _municipalityNameMeta =
      const VerificationMeta('municipalityName');
  @override
  late final GeneratedColumn<String> municipalityName = GeneratedColumn<String>(
      'municipality_name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _localityIdMeta =
      const VerificationMeta('localityId');
  @override
  late final GeneratedColumn<int> localityId = GeneratedColumn<int>(
      'locality_id', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _localityNameMeta =
      const VerificationMeta('localityName');
  @override
  late final GeneratedColumn<String> localityName = GeneratedColumn<String>(
      'locality_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _localityOtherMeta =
      const VerificationMeta('localityOther');
  @override
  late final GeneratedColumn<String> localityOther = GeneratedColumn<String>(
      'locality_other', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _isInSyncMeta =
      const VerificationMeta('isInSync');
  @override
  late final GeneratedColumn<bool> isInSync = GeneratedColumn<bool>(
      'is_in_sync', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("is_in_sync" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _createdMeta =
      const VerificationMeta('created');
  @override
  late final GeneratedColumn<DateTime> created = GeneratedColumn<DateTime>(
      'created', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        uuid,
        registrationDate,
        fullName,
        phone,
        municipalityId,
        municipalityName,
        localityId,
        localityName,
        localityOther,
        isInSync,
        created
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'issues';
  @override
  VerificationContext validateIntegrity(Insertable<IssueRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('uuid')) {
      context.handle(
          _uuidMeta, uuid.isAcceptableOrUnknown(data['uuid']!, _uuidMeta));
    } else if (isInserting) {
      context.missing(_uuidMeta);
    }
    if (data.containsKey('registration_date')) {
      context.handle(
          _registrationDateMeta,
          registrationDate.isAcceptableOrUnknown(
              data['registration_date']!, _registrationDateMeta));
    } else if (isInserting) {
      context.missing(_registrationDateMeta);
    }
    if (data.containsKey('full_name')) {
      context.handle(_fullNameMeta,
          fullName.isAcceptableOrUnknown(data['full_name']!, _fullNameMeta));
    } else if (isInserting) {
      context.missing(_fullNameMeta);
    }
    if (data.containsKey('phone')) {
      context.handle(
          _phoneMeta, phone.isAcceptableOrUnknown(data['phone']!, _phoneMeta));
    } else if (isInserting) {
      context.missing(_phoneMeta);
    }
    if (data.containsKey('municipality_id')) {
      context.handle(
          _municipalityIdMeta,
          municipalityId.isAcceptableOrUnknown(
              data['municipality_id']!, _municipalityIdMeta));
    } else if (isInserting) {
      context.missing(_municipalityIdMeta);
    }
    if (data.containsKey('municipality_name')) {
      context.handle(
          _municipalityNameMeta,
          municipalityName.isAcceptableOrUnknown(
              data['municipality_name']!, _municipalityNameMeta));
    } else if (isInserting) {
      context.missing(_municipalityNameMeta);
    }
    if (data.containsKey('locality_id')) {
      context.handle(
          _localityIdMeta,
          localityId.isAcceptableOrUnknown(
              data['locality_id']!, _localityIdMeta));
    }
    if (data.containsKey('locality_name')) {
      context.handle(
          _localityNameMeta,
          localityName.isAcceptableOrUnknown(
              data['locality_name']!, _localityNameMeta));
    }
    if (data.containsKey('locality_other')) {
      context.handle(
          _localityOtherMeta,
          localityOther.isAcceptableOrUnknown(
              data['locality_other']!, _localityOtherMeta));
    }
    if (data.containsKey('is_in_sync')) {
      context.handle(_isInSyncMeta,
          isInSync.isAcceptableOrUnknown(data['is_in_sync']!, _isInSyncMeta));
    }
    if (data.containsKey('created')) {
      context.handle(_createdMeta,
          created.isAcceptableOrUnknown(data['created']!, _createdMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  IssueRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return IssueRow(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      uuid: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}uuid'])!,
      registrationDate: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}registration_date'])!,
      fullName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}full_name'])!,
      phone: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}phone'])!,
      municipalityId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}municipality_id'])!,
      municipalityName: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}municipality_name'])!,
      localityId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}locality_id'])!,
      localityName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}locality_name']),
      localityOther: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}locality_other']),
      isInSync: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}is_in_sync'])!,
      created: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created']),
    );
  }

  @override
  $IssuesTable createAlias(String alias) {
    return $IssuesTable(attachedDatabase, alias);
  }
}

class IssueRow extends DataClass implements Insertable<IssueRow> {
  final int id;
  final String uuid;
  final String registrationDate;
  final String fullName;
  final String phone;
  final int municipalityId;
  final String municipalityName;
  final int localityId;
  final String? localityName;
  final String? localityOther;
  final bool isInSync;
  final DateTime? created;
  const IssueRow(
      {required this.id,
      required this.uuid,
      required this.registrationDate,
      required this.fullName,
      required this.phone,
      required this.municipalityId,
      required this.municipalityName,
      required this.localityId,
      this.localityName,
      this.localityOther,
      required this.isInSync,
      this.created});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['uuid'] = Variable<String>(uuid);
    map['registration_date'] = Variable<String>(registrationDate);
    map['full_name'] = Variable<String>(fullName);
    map['phone'] = Variable<String>(phone);
    map['municipality_id'] = Variable<int>(municipalityId);
    map['municipality_name'] = Variable<String>(municipalityName);
    map['locality_id'] = Variable<int>(localityId);
    if (!nullToAbsent || localityName != null) {
      map['locality_name'] = Variable<String>(localityName);
    }
    if (!nullToAbsent || localityOther != null) {
      map['locality_other'] = Variable<String>(localityOther);
    }
    map['is_in_sync'] = Variable<bool>(isInSync);
    if (!nullToAbsent || created != null) {
      map['created'] = Variable<DateTime>(created);
    }
    return map;
  }

  IssuesCompanion toCompanion(bool nullToAbsent) {
    return IssuesCompanion(
      id: Value(id),
      uuid: Value(uuid),
      registrationDate: Value(registrationDate),
      fullName: Value(fullName),
      phone: Value(phone),
      municipalityId: Value(municipalityId),
      municipalityName: Value(municipalityName),
      localityId: Value(localityId),
      localityName: localityName == null && nullToAbsent
          ? const Value.absent()
          : Value(localityName),
      localityOther: localityOther == null && nullToAbsent
          ? const Value.absent()
          : Value(localityOther),
      isInSync: Value(isInSync),
      created: created == null && nullToAbsent
          ? const Value.absent()
          : Value(created),
    );
  }

  factory IssueRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return IssueRow(
      id: serializer.fromJson<int>(json['id']),
      uuid: serializer.fromJson<String>(json['uuid']),
      registrationDate: serializer.fromJson<String>(json['registrationDate']),
      fullName: serializer.fromJson<String>(json['fullName']),
      phone: serializer.fromJson<String>(json['phone']),
      municipalityId: serializer.fromJson<int>(json['municipalityId']),
      municipalityName: serializer.fromJson<String>(json['municipalityName']),
      localityId: serializer.fromJson<int>(json['localityId']),
      localityName: serializer.fromJson<String?>(json['localityName']),
      localityOther: serializer.fromJson<String?>(json['localityOther']),
      isInSync: serializer.fromJson<bool>(json['isInSync']),
      created: serializer.fromJson<DateTime?>(json['created']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'uuid': serializer.toJson<String>(uuid),
      'registrationDate': serializer.toJson<String>(registrationDate),
      'fullName': serializer.toJson<String>(fullName),
      'phone': serializer.toJson<String>(phone),
      'municipalityId': serializer.toJson<int>(municipalityId),
      'municipalityName': serializer.toJson<String>(municipalityName),
      'localityId': serializer.toJson<int>(localityId),
      'localityName': serializer.toJson<String?>(localityName),
      'localityOther': serializer.toJson<String?>(localityOther),
      'isInSync': serializer.toJson<bool>(isInSync),
      'created': serializer.toJson<DateTime?>(created),
    };
  }

  IssueRow copyWith(
          {int? id,
          String? uuid,
          String? registrationDate,
          String? fullName,
          String? phone,
          int? municipalityId,
          String? municipalityName,
          int? localityId,
          Value<String?> localityName = const Value.absent(),
          Value<String?> localityOther = const Value.absent(),
          bool? isInSync,
          Value<DateTime?> created = const Value.absent()}) =>
      IssueRow(
        id: id ?? this.id,
        uuid: uuid ?? this.uuid,
        registrationDate: registrationDate ?? this.registrationDate,
        fullName: fullName ?? this.fullName,
        phone: phone ?? this.phone,
        municipalityId: municipalityId ?? this.municipalityId,
        municipalityName: municipalityName ?? this.municipalityName,
        localityId: localityId ?? this.localityId,
        localityName:
            localityName.present ? localityName.value : this.localityName,
        localityOther:
            localityOther.present ? localityOther.value : this.localityOther,
        isInSync: isInSync ?? this.isInSync,
        created: created.present ? created.value : this.created,
      );
  IssueRow copyWithCompanion(IssuesCompanion data) {
    return IssueRow(
      id: data.id.present ? data.id.value : this.id,
      uuid: data.uuid.present ? data.uuid.value : this.uuid,
      registrationDate: data.registrationDate.present
          ? data.registrationDate.value
          : this.registrationDate,
      fullName: data.fullName.present ? data.fullName.value : this.fullName,
      phone: data.phone.present ? data.phone.value : this.phone,
      municipalityId: data.municipalityId.present
          ? data.municipalityId.value
          : this.municipalityId,
      municipalityName: data.municipalityName.present
          ? data.municipalityName.value
          : this.municipalityName,
      localityId:
          data.localityId.present ? data.localityId.value : this.localityId,
      localityName: data.localityName.present
          ? data.localityName.value
          : this.localityName,
      localityOther: data.localityOther.present
          ? data.localityOther.value
          : this.localityOther,
      isInSync: data.isInSync.present ? data.isInSync.value : this.isInSync,
      created: data.created.present ? data.created.value : this.created,
    );
  }

  @override
  String toString() {
    return (StringBuffer('IssueRow(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('registrationDate: $registrationDate, ')
          ..write('fullName: $fullName, ')
          ..write('phone: $phone, ')
          ..write('municipalityId: $municipalityId, ')
          ..write('municipalityName: $municipalityName, ')
          ..write('localityId: $localityId, ')
          ..write('localityName: $localityName, ')
          ..write('localityOther: $localityOther, ')
          ..write('isInSync: $isInSync, ')
          ..write('created: $created')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id,
      uuid,
      registrationDate,
      fullName,
      phone,
      municipalityId,
      municipalityName,
      localityId,
      localityName,
      localityOther,
      isInSync,
      created);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is IssueRow &&
          other.id == this.id &&
          other.uuid == this.uuid &&
          other.registrationDate == this.registrationDate &&
          other.fullName == this.fullName &&
          other.phone == this.phone &&
          other.municipalityId == this.municipalityId &&
          other.municipalityName == this.municipalityName &&
          other.localityId == this.localityId &&
          other.localityName == this.localityName &&
          other.localityOther == this.localityOther &&
          other.isInSync == this.isInSync &&
          other.created == this.created);
}

class IssuesCompanion extends UpdateCompanion<IssueRow> {
  final Value<int> id;
  final Value<String> uuid;
  final Value<String> registrationDate;
  final Value<String> fullName;
  final Value<String> phone;
  final Value<int> municipalityId;
  final Value<String> municipalityName;
  final Value<int> localityId;
  final Value<String?> localityName;
  final Value<String?> localityOther;
  final Value<bool> isInSync;
  final Value<DateTime?> created;
  const IssuesCompanion({
    this.id = const Value.absent(),
    this.uuid = const Value.absent(),
    this.registrationDate = const Value.absent(),
    this.fullName = const Value.absent(),
    this.phone = const Value.absent(),
    this.municipalityId = const Value.absent(),
    this.municipalityName = const Value.absent(),
    this.localityId = const Value.absent(),
    this.localityName = const Value.absent(),
    this.localityOther = const Value.absent(),
    this.isInSync = const Value.absent(),
    this.created = const Value.absent(),
  });
  IssuesCompanion.insert({
    this.id = const Value.absent(),
    required String uuid,
    required String registrationDate,
    required String fullName,
    required String phone,
    required int municipalityId,
    required String municipalityName,
    this.localityId = const Value.absent(),
    this.localityName = const Value.absent(),
    this.localityOther = const Value.absent(),
    this.isInSync = const Value.absent(),
    this.created = const Value.absent(),
  })  : uuid = Value(uuid),
        registrationDate = Value(registrationDate),
        fullName = Value(fullName),
        phone = Value(phone),
        municipalityId = Value(municipalityId),
        municipalityName = Value(municipalityName);
  static Insertable<IssueRow> custom({
    Expression<int>? id,
    Expression<String>? uuid,
    Expression<String>? registrationDate,
    Expression<String>? fullName,
    Expression<String>? phone,
    Expression<int>? municipalityId,
    Expression<String>? municipalityName,
    Expression<int>? localityId,
    Expression<String>? localityName,
    Expression<String>? localityOther,
    Expression<bool>? isInSync,
    Expression<DateTime>? created,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (uuid != null) 'uuid': uuid,
      if (registrationDate != null) 'registration_date': registrationDate,
      if (fullName != null) 'full_name': fullName,
      if (phone != null) 'phone': phone,
      if (municipalityId != null) 'municipality_id': municipalityId,
      if (municipalityName != null) 'municipality_name': municipalityName,
      if (localityId != null) 'locality_id': localityId,
      if (localityName != null) 'locality_name': localityName,
      if (localityOther != null) 'locality_other': localityOther,
      if (isInSync != null) 'is_in_sync': isInSync,
      if (created != null) 'created': created,
    });
  }

  IssuesCompanion copyWith(
      {Value<int>? id,
      Value<String>? uuid,
      Value<String>? registrationDate,
      Value<String>? fullName,
      Value<String>? phone,
      Value<int>? municipalityId,
      Value<String>? municipalityName,
      Value<int>? localityId,
      Value<String?>? localityName,
      Value<String?>? localityOther,
      Value<bool>? isInSync,
      Value<DateTime?>? created}) {
    return IssuesCompanion(
      id: id ?? this.id,
      uuid: uuid ?? this.uuid,
      registrationDate: registrationDate ?? this.registrationDate,
      fullName: fullName ?? this.fullName,
      phone: phone ?? this.phone,
      municipalityId: municipalityId ?? this.municipalityId,
      municipalityName: municipalityName ?? this.municipalityName,
      localityId: localityId ?? this.localityId,
      localityName: localityName ?? this.localityName,
      localityOther: localityOther ?? this.localityOther,
      isInSync: isInSync ?? this.isInSync,
      created: created ?? this.created,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (uuid.present) {
      map['uuid'] = Variable<String>(uuid.value);
    }
    if (registrationDate.present) {
      map['registration_date'] = Variable<String>(registrationDate.value);
    }
    if (fullName.present) {
      map['full_name'] = Variable<String>(fullName.value);
    }
    if (phone.present) {
      map['phone'] = Variable<String>(phone.value);
    }
    if (municipalityId.present) {
      map['municipality_id'] = Variable<int>(municipalityId.value);
    }
    if (municipalityName.present) {
      map['municipality_name'] = Variable<String>(municipalityName.value);
    }
    if (localityId.present) {
      map['locality_id'] = Variable<int>(localityId.value);
    }
    if (localityName.present) {
      map['locality_name'] = Variable<String>(localityName.value);
    }
    if (localityOther.present) {
      map['locality_other'] = Variable<String>(localityOther.value);
    }
    if (isInSync.present) {
      map['is_in_sync'] = Variable<bool>(isInSync.value);
    }
    if (created.present) {
      map['created'] = Variable<DateTime>(created.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('IssuesCompanion(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('registrationDate: $registrationDate, ')
          ..write('fullName: $fullName, ')
          ..write('phone: $phone, ')
          ..write('municipalityId: $municipalityId, ')
          ..write('municipalityName: $municipalityName, ')
          ..write('localityId: $localityId, ')
          ..write('localityName: $localityName, ')
          ..write('localityOther: $localityOther, ')
          ..write('isInSync: $isInSync, ')
          ..write('created: $created')
          ..write(')'))
        .toString();
  }
}

class $IssueDetailsTable extends IssueDetails
    with TableInfo<$IssueDetailsTable, IssueDetailRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $IssueDetailsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _uuidMeta = const VerificationMeta('uuid');
  @override
  late final GeneratedColumn<String> uuid = GeneratedColumn<String>(
      'uuid', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _issueUuidMeta =
      const VerificationMeta('issueUuid');
  @override
  late final GeneratedColumn<String> issueUuid = GeneratedColumn<String>(
      'issue_uuid', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _commentsMeta =
      const VerificationMeta('comments');
  @override
  late final GeneratedColumn<String> comments = GeneratedColumn<String>(
      'comments', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _urgentMeta = const VerificationMeta('urgent');
  @override
  late final GeneratedColumn<bool> urgent = GeneratedColumn<bool>(
      'urgent', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("urgent" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _issueTypeIdMeta =
      const VerificationMeta('issueTypeId');
  @override
  late final GeneratedColumn<int> issueTypeId = GeneratedColumn<int>(
      'issue_type_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _issueTypeNameMeta =
      const VerificationMeta('issueTypeName');
  @override
  late final GeneratedColumn<String> issueTypeName = GeneratedColumn<String>(
      'issue_type_name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _issueCategoryIdMeta =
      const VerificationMeta('issueCategoryId');
  @override
  late final GeneratedColumn<int> issueCategoryId = GeneratedColumn<int>(
      'issue_category_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _issueCategoryNameMeta =
      const VerificationMeta('issueCategoryName');
  @override
  late final GeneratedColumn<String> issueCategoryName =
      GeneratedColumn<String>('issue_category_name', aliasedName, false,
          type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _issueStatusIdMeta =
      const VerificationMeta('issueStatusId');
  @override
  late final GeneratedColumn<int> issueStatusId = GeneratedColumn<int>(
      'issue_status_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _issueStatusNameMeta =
      const VerificationMeta('issueStatusName');
  @override
  late final GeneratedColumn<String> issueStatusName = GeneratedColumn<String>(
      'issue_status_name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _lastNameMeta =
      const VerificationMeta('lastName');
  @override
  late final GeneratedColumn<String> lastName = GeneratedColumn<String>(
      'last_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _surNameMeta =
      const VerificationMeta('surName');
  @override
  late final GeneratedColumn<String> surName = GeneratedColumn<String>(
      'sur_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _curpMeta = const VerificationMeta('curp');
  @override
  late final GeneratedColumn<String> curp = GeneratedColumn<String>(
      'curp', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _resourceIdMeta =
      const VerificationMeta('resourceId');
  @override
  late final GeneratedColumn<int> resourceId = GeneratedColumn<int>(
      'resource_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _resourceNameMeta =
      const VerificationMeta('resourceName');
  @override
  late final GeneratedColumn<String> resourceName = GeneratedColumn<String>(
      'resource_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _docCategoryIdMeta =
      const VerificationMeta('docCategoryId');
  @override
  late final GeneratedColumn<int> docCategoryId = GeneratedColumn<int>(
      'doc_category_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _docCategoryNameMeta =
      const VerificationMeta('docCategoryName');
  @override
  late final GeneratedColumn<String> docCategoryName = GeneratedColumn<String>(
      'doc_category_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _localFilePathMeta =
      const VerificationMeta('localFilePath');
  @override
  late final GeneratedColumn<String> localFilePath = GeneratedColumn<String>(
      'local_file_path', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        uuid,
        issueUuid,
        comments,
        urgent,
        issueTypeId,
        issueTypeName,
        issueCategoryId,
        issueCategoryName,
        issueStatusId,
        issueStatusName,
        name,
        lastName,
        surName,
        curp,
        resourceId,
        resourceName,
        docCategoryId,
        docCategoryName,
        localFilePath
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'issue_details';
  @override
  VerificationContext validateIntegrity(Insertable<IssueDetailRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('uuid')) {
      context.handle(
          _uuidMeta, uuid.isAcceptableOrUnknown(data['uuid']!, _uuidMeta));
    } else if (isInserting) {
      context.missing(_uuidMeta);
    }
    if (data.containsKey('issue_uuid')) {
      context.handle(_issueUuidMeta,
          issueUuid.isAcceptableOrUnknown(data['issue_uuid']!, _issueUuidMeta));
    } else if (isInserting) {
      context.missing(_issueUuidMeta);
    }
    if (data.containsKey('comments')) {
      context.handle(_commentsMeta,
          comments.isAcceptableOrUnknown(data['comments']!, _commentsMeta));
    } else if (isInserting) {
      context.missing(_commentsMeta);
    }
    if (data.containsKey('urgent')) {
      context.handle(_urgentMeta,
          urgent.isAcceptableOrUnknown(data['urgent']!, _urgentMeta));
    }
    if (data.containsKey('issue_type_id')) {
      context.handle(
          _issueTypeIdMeta,
          issueTypeId.isAcceptableOrUnknown(
              data['issue_type_id']!, _issueTypeIdMeta));
    } else if (isInserting) {
      context.missing(_issueTypeIdMeta);
    }
    if (data.containsKey('issue_type_name')) {
      context.handle(
          _issueTypeNameMeta,
          issueTypeName.isAcceptableOrUnknown(
              data['issue_type_name']!, _issueTypeNameMeta));
    } else if (isInserting) {
      context.missing(_issueTypeNameMeta);
    }
    if (data.containsKey('issue_category_id')) {
      context.handle(
          _issueCategoryIdMeta,
          issueCategoryId.isAcceptableOrUnknown(
              data['issue_category_id']!, _issueCategoryIdMeta));
    } else if (isInserting) {
      context.missing(_issueCategoryIdMeta);
    }
    if (data.containsKey('issue_category_name')) {
      context.handle(
          _issueCategoryNameMeta,
          issueCategoryName.isAcceptableOrUnknown(
              data['issue_category_name']!, _issueCategoryNameMeta));
    } else if (isInserting) {
      context.missing(_issueCategoryNameMeta);
    }
    if (data.containsKey('issue_status_id')) {
      context.handle(
          _issueStatusIdMeta,
          issueStatusId.isAcceptableOrUnknown(
              data['issue_status_id']!, _issueStatusIdMeta));
    } else if (isInserting) {
      context.missing(_issueStatusIdMeta);
    }
    if (data.containsKey('issue_status_name')) {
      context.handle(
          _issueStatusNameMeta,
          issueStatusName.isAcceptableOrUnknown(
              data['issue_status_name']!, _issueStatusNameMeta));
    } else if (isInserting) {
      context.missing(_issueStatusNameMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
          _nameMeta, name.isAcceptableOrUnknown(data['name']!, _nameMeta));
    }
    if (data.containsKey('last_name')) {
      context.handle(_lastNameMeta,
          lastName.isAcceptableOrUnknown(data['last_name']!, _lastNameMeta));
    }
    if (data.containsKey('sur_name')) {
      context.handle(_surNameMeta,
          surName.isAcceptableOrUnknown(data['sur_name']!, _surNameMeta));
    }
    if (data.containsKey('curp')) {
      context.handle(
          _curpMeta, curp.isAcceptableOrUnknown(data['curp']!, _curpMeta));
    }
    if (data.containsKey('resource_id')) {
      context.handle(
          _resourceIdMeta,
          resourceId.isAcceptableOrUnknown(
              data['resource_id']!, _resourceIdMeta));
    }
    if (data.containsKey('resource_name')) {
      context.handle(
          _resourceNameMeta,
          resourceName.isAcceptableOrUnknown(
              data['resource_name']!, _resourceNameMeta));
    }
    if (data.containsKey('doc_category_id')) {
      context.handle(
          _docCategoryIdMeta,
          docCategoryId.isAcceptableOrUnknown(
              data['doc_category_id']!, _docCategoryIdMeta));
    }
    if (data.containsKey('doc_category_name')) {
      context.handle(
          _docCategoryNameMeta,
          docCategoryName.isAcceptableOrUnknown(
              data['doc_category_name']!, _docCategoryNameMeta));
    }
    if (data.containsKey('local_file_path')) {
      context.handle(
          _localFilePathMeta,
          localFilePath.isAcceptableOrUnknown(
              data['local_file_path']!, _localFilePathMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  IssueDetailRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return IssueDetailRow(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      uuid: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}uuid'])!,
      issueUuid: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}issue_uuid'])!,
      comments: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}comments'])!,
      urgent: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}urgent'])!,
      issueTypeId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}issue_type_id'])!,
      issueTypeName: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}issue_type_name'])!,
      issueCategoryId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}issue_category_id'])!,
      issueCategoryName: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}issue_category_name'])!,
      issueStatusId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}issue_status_id'])!,
      issueStatusName: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}issue_status_name'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name']),
      lastName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}last_name']),
      surName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sur_name']),
      curp: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}curp']),
      resourceId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}resource_id']),
      resourceName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}resource_name']),
      docCategoryId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}doc_category_id']),
      docCategoryName: attachedDatabase.typeMapping.read(
          DriftSqlType.string, data['${effectivePrefix}doc_category_name']),
      localFilePath: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}local_file_path']),
    );
  }

  @override
  $IssueDetailsTable createAlias(String alias) {
    return $IssueDetailsTable(attachedDatabase, alias);
  }
}

class IssueDetailRow extends DataClass implements Insertable<IssueDetailRow> {
  final int id;
  final String uuid;
  final String issueUuid;
  final String comments;
  final bool urgent;
  final int issueTypeId;
  final String issueTypeName;
  final int issueCategoryId;
  final String issueCategoryName;
  final int issueStatusId;
  final String issueStatusName;
  final String? name;
  final String? lastName;
  final String? surName;
  final String? curp;
  final int? resourceId;
  final String? resourceName;
  final int? docCategoryId;
  final String? docCategoryName;
  final String? localFilePath;
  const IssueDetailRow(
      {required this.id,
      required this.uuid,
      required this.issueUuid,
      required this.comments,
      required this.urgent,
      required this.issueTypeId,
      required this.issueTypeName,
      required this.issueCategoryId,
      required this.issueCategoryName,
      required this.issueStatusId,
      required this.issueStatusName,
      this.name,
      this.lastName,
      this.surName,
      this.curp,
      this.resourceId,
      this.resourceName,
      this.docCategoryId,
      this.docCategoryName,
      this.localFilePath});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['uuid'] = Variable<String>(uuid);
    map['issue_uuid'] = Variable<String>(issueUuid);
    map['comments'] = Variable<String>(comments);
    map['urgent'] = Variable<bool>(urgent);
    map['issue_type_id'] = Variable<int>(issueTypeId);
    map['issue_type_name'] = Variable<String>(issueTypeName);
    map['issue_category_id'] = Variable<int>(issueCategoryId);
    map['issue_category_name'] = Variable<String>(issueCategoryName);
    map['issue_status_id'] = Variable<int>(issueStatusId);
    map['issue_status_name'] = Variable<String>(issueStatusName);
    if (!nullToAbsent || name != null) {
      map['name'] = Variable<String>(name);
    }
    if (!nullToAbsent || lastName != null) {
      map['last_name'] = Variable<String>(lastName);
    }
    if (!nullToAbsent || surName != null) {
      map['sur_name'] = Variable<String>(surName);
    }
    if (!nullToAbsent || curp != null) {
      map['curp'] = Variable<String>(curp);
    }
    if (!nullToAbsent || resourceId != null) {
      map['resource_id'] = Variable<int>(resourceId);
    }
    if (!nullToAbsent || resourceName != null) {
      map['resource_name'] = Variable<String>(resourceName);
    }
    if (!nullToAbsent || docCategoryId != null) {
      map['doc_category_id'] = Variable<int>(docCategoryId);
    }
    if (!nullToAbsent || docCategoryName != null) {
      map['doc_category_name'] = Variable<String>(docCategoryName);
    }
    if (!nullToAbsent || localFilePath != null) {
      map['local_file_path'] = Variable<String>(localFilePath);
    }
    return map;
  }

  IssueDetailsCompanion toCompanion(bool nullToAbsent) {
    return IssueDetailsCompanion(
      id: Value(id),
      uuid: Value(uuid),
      issueUuid: Value(issueUuid),
      comments: Value(comments),
      urgent: Value(urgent),
      issueTypeId: Value(issueTypeId),
      issueTypeName: Value(issueTypeName),
      issueCategoryId: Value(issueCategoryId),
      issueCategoryName: Value(issueCategoryName),
      issueStatusId: Value(issueStatusId),
      issueStatusName: Value(issueStatusName),
      name: name == null && nullToAbsent ? const Value.absent() : Value(name),
      lastName: lastName == null && nullToAbsent
          ? const Value.absent()
          : Value(lastName),
      surName: surName == null && nullToAbsent
          ? const Value.absent()
          : Value(surName),
      curp: curp == null && nullToAbsent ? const Value.absent() : Value(curp),
      resourceId: resourceId == null && nullToAbsent
          ? const Value.absent()
          : Value(resourceId),
      resourceName: resourceName == null && nullToAbsent
          ? const Value.absent()
          : Value(resourceName),
      docCategoryId: docCategoryId == null && nullToAbsent
          ? const Value.absent()
          : Value(docCategoryId),
      docCategoryName: docCategoryName == null && nullToAbsent
          ? const Value.absent()
          : Value(docCategoryName),
      localFilePath: localFilePath == null && nullToAbsent
          ? const Value.absent()
          : Value(localFilePath),
    );
  }

  factory IssueDetailRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return IssueDetailRow(
      id: serializer.fromJson<int>(json['id']),
      uuid: serializer.fromJson<String>(json['uuid']),
      issueUuid: serializer.fromJson<String>(json['issueUuid']),
      comments: serializer.fromJson<String>(json['comments']),
      urgent: serializer.fromJson<bool>(json['urgent']),
      issueTypeId: serializer.fromJson<int>(json['issueTypeId']),
      issueTypeName: serializer.fromJson<String>(json['issueTypeName']),
      issueCategoryId: serializer.fromJson<int>(json['issueCategoryId']),
      issueCategoryName: serializer.fromJson<String>(json['issueCategoryName']),
      issueStatusId: serializer.fromJson<int>(json['issueStatusId']),
      issueStatusName: serializer.fromJson<String>(json['issueStatusName']),
      name: serializer.fromJson<String?>(json['name']),
      lastName: serializer.fromJson<String?>(json['lastName']),
      surName: serializer.fromJson<String?>(json['surName']),
      curp: serializer.fromJson<String?>(json['curp']),
      resourceId: serializer.fromJson<int?>(json['resourceId']),
      resourceName: serializer.fromJson<String?>(json['resourceName']),
      docCategoryId: serializer.fromJson<int?>(json['docCategoryId']),
      docCategoryName: serializer.fromJson<String?>(json['docCategoryName']),
      localFilePath: serializer.fromJson<String?>(json['localFilePath']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'uuid': serializer.toJson<String>(uuid),
      'issueUuid': serializer.toJson<String>(issueUuid),
      'comments': serializer.toJson<String>(comments),
      'urgent': serializer.toJson<bool>(urgent),
      'issueTypeId': serializer.toJson<int>(issueTypeId),
      'issueTypeName': serializer.toJson<String>(issueTypeName),
      'issueCategoryId': serializer.toJson<int>(issueCategoryId),
      'issueCategoryName': serializer.toJson<String>(issueCategoryName),
      'issueStatusId': serializer.toJson<int>(issueStatusId),
      'issueStatusName': serializer.toJson<String>(issueStatusName),
      'name': serializer.toJson<String?>(name),
      'lastName': serializer.toJson<String?>(lastName),
      'surName': serializer.toJson<String?>(surName),
      'curp': serializer.toJson<String?>(curp),
      'resourceId': serializer.toJson<int?>(resourceId),
      'resourceName': serializer.toJson<String?>(resourceName),
      'docCategoryId': serializer.toJson<int?>(docCategoryId),
      'docCategoryName': serializer.toJson<String?>(docCategoryName),
      'localFilePath': serializer.toJson<String?>(localFilePath),
    };
  }

  IssueDetailRow copyWith(
          {int? id,
          String? uuid,
          String? issueUuid,
          String? comments,
          bool? urgent,
          int? issueTypeId,
          String? issueTypeName,
          int? issueCategoryId,
          String? issueCategoryName,
          int? issueStatusId,
          String? issueStatusName,
          Value<String?> name = const Value.absent(),
          Value<String?> lastName = const Value.absent(),
          Value<String?> surName = const Value.absent(),
          Value<String?> curp = const Value.absent(),
          Value<int?> resourceId = const Value.absent(),
          Value<String?> resourceName = const Value.absent(),
          Value<int?> docCategoryId = const Value.absent(),
          Value<String?> docCategoryName = const Value.absent(),
          Value<String?> localFilePath = const Value.absent()}) =>
      IssueDetailRow(
        id: id ?? this.id,
        uuid: uuid ?? this.uuid,
        issueUuid: issueUuid ?? this.issueUuid,
        comments: comments ?? this.comments,
        urgent: urgent ?? this.urgent,
        issueTypeId: issueTypeId ?? this.issueTypeId,
        issueTypeName: issueTypeName ?? this.issueTypeName,
        issueCategoryId: issueCategoryId ?? this.issueCategoryId,
        issueCategoryName: issueCategoryName ?? this.issueCategoryName,
        issueStatusId: issueStatusId ?? this.issueStatusId,
        issueStatusName: issueStatusName ?? this.issueStatusName,
        name: name.present ? name.value : this.name,
        lastName: lastName.present ? lastName.value : this.lastName,
        surName: surName.present ? surName.value : this.surName,
        curp: curp.present ? curp.value : this.curp,
        resourceId: resourceId.present ? resourceId.value : this.resourceId,
        resourceName:
            resourceName.present ? resourceName.value : this.resourceName,
        docCategoryId:
            docCategoryId.present ? docCategoryId.value : this.docCategoryId,
        docCategoryName: docCategoryName.present
            ? docCategoryName.value
            : this.docCategoryName,
        localFilePath:
            localFilePath.present ? localFilePath.value : this.localFilePath,
      );
  IssueDetailRow copyWithCompanion(IssueDetailsCompanion data) {
    return IssueDetailRow(
      id: data.id.present ? data.id.value : this.id,
      uuid: data.uuid.present ? data.uuid.value : this.uuid,
      issueUuid: data.issueUuid.present ? data.issueUuid.value : this.issueUuid,
      comments: data.comments.present ? data.comments.value : this.comments,
      urgent: data.urgent.present ? data.urgent.value : this.urgent,
      issueTypeId:
          data.issueTypeId.present ? data.issueTypeId.value : this.issueTypeId,
      issueTypeName: data.issueTypeName.present
          ? data.issueTypeName.value
          : this.issueTypeName,
      issueCategoryId: data.issueCategoryId.present
          ? data.issueCategoryId.value
          : this.issueCategoryId,
      issueCategoryName: data.issueCategoryName.present
          ? data.issueCategoryName.value
          : this.issueCategoryName,
      issueStatusId: data.issueStatusId.present
          ? data.issueStatusId.value
          : this.issueStatusId,
      issueStatusName: data.issueStatusName.present
          ? data.issueStatusName.value
          : this.issueStatusName,
      name: data.name.present ? data.name.value : this.name,
      lastName: data.lastName.present ? data.lastName.value : this.lastName,
      surName: data.surName.present ? data.surName.value : this.surName,
      curp: data.curp.present ? data.curp.value : this.curp,
      resourceId:
          data.resourceId.present ? data.resourceId.value : this.resourceId,
      resourceName: data.resourceName.present
          ? data.resourceName.value
          : this.resourceName,
      docCategoryId: data.docCategoryId.present
          ? data.docCategoryId.value
          : this.docCategoryId,
      docCategoryName: data.docCategoryName.present
          ? data.docCategoryName.value
          : this.docCategoryName,
      localFilePath: data.localFilePath.present
          ? data.localFilePath.value
          : this.localFilePath,
    );
  }

  @override
  String toString() {
    return (StringBuffer('IssueDetailRow(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('issueUuid: $issueUuid, ')
          ..write('comments: $comments, ')
          ..write('urgent: $urgent, ')
          ..write('issueTypeId: $issueTypeId, ')
          ..write('issueTypeName: $issueTypeName, ')
          ..write('issueCategoryId: $issueCategoryId, ')
          ..write('issueCategoryName: $issueCategoryName, ')
          ..write('issueStatusId: $issueStatusId, ')
          ..write('issueStatusName: $issueStatusName, ')
          ..write('name: $name, ')
          ..write('lastName: $lastName, ')
          ..write('surName: $surName, ')
          ..write('curp: $curp, ')
          ..write('resourceId: $resourceId, ')
          ..write('resourceName: $resourceName, ')
          ..write('docCategoryId: $docCategoryId, ')
          ..write('docCategoryName: $docCategoryName, ')
          ..write('localFilePath: $localFilePath')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id,
      uuid,
      issueUuid,
      comments,
      urgent,
      issueTypeId,
      issueTypeName,
      issueCategoryId,
      issueCategoryName,
      issueStatusId,
      issueStatusName,
      name,
      lastName,
      surName,
      curp,
      resourceId,
      resourceName,
      docCategoryId,
      docCategoryName,
      localFilePath);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is IssueDetailRow &&
          other.id == this.id &&
          other.uuid == this.uuid &&
          other.issueUuid == this.issueUuid &&
          other.comments == this.comments &&
          other.urgent == this.urgent &&
          other.issueTypeId == this.issueTypeId &&
          other.issueTypeName == this.issueTypeName &&
          other.issueCategoryId == this.issueCategoryId &&
          other.issueCategoryName == this.issueCategoryName &&
          other.issueStatusId == this.issueStatusId &&
          other.issueStatusName == this.issueStatusName &&
          other.name == this.name &&
          other.lastName == this.lastName &&
          other.surName == this.surName &&
          other.curp == this.curp &&
          other.resourceId == this.resourceId &&
          other.resourceName == this.resourceName &&
          other.docCategoryId == this.docCategoryId &&
          other.docCategoryName == this.docCategoryName &&
          other.localFilePath == this.localFilePath);
}

class IssueDetailsCompanion extends UpdateCompanion<IssueDetailRow> {
  final Value<int> id;
  final Value<String> uuid;
  final Value<String> issueUuid;
  final Value<String> comments;
  final Value<bool> urgent;
  final Value<int> issueTypeId;
  final Value<String> issueTypeName;
  final Value<int> issueCategoryId;
  final Value<String> issueCategoryName;
  final Value<int> issueStatusId;
  final Value<String> issueStatusName;
  final Value<String?> name;
  final Value<String?> lastName;
  final Value<String?> surName;
  final Value<String?> curp;
  final Value<int?> resourceId;
  final Value<String?> resourceName;
  final Value<int?> docCategoryId;
  final Value<String?> docCategoryName;
  final Value<String?> localFilePath;
  const IssueDetailsCompanion({
    this.id = const Value.absent(),
    this.uuid = const Value.absent(),
    this.issueUuid = const Value.absent(),
    this.comments = const Value.absent(),
    this.urgent = const Value.absent(),
    this.issueTypeId = const Value.absent(),
    this.issueTypeName = const Value.absent(),
    this.issueCategoryId = const Value.absent(),
    this.issueCategoryName = const Value.absent(),
    this.issueStatusId = const Value.absent(),
    this.issueStatusName = const Value.absent(),
    this.name = const Value.absent(),
    this.lastName = const Value.absent(),
    this.surName = const Value.absent(),
    this.curp = const Value.absent(),
    this.resourceId = const Value.absent(),
    this.resourceName = const Value.absent(),
    this.docCategoryId = const Value.absent(),
    this.docCategoryName = const Value.absent(),
    this.localFilePath = const Value.absent(),
  });
  IssueDetailsCompanion.insert({
    this.id = const Value.absent(),
    required String uuid,
    required String issueUuid,
    required String comments,
    this.urgent = const Value.absent(),
    required int issueTypeId,
    required String issueTypeName,
    required int issueCategoryId,
    required String issueCategoryName,
    required int issueStatusId,
    required String issueStatusName,
    this.name = const Value.absent(),
    this.lastName = const Value.absent(),
    this.surName = const Value.absent(),
    this.curp = const Value.absent(),
    this.resourceId = const Value.absent(),
    this.resourceName = const Value.absent(),
    this.docCategoryId = const Value.absent(),
    this.docCategoryName = const Value.absent(),
    this.localFilePath = const Value.absent(),
  })  : uuid = Value(uuid),
        issueUuid = Value(issueUuid),
        comments = Value(comments),
        issueTypeId = Value(issueTypeId),
        issueTypeName = Value(issueTypeName),
        issueCategoryId = Value(issueCategoryId),
        issueCategoryName = Value(issueCategoryName),
        issueStatusId = Value(issueStatusId),
        issueStatusName = Value(issueStatusName);
  static Insertable<IssueDetailRow> custom({
    Expression<int>? id,
    Expression<String>? uuid,
    Expression<String>? issueUuid,
    Expression<String>? comments,
    Expression<bool>? urgent,
    Expression<int>? issueTypeId,
    Expression<String>? issueTypeName,
    Expression<int>? issueCategoryId,
    Expression<String>? issueCategoryName,
    Expression<int>? issueStatusId,
    Expression<String>? issueStatusName,
    Expression<String>? name,
    Expression<String>? lastName,
    Expression<String>? surName,
    Expression<String>? curp,
    Expression<int>? resourceId,
    Expression<String>? resourceName,
    Expression<int>? docCategoryId,
    Expression<String>? docCategoryName,
    Expression<String>? localFilePath,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (uuid != null) 'uuid': uuid,
      if (issueUuid != null) 'issue_uuid': issueUuid,
      if (comments != null) 'comments': comments,
      if (urgent != null) 'urgent': urgent,
      if (issueTypeId != null) 'issue_type_id': issueTypeId,
      if (issueTypeName != null) 'issue_type_name': issueTypeName,
      if (issueCategoryId != null) 'issue_category_id': issueCategoryId,
      if (issueCategoryName != null) 'issue_category_name': issueCategoryName,
      if (issueStatusId != null) 'issue_status_id': issueStatusId,
      if (issueStatusName != null) 'issue_status_name': issueStatusName,
      if (name != null) 'name': name,
      if (lastName != null) 'last_name': lastName,
      if (surName != null) 'sur_name': surName,
      if (curp != null) 'curp': curp,
      if (resourceId != null) 'resource_id': resourceId,
      if (resourceName != null) 'resource_name': resourceName,
      if (docCategoryId != null) 'doc_category_id': docCategoryId,
      if (docCategoryName != null) 'doc_category_name': docCategoryName,
      if (localFilePath != null) 'local_file_path': localFilePath,
    });
  }

  IssueDetailsCompanion copyWith(
      {Value<int>? id,
      Value<String>? uuid,
      Value<String>? issueUuid,
      Value<String>? comments,
      Value<bool>? urgent,
      Value<int>? issueTypeId,
      Value<String>? issueTypeName,
      Value<int>? issueCategoryId,
      Value<String>? issueCategoryName,
      Value<int>? issueStatusId,
      Value<String>? issueStatusName,
      Value<String?>? name,
      Value<String?>? lastName,
      Value<String?>? surName,
      Value<String?>? curp,
      Value<int?>? resourceId,
      Value<String?>? resourceName,
      Value<int?>? docCategoryId,
      Value<String?>? docCategoryName,
      Value<String?>? localFilePath}) {
    return IssueDetailsCompanion(
      id: id ?? this.id,
      uuid: uuid ?? this.uuid,
      issueUuid: issueUuid ?? this.issueUuid,
      comments: comments ?? this.comments,
      urgent: urgent ?? this.urgent,
      issueTypeId: issueTypeId ?? this.issueTypeId,
      issueTypeName: issueTypeName ?? this.issueTypeName,
      issueCategoryId: issueCategoryId ?? this.issueCategoryId,
      issueCategoryName: issueCategoryName ?? this.issueCategoryName,
      issueStatusId: issueStatusId ?? this.issueStatusId,
      issueStatusName: issueStatusName ?? this.issueStatusName,
      name: name ?? this.name,
      lastName: lastName ?? this.lastName,
      surName: surName ?? this.surName,
      curp: curp ?? this.curp,
      resourceId: resourceId ?? this.resourceId,
      resourceName: resourceName ?? this.resourceName,
      docCategoryId: docCategoryId ?? this.docCategoryId,
      docCategoryName: docCategoryName ?? this.docCategoryName,
      localFilePath: localFilePath ?? this.localFilePath,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (uuid.present) {
      map['uuid'] = Variable<String>(uuid.value);
    }
    if (issueUuid.present) {
      map['issue_uuid'] = Variable<String>(issueUuid.value);
    }
    if (comments.present) {
      map['comments'] = Variable<String>(comments.value);
    }
    if (urgent.present) {
      map['urgent'] = Variable<bool>(urgent.value);
    }
    if (issueTypeId.present) {
      map['issue_type_id'] = Variable<int>(issueTypeId.value);
    }
    if (issueTypeName.present) {
      map['issue_type_name'] = Variable<String>(issueTypeName.value);
    }
    if (issueCategoryId.present) {
      map['issue_category_id'] = Variable<int>(issueCategoryId.value);
    }
    if (issueCategoryName.present) {
      map['issue_category_name'] = Variable<String>(issueCategoryName.value);
    }
    if (issueStatusId.present) {
      map['issue_status_id'] = Variable<int>(issueStatusId.value);
    }
    if (issueStatusName.present) {
      map['issue_status_name'] = Variable<String>(issueStatusName.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (lastName.present) {
      map['last_name'] = Variable<String>(lastName.value);
    }
    if (surName.present) {
      map['sur_name'] = Variable<String>(surName.value);
    }
    if (curp.present) {
      map['curp'] = Variable<String>(curp.value);
    }
    if (resourceId.present) {
      map['resource_id'] = Variable<int>(resourceId.value);
    }
    if (resourceName.present) {
      map['resource_name'] = Variable<String>(resourceName.value);
    }
    if (docCategoryId.present) {
      map['doc_category_id'] = Variable<int>(docCategoryId.value);
    }
    if (docCategoryName.present) {
      map['doc_category_name'] = Variable<String>(docCategoryName.value);
    }
    if (localFilePath.present) {
      map['local_file_path'] = Variable<String>(localFilePath.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('IssueDetailsCompanion(')
          ..write('id: $id, ')
          ..write('uuid: $uuid, ')
          ..write('issueUuid: $issueUuid, ')
          ..write('comments: $comments, ')
          ..write('urgent: $urgent, ')
          ..write('issueTypeId: $issueTypeId, ')
          ..write('issueTypeName: $issueTypeName, ')
          ..write('issueCategoryId: $issueCategoryId, ')
          ..write('issueCategoryName: $issueCategoryName, ')
          ..write('issueStatusId: $issueStatusId, ')
          ..write('issueStatusName: $issueStatusName, ')
          ..write('name: $name, ')
          ..write('lastName: $lastName, ')
          ..write('surName: $surName, ')
          ..write('curp: $curp, ')
          ..write('resourceId: $resourceId, ')
          ..write('resourceName: $resourceName, ')
          ..write('docCategoryId: $docCategoryId, ')
          ..write('docCategoryName: $docCategoryName, ')
          ..write('localFilePath: $localFilePath')
          ..write(')'))
        .toString();
  }
}

class $CaptureTargetsTable extends CaptureTargets
    with TableInfo<$CaptureTargetsTable, CaptureTargetRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $CaptureTargetsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _positionIdMeta =
      const VerificationMeta('positionId');
  @override
  late final GeneratedColumn<int> positionId = GeneratedColumn<int>(
      'position_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _levelMeta = const VerificationMeta('level');
  @override
  late final GeneratedColumn<String> level = GeneratedColumn<String>(
      'level', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _labelMeta = const VerificationMeta('label');
  @override
  late final GeneratedColumn<String> label = GeneratedColumn<String>(
      'label', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _holderNameMeta =
      const VerificationMeta('holderName');
  @override
  late final GeneratedColumn<String> holderName = GeneratedColumn<String>(
      'holder_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _sectionGeoIdMeta =
      const VerificationMeta('sectionGeoId');
  @override
  late final GeneratedColumn<int> sectionGeoId = GeneratedColumn<int>(
      'section_geo_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _municipalityGeoIdMeta =
      const VerificationMeta('municipalityGeoId');
  @override
  late final GeneratedColumn<int> municipalityGeoId = GeneratedColumn<int>(
      'municipality_geo_id', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _ownMeta = const VerificationMeta('own');
  @override
  late final GeneratedColumn<bool> own = GeneratedColumn<bool>(
      'own', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("own" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _updatedMeta =
      const VerificationMeta('updated');
  @override
  late final GeneratedColumn<DateTime> updated = GeneratedColumn<DateTime>(
      'updated', aliasedName, false,
      type: DriftSqlType.dateTime, requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns => [
        positionId,
        level,
        label,
        holderName,
        sectionGeoId,
        municipalityGeoId,
        own,
        updated
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'capture_targets';
  @override
  VerificationContext validateIntegrity(Insertable<CaptureTargetRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('position_id')) {
      context.handle(
          _positionIdMeta,
          positionId.isAcceptableOrUnknown(
              data['position_id']!, _positionIdMeta));
    }
    if (data.containsKey('level')) {
      context.handle(
          _levelMeta, level.isAcceptableOrUnknown(data['level']!, _levelMeta));
    } else if (isInserting) {
      context.missing(_levelMeta);
    }
    if (data.containsKey('label')) {
      context.handle(
          _labelMeta, label.isAcceptableOrUnknown(data['label']!, _labelMeta));
    } else if (isInserting) {
      context.missing(_labelMeta);
    }
    if (data.containsKey('holder_name')) {
      context.handle(
          _holderNameMeta,
          holderName.isAcceptableOrUnknown(
              data['holder_name']!, _holderNameMeta));
    }
    if (data.containsKey('section_geo_id')) {
      context.handle(
          _sectionGeoIdMeta,
          sectionGeoId.isAcceptableOrUnknown(
              data['section_geo_id']!, _sectionGeoIdMeta));
    }
    if (data.containsKey('municipality_geo_id')) {
      context.handle(
          _municipalityGeoIdMeta,
          municipalityGeoId.isAcceptableOrUnknown(
              data['municipality_geo_id']!, _municipalityGeoIdMeta));
    }
    if (data.containsKey('own')) {
      context.handle(
          _ownMeta, own.isAcceptableOrUnknown(data['own']!, _ownMeta));
    }
    if (data.containsKey('updated')) {
      context.handle(_updatedMeta,
          updated.isAcceptableOrUnknown(data['updated']!, _updatedMeta));
    } else if (isInserting) {
      context.missing(_updatedMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {positionId};
  @override
  CaptureTargetRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return CaptureTargetRow(
      positionId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}position_id'])!,
      level: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}level'])!,
      label: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}label'])!,
      holderName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}holder_name']),
      sectionGeoId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}section_geo_id']),
      municipalityGeoId: attachedDatabase.typeMapping.read(
          DriftSqlType.int, data['${effectivePrefix}municipality_geo_id']),
      own: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}own'])!,
      updated: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated'])!,
    );
  }

  @override
  $CaptureTargetsTable createAlias(String alias) {
    return $CaptureTargetsTable(attachedDatabase, alias);
  }
}

class CaptureTargetRow extends DataClass
    implements Insertable<CaptureTargetRow> {
  final int positionId;
  final String level;
  final String label;
  final String? holderName;
  final int? sectionGeoId;
  final int? municipalityGeoId;
  final bool own;
  final DateTime updated;
  const CaptureTargetRow(
      {required this.positionId,
      required this.level,
      required this.label,
      this.holderName,
      this.sectionGeoId,
      this.municipalityGeoId,
      required this.own,
      required this.updated});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['position_id'] = Variable<int>(positionId);
    map['level'] = Variable<String>(level);
    map['label'] = Variable<String>(label);
    if (!nullToAbsent || holderName != null) {
      map['holder_name'] = Variable<String>(holderName);
    }
    if (!nullToAbsent || sectionGeoId != null) {
      map['section_geo_id'] = Variable<int>(sectionGeoId);
    }
    if (!nullToAbsent || municipalityGeoId != null) {
      map['municipality_geo_id'] = Variable<int>(municipalityGeoId);
    }
    map['own'] = Variable<bool>(own);
    map['updated'] = Variable<DateTime>(updated);
    return map;
  }

  CaptureTargetsCompanion toCompanion(bool nullToAbsent) {
    return CaptureTargetsCompanion(
      positionId: Value(positionId),
      level: Value(level),
      label: Value(label),
      holderName: holderName == null && nullToAbsent
          ? const Value.absent()
          : Value(holderName),
      sectionGeoId: sectionGeoId == null && nullToAbsent
          ? const Value.absent()
          : Value(sectionGeoId),
      municipalityGeoId: municipalityGeoId == null && nullToAbsent
          ? const Value.absent()
          : Value(municipalityGeoId),
      own: Value(own),
      updated: Value(updated),
    );
  }

  factory CaptureTargetRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return CaptureTargetRow(
      positionId: serializer.fromJson<int>(json['positionId']),
      level: serializer.fromJson<String>(json['level']),
      label: serializer.fromJson<String>(json['label']),
      holderName: serializer.fromJson<String?>(json['holderName']),
      sectionGeoId: serializer.fromJson<int?>(json['sectionGeoId']),
      municipalityGeoId: serializer.fromJson<int?>(json['municipalityGeoId']),
      own: serializer.fromJson<bool>(json['own']),
      updated: serializer.fromJson<DateTime>(json['updated']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'positionId': serializer.toJson<int>(positionId),
      'level': serializer.toJson<String>(level),
      'label': serializer.toJson<String>(label),
      'holderName': serializer.toJson<String?>(holderName),
      'sectionGeoId': serializer.toJson<int?>(sectionGeoId),
      'municipalityGeoId': serializer.toJson<int?>(municipalityGeoId),
      'own': serializer.toJson<bool>(own),
      'updated': serializer.toJson<DateTime>(updated),
    };
  }

  CaptureTargetRow copyWith(
          {int? positionId,
          String? level,
          String? label,
          Value<String?> holderName = const Value.absent(),
          Value<int?> sectionGeoId = const Value.absent(),
          Value<int?> municipalityGeoId = const Value.absent(),
          bool? own,
          DateTime? updated}) =>
      CaptureTargetRow(
        positionId: positionId ?? this.positionId,
        level: level ?? this.level,
        label: label ?? this.label,
        holderName: holderName.present ? holderName.value : this.holderName,
        sectionGeoId:
            sectionGeoId.present ? sectionGeoId.value : this.sectionGeoId,
        municipalityGeoId: municipalityGeoId.present
            ? municipalityGeoId.value
            : this.municipalityGeoId,
        own: own ?? this.own,
        updated: updated ?? this.updated,
      );
  CaptureTargetRow copyWithCompanion(CaptureTargetsCompanion data) {
    return CaptureTargetRow(
      positionId:
          data.positionId.present ? data.positionId.value : this.positionId,
      level: data.level.present ? data.level.value : this.level,
      label: data.label.present ? data.label.value : this.label,
      holderName:
          data.holderName.present ? data.holderName.value : this.holderName,
      sectionGeoId: data.sectionGeoId.present
          ? data.sectionGeoId.value
          : this.sectionGeoId,
      municipalityGeoId: data.municipalityGeoId.present
          ? data.municipalityGeoId.value
          : this.municipalityGeoId,
      own: data.own.present ? data.own.value : this.own,
      updated: data.updated.present ? data.updated.value : this.updated,
    );
  }

  @override
  String toString() {
    return (StringBuffer('CaptureTargetRow(')
          ..write('positionId: $positionId, ')
          ..write('level: $level, ')
          ..write('label: $label, ')
          ..write('holderName: $holderName, ')
          ..write('sectionGeoId: $sectionGeoId, ')
          ..write('municipalityGeoId: $municipalityGeoId, ')
          ..write('own: $own, ')
          ..write('updated: $updated')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(positionId, level, label, holderName,
      sectionGeoId, municipalityGeoId, own, updated);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is CaptureTargetRow &&
          other.positionId == this.positionId &&
          other.level == this.level &&
          other.label == this.label &&
          other.holderName == this.holderName &&
          other.sectionGeoId == this.sectionGeoId &&
          other.municipalityGeoId == this.municipalityGeoId &&
          other.own == this.own &&
          other.updated == this.updated);
}

class CaptureTargetsCompanion extends UpdateCompanion<CaptureTargetRow> {
  final Value<int> positionId;
  final Value<String> level;
  final Value<String> label;
  final Value<String?> holderName;
  final Value<int?> sectionGeoId;
  final Value<int?> municipalityGeoId;
  final Value<bool> own;
  final Value<DateTime> updated;
  const CaptureTargetsCompanion({
    this.positionId = const Value.absent(),
    this.level = const Value.absent(),
    this.label = const Value.absent(),
    this.holderName = const Value.absent(),
    this.sectionGeoId = const Value.absent(),
    this.municipalityGeoId = const Value.absent(),
    this.own = const Value.absent(),
    this.updated = const Value.absent(),
  });
  CaptureTargetsCompanion.insert({
    this.positionId = const Value.absent(),
    required String level,
    required String label,
    this.holderName = const Value.absent(),
    this.sectionGeoId = const Value.absent(),
    this.municipalityGeoId = const Value.absent(),
    this.own = const Value.absent(),
    required DateTime updated,
  })  : level = Value(level),
        label = Value(label),
        updated = Value(updated);
  static Insertable<CaptureTargetRow> custom({
    Expression<int>? positionId,
    Expression<String>? level,
    Expression<String>? label,
    Expression<String>? holderName,
    Expression<int>? sectionGeoId,
    Expression<int>? municipalityGeoId,
    Expression<bool>? own,
    Expression<DateTime>? updated,
  }) {
    return RawValuesInsertable({
      if (positionId != null) 'position_id': positionId,
      if (level != null) 'level': level,
      if (label != null) 'label': label,
      if (holderName != null) 'holder_name': holderName,
      if (sectionGeoId != null) 'section_geo_id': sectionGeoId,
      if (municipalityGeoId != null) 'municipality_geo_id': municipalityGeoId,
      if (own != null) 'own': own,
      if (updated != null) 'updated': updated,
    });
  }

  CaptureTargetsCompanion copyWith(
      {Value<int>? positionId,
      Value<String>? level,
      Value<String>? label,
      Value<String?>? holderName,
      Value<int?>? sectionGeoId,
      Value<int?>? municipalityGeoId,
      Value<bool>? own,
      Value<DateTime>? updated}) {
    return CaptureTargetsCompanion(
      positionId: positionId ?? this.positionId,
      level: level ?? this.level,
      label: label ?? this.label,
      holderName: holderName ?? this.holderName,
      sectionGeoId: sectionGeoId ?? this.sectionGeoId,
      municipalityGeoId: municipalityGeoId ?? this.municipalityGeoId,
      own: own ?? this.own,
      updated: updated ?? this.updated,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (positionId.present) {
      map['position_id'] = Variable<int>(positionId.value);
    }
    if (level.present) {
      map['level'] = Variable<String>(level.value);
    }
    if (label.present) {
      map['label'] = Variable<String>(label.value);
    }
    if (holderName.present) {
      map['holder_name'] = Variable<String>(holderName.value);
    }
    if (sectionGeoId.present) {
      map['section_geo_id'] = Variable<int>(sectionGeoId.value);
    }
    if (municipalityGeoId.present) {
      map['municipality_geo_id'] = Variable<int>(municipalityGeoId.value);
    }
    if (own.present) {
      map['own'] = Variable<bool>(own.value);
    }
    if (updated.present) {
      map['updated'] = Variable<DateTime>(updated.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('CaptureTargetsCompanion(')
          ..write('positionId: $positionId, ')
          ..write('level: $level, ')
          ..write('label: $label, ')
          ..write('holderName: $holderName, ')
          ..write('sectionGeoId: $sectionGeoId, ')
          ..write('municipalityGeoId: $municipalityGeoId, ')
          ..write('own: $own, ')
          ..write('updated: $updated')
          ..write(')'))
        .toString();
  }
}

class $SectionGeometriesTable extends SectionGeometries
    with TableInfo<$SectionGeometriesTable, SectionGeometryRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SectionGeometriesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _sectionGeoIdMeta =
      const VerificationMeta('sectionGeoId');
  @override
  late final GeneratedColumn<int> sectionGeoId = GeneratedColumn<int>(
      'section_geo_id', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _geoJsonMeta =
      const VerificationMeta('geoJson');
  @override
  late final GeneratedColumn<String> geoJson = GeneratedColumn<String>(
      'geo_json', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _updatedMeta =
      const VerificationMeta('updated');
  @override
  late final GeneratedColumn<DateTime> updated = GeneratedColumn<DateTime>(
      'updated', aliasedName, false,
      type: DriftSqlType.dateTime, requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns => [sectionGeoId, geoJson, updated];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'section_geometries';
  @override
  VerificationContext validateIntegrity(Insertable<SectionGeometryRow> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('section_geo_id')) {
      context.handle(
          _sectionGeoIdMeta,
          sectionGeoId.isAcceptableOrUnknown(
              data['section_geo_id']!, _sectionGeoIdMeta));
    }
    if (data.containsKey('geo_json')) {
      context.handle(_geoJsonMeta,
          geoJson.isAcceptableOrUnknown(data['geo_json']!, _geoJsonMeta));
    } else if (isInserting) {
      context.missing(_geoJsonMeta);
    }
    if (data.containsKey('updated')) {
      context.handle(_updatedMeta,
          updated.isAcceptableOrUnknown(data['updated']!, _updatedMeta));
    } else if (isInserting) {
      context.missing(_updatedMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {sectionGeoId};
  @override
  SectionGeometryRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return SectionGeometryRow(
      sectionGeoId: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}section_geo_id'])!,
      geoJson: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}geo_json'])!,
      updated: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated'])!,
    );
  }

  @override
  $SectionGeometriesTable createAlias(String alias) {
    return $SectionGeometriesTable(attachedDatabase, alias);
  }
}

class SectionGeometryRow extends DataClass
    implements Insertable<SectionGeometryRow> {
  final int sectionGeoId;
  final String geoJson;
  final DateTime updated;
  const SectionGeometryRow(
      {required this.sectionGeoId,
      required this.geoJson,
      required this.updated});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['section_geo_id'] = Variable<int>(sectionGeoId);
    map['geo_json'] = Variable<String>(geoJson);
    map['updated'] = Variable<DateTime>(updated);
    return map;
  }

  SectionGeometriesCompanion toCompanion(bool nullToAbsent) {
    return SectionGeometriesCompanion(
      sectionGeoId: Value(sectionGeoId),
      geoJson: Value(geoJson),
      updated: Value(updated),
    );
  }

  factory SectionGeometryRow.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return SectionGeometryRow(
      sectionGeoId: serializer.fromJson<int>(json['sectionGeoId']),
      geoJson: serializer.fromJson<String>(json['geoJson']),
      updated: serializer.fromJson<DateTime>(json['updated']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'sectionGeoId': serializer.toJson<int>(sectionGeoId),
      'geoJson': serializer.toJson<String>(geoJson),
      'updated': serializer.toJson<DateTime>(updated),
    };
  }

  SectionGeometryRow copyWith(
          {int? sectionGeoId, String? geoJson, DateTime? updated}) =>
      SectionGeometryRow(
        sectionGeoId: sectionGeoId ?? this.sectionGeoId,
        geoJson: geoJson ?? this.geoJson,
        updated: updated ?? this.updated,
      );
  SectionGeometryRow copyWithCompanion(SectionGeometriesCompanion data) {
    return SectionGeometryRow(
      sectionGeoId: data.sectionGeoId.present
          ? data.sectionGeoId.value
          : this.sectionGeoId,
      geoJson: data.geoJson.present ? data.geoJson.value : this.geoJson,
      updated: data.updated.present ? data.updated.value : this.updated,
    );
  }

  @override
  String toString() {
    return (StringBuffer('SectionGeometryRow(')
          ..write('sectionGeoId: $sectionGeoId, ')
          ..write('geoJson: $geoJson, ')
          ..write('updated: $updated')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(sectionGeoId, geoJson, updated);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SectionGeometryRow &&
          other.sectionGeoId == this.sectionGeoId &&
          other.geoJson == this.geoJson &&
          other.updated == this.updated);
}

class SectionGeometriesCompanion extends UpdateCompanion<SectionGeometryRow> {
  final Value<int> sectionGeoId;
  final Value<String> geoJson;
  final Value<DateTime> updated;
  const SectionGeometriesCompanion({
    this.sectionGeoId = const Value.absent(),
    this.geoJson = const Value.absent(),
    this.updated = const Value.absent(),
  });
  SectionGeometriesCompanion.insert({
    this.sectionGeoId = const Value.absent(),
    required String geoJson,
    required DateTime updated,
  })  : geoJson = Value(geoJson),
        updated = Value(updated);
  static Insertable<SectionGeometryRow> custom({
    Expression<int>? sectionGeoId,
    Expression<String>? geoJson,
    Expression<DateTime>? updated,
  }) {
    return RawValuesInsertable({
      if (sectionGeoId != null) 'section_geo_id': sectionGeoId,
      if (geoJson != null) 'geo_json': geoJson,
      if (updated != null) 'updated': updated,
    });
  }

  SectionGeometriesCompanion copyWith(
      {Value<int>? sectionGeoId,
      Value<String>? geoJson,
      Value<DateTime>? updated}) {
    return SectionGeometriesCompanion(
      sectionGeoId: sectionGeoId ?? this.sectionGeoId,
      geoJson: geoJson ?? this.geoJson,
      updated: updated ?? this.updated,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (sectionGeoId.present) {
      map['section_geo_id'] = Variable<int>(sectionGeoId.value);
    }
    if (geoJson.present) {
      map['geo_json'] = Variable<String>(geoJson.value);
    }
    if (updated.present) {
      map['updated'] = Variable<DateTime>(updated.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SectionGeometriesCompanion(')
          ..write('sectionGeoId: $sectionGeoId, ')
          ..write('geoJson: $geoJson, ')
          ..write('updated: $updated')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $CanvasWallsTable canvasWalls = $CanvasWallsTable(this);
  late final $VisitPeoplesTable visitPeoples = $VisitPeoplesTable(this);
  late final $FollowersTable followers = $FollowersTable(this);
  late final $UserSettingsTableTable userSettingsTable =
      $UserSettingsTableTable(this);
  late final $IssuesTable issues = $IssuesTable(this);
  late final $IssueDetailsTable issueDetails = $IssueDetailsTable(this);
  late final $CaptureTargetsTable captureTargets = $CaptureTargetsTable(this);
  late final $SectionGeometriesTable sectionGeometries =
      $SectionGeometriesTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
        canvasWalls,
        visitPeoples,
        followers,
        userSettingsTable,
        issues,
        issueDetails,
        captureTargets,
        sectionGeometries
      ];
}

typedef $$CanvasWallsTableCreateCompanionBuilder = CanvasWallsCompanion
    Function({
  Value<int> id,
  required String uuid,
  required String deviceId,
  required String latitude,
  required String longitude,
  required String projectId,
  required String categoryId,
  required String date,
  Value<Uint8List?> file,
  Value<bool> isInSync,
});
typedef $$CanvasWallsTableUpdateCompanionBuilder = CanvasWallsCompanion
    Function({
  Value<int> id,
  Value<String> uuid,
  Value<String> deviceId,
  Value<String> latitude,
  Value<String> longitude,
  Value<String> projectId,
  Value<String> categoryId,
  Value<String> date,
  Value<Uint8List?> file,
  Value<bool> isInSync,
});

class $$CanvasWallsTableFilterComposer
    extends Composer<_$AppDatabase, $CanvasWallsTable> {
  $$CanvasWallsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get deviceId => $composableBuilder(
      column: $table.deviceId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get latitude => $composableBuilder(
      column: $table.latitude, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get longitude => $composableBuilder(
      column: $table.longitude, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get projectId => $composableBuilder(
      column: $table.projectId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get categoryId => $composableBuilder(
      column: $table.categoryId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get date => $composableBuilder(
      column: $table.date, builder: (column) => ColumnFilters(column));

  ColumnFilters<Uint8List> get file => $composableBuilder(
      column: $table.file, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isInSync => $composableBuilder(
      column: $table.isInSync, builder: (column) => ColumnFilters(column));
}

class $$CanvasWallsTableOrderingComposer
    extends Composer<_$AppDatabase, $CanvasWallsTable> {
  $$CanvasWallsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get deviceId => $composableBuilder(
      column: $table.deviceId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get latitude => $composableBuilder(
      column: $table.latitude, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get longitude => $composableBuilder(
      column: $table.longitude, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get projectId => $composableBuilder(
      column: $table.projectId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get categoryId => $composableBuilder(
      column: $table.categoryId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get date => $composableBuilder(
      column: $table.date, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<Uint8List> get file => $composableBuilder(
      column: $table.file, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isInSync => $composableBuilder(
      column: $table.isInSync, builder: (column) => ColumnOrderings(column));
}

class $$CanvasWallsTableAnnotationComposer
    extends Composer<_$AppDatabase, $CanvasWallsTable> {
  $$CanvasWallsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get uuid =>
      $composableBuilder(column: $table.uuid, builder: (column) => column);

  GeneratedColumn<String> get deviceId =>
      $composableBuilder(column: $table.deviceId, builder: (column) => column);

  GeneratedColumn<String> get latitude =>
      $composableBuilder(column: $table.latitude, builder: (column) => column);

  GeneratedColumn<String> get longitude =>
      $composableBuilder(column: $table.longitude, builder: (column) => column);

  GeneratedColumn<String> get projectId =>
      $composableBuilder(column: $table.projectId, builder: (column) => column);

  GeneratedColumn<String> get categoryId => $composableBuilder(
      column: $table.categoryId, builder: (column) => column);

  GeneratedColumn<String> get date =>
      $composableBuilder(column: $table.date, builder: (column) => column);

  GeneratedColumn<Uint8List> get file =>
      $composableBuilder(column: $table.file, builder: (column) => column);

  GeneratedColumn<bool> get isInSync =>
      $composableBuilder(column: $table.isInSync, builder: (column) => column);
}

class $$CanvasWallsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $CanvasWallsTable,
    CanvasWallRow,
    $$CanvasWallsTableFilterComposer,
    $$CanvasWallsTableOrderingComposer,
    $$CanvasWallsTableAnnotationComposer,
    $$CanvasWallsTableCreateCompanionBuilder,
    $$CanvasWallsTableUpdateCompanionBuilder,
    (
      CanvasWallRow,
      BaseReferences<_$AppDatabase, $CanvasWallsTable, CanvasWallRow>
    ),
    CanvasWallRow,
    PrefetchHooks Function()> {
  $$CanvasWallsTableTableManager(_$AppDatabase db, $CanvasWallsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$CanvasWallsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$CanvasWallsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$CanvasWallsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<String> uuid = const Value.absent(),
            Value<String> deviceId = const Value.absent(),
            Value<String> latitude = const Value.absent(),
            Value<String> longitude = const Value.absent(),
            Value<String> projectId = const Value.absent(),
            Value<String> categoryId = const Value.absent(),
            Value<String> date = const Value.absent(),
            Value<Uint8List?> file = const Value.absent(),
            Value<bool> isInSync = const Value.absent(),
          }) =>
              CanvasWallsCompanion(
            id: id,
            uuid: uuid,
            deviceId: deviceId,
            latitude: latitude,
            longitude: longitude,
            projectId: projectId,
            categoryId: categoryId,
            date: date,
            file: file,
            isInSync: isInSync,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            required String uuid,
            required String deviceId,
            required String latitude,
            required String longitude,
            required String projectId,
            required String categoryId,
            required String date,
            Value<Uint8List?> file = const Value.absent(),
            Value<bool> isInSync = const Value.absent(),
          }) =>
              CanvasWallsCompanion.insert(
            id: id,
            uuid: uuid,
            deviceId: deviceId,
            latitude: latitude,
            longitude: longitude,
            projectId: projectId,
            categoryId: categoryId,
            date: date,
            file: file,
            isInSync: isInSync,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$CanvasWallsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $CanvasWallsTable,
    CanvasWallRow,
    $$CanvasWallsTableFilterComposer,
    $$CanvasWallsTableOrderingComposer,
    $$CanvasWallsTableAnnotationComposer,
    $$CanvasWallsTableCreateCompanionBuilder,
    $$CanvasWallsTableUpdateCompanionBuilder,
    (
      CanvasWallRow,
      BaseReferences<_$AppDatabase, $CanvasWallsTable, CanvasWallRow>
    ),
    CanvasWallRow,
    PrefetchHooks Function()>;
typedef $$VisitPeoplesTableCreateCompanionBuilder = VisitPeoplesCompanion
    Function({
  Value<int> id,
  required String uuid,
  required String deviceId,
  Value<bool> openTheDoor,
  required String street,
  required String externallNumber,
  required String longitude,
  required String latitude,
  required String visitNumber,
  Value<bool> syncVisit,
  Value<String?> age,
  Value<String?> names,
  Value<String?> lastName,
  Value<String?> surName,
  Value<String?> phone,
  Value<bool?> acceptInformation,
  Value<String?> sex,
  Value<String?> hasSympathy,
  Value<DateTime?> created,
});
typedef $$VisitPeoplesTableUpdateCompanionBuilder = VisitPeoplesCompanion
    Function({
  Value<int> id,
  Value<String> uuid,
  Value<String> deviceId,
  Value<bool> openTheDoor,
  Value<String> street,
  Value<String> externallNumber,
  Value<String> longitude,
  Value<String> latitude,
  Value<String> visitNumber,
  Value<bool> syncVisit,
  Value<String?> age,
  Value<String?> names,
  Value<String?> lastName,
  Value<String?> surName,
  Value<String?> phone,
  Value<bool?> acceptInformation,
  Value<String?> sex,
  Value<String?> hasSympathy,
  Value<DateTime?> created,
});

class $$VisitPeoplesTableFilterComposer
    extends Composer<_$AppDatabase, $VisitPeoplesTable> {
  $$VisitPeoplesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get deviceId => $composableBuilder(
      column: $table.deviceId, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get openTheDoor => $composableBuilder(
      column: $table.openTheDoor, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get street => $composableBuilder(
      column: $table.street, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get externallNumber => $composableBuilder(
      column: $table.externallNumber,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get longitude => $composableBuilder(
      column: $table.longitude, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get latitude => $composableBuilder(
      column: $table.latitude, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get visitNumber => $composableBuilder(
      column: $table.visitNumber, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get syncVisit => $composableBuilder(
      column: $table.syncVisit, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get age => $composableBuilder(
      column: $table.age, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get names => $composableBuilder(
      column: $table.names, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get lastName => $composableBuilder(
      column: $table.lastName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get surName => $composableBuilder(
      column: $table.surName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get phone => $composableBuilder(
      column: $table.phone, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get acceptInformation => $composableBuilder(
      column: $table.acceptInformation,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get sex => $composableBuilder(
      column: $table.sex, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get hasSympathy => $composableBuilder(
      column: $table.hasSympathy, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get created => $composableBuilder(
      column: $table.created, builder: (column) => ColumnFilters(column));
}

class $$VisitPeoplesTableOrderingComposer
    extends Composer<_$AppDatabase, $VisitPeoplesTable> {
  $$VisitPeoplesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get deviceId => $composableBuilder(
      column: $table.deviceId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get openTheDoor => $composableBuilder(
      column: $table.openTheDoor, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get street => $composableBuilder(
      column: $table.street, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get externallNumber => $composableBuilder(
      column: $table.externallNumber,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get longitude => $composableBuilder(
      column: $table.longitude, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get latitude => $composableBuilder(
      column: $table.latitude, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get visitNumber => $composableBuilder(
      column: $table.visitNumber, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get syncVisit => $composableBuilder(
      column: $table.syncVisit, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get age => $composableBuilder(
      column: $table.age, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get names => $composableBuilder(
      column: $table.names, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get lastName => $composableBuilder(
      column: $table.lastName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get surName => $composableBuilder(
      column: $table.surName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get phone => $composableBuilder(
      column: $table.phone, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get acceptInformation => $composableBuilder(
      column: $table.acceptInformation,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get sex => $composableBuilder(
      column: $table.sex, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get hasSympathy => $composableBuilder(
      column: $table.hasSympathy, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get created => $composableBuilder(
      column: $table.created, builder: (column) => ColumnOrderings(column));
}

class $$VisitPeoplesTableAnnotationComposer
    extends Composer<_$AppDatabase, $VisitPeoplesTable> {
  $$VisitPeoplesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get uuid =>
      $composableBuilder(column: $table.uuid, builder: (column) => column);

  GeneratedColumn<String> get deviceId =>
      $composableBuilder(column: $table.deviceId, builder: (column) => column);

  GeneratedColumn<bool> get openTheDoor => $composableBuilder(
      column: $table.openTheDoor, builder: (column) => column);

  GeneratedColumn<String> get street =>
      $composableBuilder(column: $table.street, builder: (column) => column);

  GeneratedColumn<String> get externallNumber => $composableBuilder(
      column: $table.externallNumber, builder: (column) => column);

  GeneratedColumn<String> get longitude =>
      $composableBuilder(column: $table.longitude, builder: (column) => column);

  GeneratedColumn<String> get latitude =>
      $composableBuilder(column: $table.latitude, builder: (column) => column);

  GeneratedColumn<String> get visitNumber => $composableBuilder(
      column: $table.visitNumber, builder: (column) => column);

  GeneratedColumn<bool> get syncVisit =>
      $composableBuilder(column: $table.syncVisit, builder: (column) => column);

  GeneratedColumn<String> get age =>
      $composableBuilder(column: $table.age, builder: (column) => column);

  GeneratedColumn<String> get names =>
      $composableBuilder(column: $table.names, builder: (column) => column);

  GeneratedColumn<String> get lastName =>
      $composableBuilder(column: $table.lastName, builder: (column) => column);

  GeneratedColumn<String> get surName =>
      $composableBuilder(column: $table.surName, builder: (column) => column);

  GeneratedColumn<String> get phone =>
      $composableBuilder(column: $table.phone, builder: (column) => column);

  GeneratedColumn<bool> get acceptInformation => $composableBuilder(
      column: $table.acceptInformation, builder: (column) => column);

  GeneratedColumn<String> get sex =>
      $composableBuilder(column: $table.sex, builder: (column) => column);

  GeneratedColumn<String> get hasSympathy => $composableBuilder(
      column: $table.hasSympathy, builder: (column) => column);

  GeneratedColumn<DateTime> get created =>
      $composableBuilder(column: $table.created, builder: (column) => column);
}

class $$VisitPeoplesTableTableManager extends RootTableManager<
    _$AppDatabase,
    $VisitPeoplesTable,
    VisitPeopleRow,
    $$VisitPeoplesTableFilterComposer,
    $$VisitPeoplesTableOrderingComposer,
    $$VisitPeoplesTableAnnotationComposer,
    $$VisitPeoplesTableCreateCompanionBuilder,
    $$VisitPeoplesTableUpdateCompanionBuilder,
    (
      VisitPeopleRow,
      BaseReferences<_$AppDatabase, $VisitPeoplesTable, VisitPeopleRow>
    ),
    VisitPeopleRow,
    PrefetchHooks Function()> {
  $$VisitPeoplesTableTableManager(_$AppDatabase db, $VisitPeoplesTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$VisitPeoplesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$VisitPeoplesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$VisitPeoplesTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<String> uuid = const Value.absent(),
            Value<String> deviceId = const Value.absent(),
            Value<bool> openTheDoor = const Value.absent(),
            Value<String> street = const Value.absent(),
            Value<String> externallNumber = const Value.absent(),
            Value<String> longitude = const Value.absent(),
            Value<String> latitude = const Value.absent(),
            Value<String> visitNumber = const Value.absent(),
            Value<bool> syncVisit = const Value.absent(),
            Value<String?> age = const Value.absent(),
            Value<String?> names = const Value.absent(),
            Value<String?> lastName = const Value.absent(),
            Value<String?> surName = const Value.absent(),
            Value<String?> phone = const Value.absent(),
            Value<bool?> acceptInformation = const Value.absent(),
            Value<String?> sex = const Value.absent(),
            Value<String?> hasSympathy = const Value.absent(),
            Value<DateTime?> created = const Value.absent(),
          }) =>
              VisitPeoplesCompanion(
            id: id,
            uuid: uuid,
            deviceId: deviceId,
            openTheDoor: openTheDoor,
            street: street,
            externallNumber: externallNumber,
            longitude: longitude,
            latitude: latitude,
            visitNumber: visitNumber,
            syncVisit: syncVisit,
            age: age,
            names: names,
            lastName: lastName,
            surName: surName,
            phone: phone,
            acceptInformation: acceptInformation,
            sex: sex,
            hasSympathy: hasSympathy,
            created: created,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            required String uuid,
            required String deviceId,
            Value<bool> openTheDoor = const Value.absent(),
            required String street,
            required String externallNumber,
            required String longitude,
            required String latitude,
            required String visitNumber,
            Value<bool> syncVisit = const Value.absent(),
            Value<String?> age = const Value.absent(),
            Value<String?> names = const Value.absent(),
            Value<String?> lastName = const Value.absent(),
            Value<String?> surName = const Value.absent(),
            Value<String?> phone = const Value.absent(),
            Value<bool?> acceptInformation = const Value.absent(),
            Value<String?> sex = const Value.absent(),
            Value<String?> hasSympathy = const Value.absent(),
            Value<DateTime?> created = const Value.absent(),
          }) =>
              VisitPeoplesCompanion.insert(
            id: id,
            uuid: uuid,
            deviceId: deviceId,
            openTheDoor: openTheDoor,
            street: street,
            externallNumber: externallNumber,
            longitude: longitude,
            latitude: latitude,
            visitNumber: visitNumber,
            syncVisit: syncVisit,
            age: age,
            names: names,
            lastName: lastName,
            surName: surName,
            phone: phone,
            acceptInformation: acceptInformation,
            sex: sex,
            hasSympathy: hasSympathy,
            created: created,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$VisitPeoplesTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $VisitPeoplesTable,
    VisitPeopleRow,
    $$VisitPeoplesTableFilterComposer,
    $$VisitPeoplesTableOrderingComposer,
    $$VisitPeoplesTableAnnotationComposer,
    $$VisitPeoplesTableCreateCompanionBuilder,
    $$VisitPeoplesTableUpdateCompanionBuilder,
    (
      VisitPeopleRow,
      BaseReferences<_$AppDatabase, $VisitPeoplesTable, VisitPeopleRow>
    ),
    VisitPeopleRow,
    PrefetchHooks Function()>;
typedef $$FollowersTableCreateCompanionBuilder = FollowersCompanion Function({
  Value<int> id,
  required String uuid,
  required String deviceId,
  required String latitude,
  required String longitude,
  required String sectionNumber,
  required String street,
  required String externalNumber,
  Value<String?> internalNumber,
  Value<String?> zipCode,
  Value<String?> neighborhood,
  Value<String?> sectionNumberIne,
  Value<int?> residenceEntityId,
  Value<int?> residenceMunicipalityId,
  Value<bool?> isAdult,
  Value<String?> names,
  Value<String?> lastName,
  Value<String?> surName,
  Value<String?> curp,
  Value<String?> ine,
  Value<String?> validity,
  Value<String?> birthdate,
  Value<String?> cellPhoneNumber,
  Value<String?> supportsWhatsapp,
  Value<String?> cellPhoneNumberOther,
  Value<String?> email,
  Value<int?> catSexId,
  Value<String?> catSexNombre,
  Value<int?> birthEntityId,
  Value<bool> isInSync,
  required String supportsMovement,
  Value<String?> committeeParticipation,
  Value<String?> pollinStationRepresentativeParticipation,
  Value<String?> invitingOthersToParticipate,
  Value<String?> numberOfPeopleInvited,
  Value<DateTime?> created,
  Value<int?> positionId,
  Value<String> syncStatus,
  Value<String?> syncError,
  Value<int?> membershipId,
  Value<String?> positionLabel,
  Value<DateTime?> syncedAt,
});
typedef $$FollowersTableUpdateCompanionBuilder = FollowersCompanion Function({
  Value<int> id,
  Value<String> uuid,
  Value<String> deviceId,
  Value<String> latitude,
  Value<String> longitude,
  Value<String> sectionNumber,
  Value<String> street,
  Value<String> externalNumber,
  Value<String?> internalNumber,
  Value<String?> zipCode,
  Value<String?> neighborhood,
  Value<String?> sectionNumberIne,
  Value<int?> residenceEntityId,
  Value<int?> residenceMunicipalityId,
  Value<bool?> isAdult,
  Value<String?> names,
  Value<String?> lastName,
  Value<String?> surName,
  Value<String?> curp,
  Value<String?> ine,
  Value<String?> validity,
  Value<String?> birthdate,
  Value<String?> cellPhoneNumber,
  Value<String?> supportsWhatsapp,
  Value<String?> cellPhoneNumberOther,
  Value<String?> email,
  Value<int?> catSexId,
  Value<String?> catSexNombre,
  Value<int?> birthEntityId,
  Value<bool> isInSync,
  Value<String> supportsMovement,
  Value<String?> committeeParticipation,
  Value<String?> pollinStationRepresentativeParticipation,
  Value<String?> invitingOthersToParticipate,
  Value<String?> numberOfPeopleInvited,
  Value<DateTime?> created,
  Value<int?> positionId,
  Value<String> syncStatus,
  Value<String?> syncError,
  Value<int?> membershipId,
  Value<String?> positionLabel,
  Value<DateTime?> syncedAt,
});

class $$FollowersTableFilterComposer
    extends Composer<_$AppDatabase, $FollowersTable> {
  $$FollowersTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get deviceId => $composableBuilder(
      column: $table.deviceId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get latitude => $composableBuilder(
      column: $table.latitude, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get longitude => $composableBuilder(
      column: $table.longitude, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get sectionNumber => $composableBuilder(
      column: $table.sectionNumber, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get street => $composableBuilder(
      column: $table.street, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get externalNumber => $composableBuilder(
      column: $table.externalNumber,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get internalNumber => $composableBuilder(
      column: $table.internalNumber,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get zipCode => $composableBuilder(
      column: $table.zipCode, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get neighborhood => $composableBuilder(
      column: $table.neighborhood, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get sectionNumberIne => $composableBuilder(
      column: $table.sectionNumberIne,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get residenceEntityId => $composableBuilder(
      column: $table.residenceEntityId,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get residenceMunicipalityId => $composableBuilder(
      column: $table.residenceMunicipalityId,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isAdult => $composableBuilder(
      column: $table.isAdult, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get names => $composableBuilder(
      column: $table.names, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get lastName => $composableBuilder(
      column: $table.lastName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get surName => $composableBuilder(
      column: $table.surName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get curp => $composableBuilder(
      column: $table.curp, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get ine => $composableBuilder(
      column: $table.ine, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get validity => $composableBuilder(
      column: $table.validity, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get birthdate => $composableBuilder(
      column: $table.birthdate, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get cellPhoneNumber => $composableBuilder(
      column: $table.cellPhoneNumber,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get supportsWhatsapp => $composableBuilder(
      column: $table.supportsWhatsapp,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get cellPhoneNumberOther => $composableBuilder(
      column: $table.cellPhoneNumberOther,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get email => $composableBuilder(
      column: $table.email, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get catSexId => $composableBuilder(
      column: $table.catSexId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get catSexNombre => $composableBuilder(
      column: $table.catSexNombre, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get birthEntityId => $composableBuilder(
      column: $table.birthEntityId, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isInSync => $composableBuilder(
      column: $table.isInSync, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get supportsMovement => $composableBuilder(
      column: $table.supportsMovement,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get committeeParticipation => $composableBuilder(
      column: $table.committeeParticipation,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get pollinStationRepresentativeParticipation =>
      $composableBuilder(
          column: $table.pollinStationRepresentativeParticipation,
          builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get invitingOthersToParticipate => $composableBuilder(
      column: $table.invitingOthersToParticipate,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get numberOfPeopleInvited => $composableBuilder(
      column: $table.numberOfPeopleInvited,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get created => $composableBuilder(
      column: $table.created, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get positionId => $composableBuilder(
      column: $table.positionId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get syncStatus => $composableBuilder(
      column: $table.syncStatus, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get syncError => $composableBuilder(
      column: $table.syncError, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get membershipId => $composableBuilder(
      column: $table.membershipId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get positionLabel => $composableBuilder(
      column: $table.positionLabel, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get syncedAt => $composableBuilder(
      column: $table.syncedAt, builder: (column) => ColumnFilters(column));
}

class $$FollowersTableOrderingComposer
    extends Composer<_$AppDatabase, $FollowersTable> {
  $$FollowersTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get deviceId => $composableBuilder(
      column: $table.deviceId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get latitude => $composableBuilder(
      column: $table.latitude, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get longitude => $composableBuilder(
      column: $table.longitude, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get sectionNumber => $composableBuilder(
      column: $table.sectionNumber,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get street => $composableBuilder(
      column: $table.street, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get externalNumber => $composableBuilder(
      column: $table.externalNumber,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get internalNumber => $composableBuilder(
      column: $table.internalNumber,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get zipCode => $composableBuilder(
      column: $table.zipCode, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get neighborhood => $composableBuilder(
      column: $table.neighborhood,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get sectionNumberIne => $composableBuilder(
      column: $table.sectionNumberIne,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get residenceEntityId => $composableBuilder(
      column: $table.residenceEntityId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get residenceMunicipalityId => $composableBuilder(
      column: $table.residenceMunicipalityId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isAdult => $composableBuilder(
      column: $table.isAdult, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get names => $composableBuilder(
      column: $table.names, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get lastName => $composableBuilder(
      column: $table.lastName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get surName => $composableBuilder(
      column: $table.surName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get curp => $composableBuilder(
      column: $table.curp, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get ine => $composableBuilder(
      column: $table.ine, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get validity => $composableBuilder(
      column: $table.validity, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get birthdate => $composableBuilder(
      column: $table.birthdate, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get cellPhoneNumber => $composableBuilder(
      column: $table.cellPhoneNumber,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get supportsWhatsapp => $composableBuilder(
      column: $table.supportsWhatsapp,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get cellPhoneNumberOther => $composableBuilder(
      column: $table.cellPhoneNumberOther,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get email => $composableBuilder(
      column: $table.email, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get catSexId => $composableBuilder(
      column: $table.catSexId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get catSexNombre => $composableBuilder(
      column: $table.catSexNombre,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get birthEntityId => $composableBuilder(
      column: $table.birthEntityId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isInSync => $composableBuilder(
      column: $table.isInSync, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get supportsMovement => $composableBuilder(
      column: $table.supportsMovement,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get committeeParticipation => $composableBuilder(
      column: $table.committeeParticipation,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get pollinStationRepresentativeParticipation =>
      $composableBuilder(
          column: $table.pollinStationRepresentativeParticipation,
          builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get invitingOthersToParticipate => $composableBuilder(
      column: $table.invitingOthersToParticipate,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get numberOfPeopleInvited => $composableBuilder(
      column: $table.numberOfPeopleInvited,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get created => $composableBuilder(
      column: $table.created, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get positionId => $composableBuilder(
      column: $table.positionId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get syncStatus => $composableBuilder(
      column: $table.syncStatus, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get syncError => $composableBuilder(
      column: $table.syncError, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get membershipId => $composableBuilder(
      column: $table.membershipId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get positionLabel => $composableBuilder(
      column: $table.positionLabel,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get syncedAt => $composableBuilder(
      column: $table.syncedAt, builder: (column) => ColumnOrderings(column));
}

class $$FollowersTableAnnotationComposer
    extends Composer<_$AppDatabase, $FollowersTable> {
  $$FollowersTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get uuid =>
      $composableBuilder(column: $table.uuid, builder: (column) => column);

  GeneratedColumn<String> get deviceId =>
      $composableBuilder(column: $table.deviceId, builder: (column) => column);

  GeneratedColumn<String> get latitude =>
      $composableBuilder(column: $table.latitude, builder: (column) => column);

  GeneratedColumn<String> get longitude =>
      $composableBuilder(column: $table.longitude, builder: (column) => column);

  GeneratedColumn<String> get sectionNumber => $composableBuilder(
      column: $table.sectionNumber, builder: (column) => column);

  GeneratedColumn<String> get street =>
      $composableBuilder(column: $table.street, builder: (column) => column);

  GeneratedColumn<String> get externalNumber => $composableBuilder(
      column: $table.externalNumber, builder: (column) => column);

  GeneratedColumn<String> get internalNumber => $composableBuilder(
      column: $table.internalNumber, builder: (column) => column);

  GeneratedColumn<String> get zipCode =>
      $composableBuilder(column: $table.zipCode, builder: (column) => column);

  GeneratedColumn<String> get neighborhood => $composableBuilder(
      column: $table.neighborhood, builder: (column) => column);

  GeneratedColumn<String> get sectionNumberIne => $composableBuilder(
      column: $table.sectionNumberIne, builder: (column) => column);

  GeneratedColumn<int> get residenceEntityId => $composableBuilder(
      column: $table.residenceEntityId, builder: (column) => column);

  GeneratedColumn<int> get residenceMunicipalityId => $composableBuilder(
      column: $table.residenceMunicipalityId, builder: (column) => column);

  GeneratedColumn<bool> get isAdult =>
      $composableBuilder(column: $table.isAdult, builder: (column) => column);

  GeneratedColumn<String> get names =>
      $composableBuilder(column: $table.names, builder: (column) => column);

  GeneratedColumn<String> get lastName =>
      $composableBuilder(column: $table.lastName, builder: (column) => column);

  GeneratedColumn<String> get surName =>
      $composableBuilder(column: $table.surName, builder: (column) => column);

  GeneratedColumn<String> get curp =>
      $composableBuilder(column: $table.curp, builder: (column) => column);

  GeneratedColumn<String> get ine =>
      $composableBuilder(column: $table.ine, builder: (column) => column);

  GeneratedColumn<String> get validity =>
      $composableBuilder(column: $table.validity, builder: (column) => column);

  GeneratedColumn<String> get birthdate =>
      $composableBuilder(column: $table.birthdate, builder: (column) => column);

  GeneratedColumn<String> get cellPhoneNumber => $composableBuilder(
      column: $table.cellPhoneNumber, builder: (column) => column);

  GeneratedColumn<String> get supportsWhatsapp => $composableBuilder(
      column: $table.supportsWhatsapp, builder: (column) => column);

  GeneratedColumn<String> get cellPhoneNumberOther => $composableBuilder(
      column: $table.cellPhoneNumberOther, builder: (column) => column);

  GeneratedColumn<String> get email =>
      $composableBuilder(column: $table.email, builder: (column) => column);

  GeneratedColumn<int> get catSexId =>
      $composableBuilder(column: $table.catSexId, builder: (column) => column);

  GeneratedColumn<String> get catSexNombre => $composableBuilder(
      column: $table.catSexNombre, builder: (column) => column);

  GeneratedColumn<int> get birthEntityId => $composableBuilder(
      column: $table.birthEntityId, builder: (column) => column);

  GeneratedColumn<bool> get isInSync =>
      $composableBuilder(column: $table.isInSync, builder: (column) => column);

  GeneratedColumn<String> get supportsMovement => $composableBuilder(
      column: $table.supportsMovement, builder: (column) => column);

  GeneratedColumn<String> get committeeParticipation => $composableBuilder(
      column: $table.committeeParticipation, builder: (column) => column);

  GeneratedColumn<String> get pollinStationRepresentativeParticipation =>
      $composableBuilder(
          column: $table.pollinStationRepresentativeParticipation,
          builder: (column) => column);

  GeneratedColumn<String> get invitingOthersToParticipate => $composableBuilder(
      column: $table.invitingOthersToParticipate, builder: (column) => column);

  GeneratedColumn<String> get numberOfPeopleInvited => $composableBuilder(
      column: $table.numberOfPeopleInvited, builder: (column) => column);

  GeneratedColumn<DateTime> get created =>
      $composableBuilder(column: $table.created, builder: (column) => column);

  GeneratedColumn<int> get positionId => $composableBuilder(
      column: $table.positionId, builder: (column) => column);

  GeneratedColumn<String> get syncStatus => $composableBuilder(
      column: $table.syncStatus, builder: (column) => column);

  GeneratedColumn<String> get syncError =>
      $composableBuilder(column: $table.syncError, builder: (column) => column);

  GeneratedColumn<int> get membershipId => $composableBuilder(
      column: $table.membershipId, builder: (column) => column);

  GeneratedColumn<String> get positionLabel => $composableBuilder(
      column: $table.positionLabel, builder: (column) => column);

  GeneratedColumn<DateTime> get syncedAt =>
      $composableBuilder(column: $table.syncedAt, builder: (column) => column);
}

class $$FollowersTableTableManager extends RootTableManager<
    _$AppDatabase,
    $FollowersTable,
    FollowerRow,
    $$FollowersTableFilterComposer,
    $$FollowersTableOrderingComposer,
    $$FollowersTableAnnotationComposer,
    $$FollowersTableCreateCompanionBuilder,
    $$FollowersTableUpdateCompanionBuilder,
    (FollowerRow, BaseReferences<_$AppDatabase, $FollowersTable, FollowerRow>),
    FollowerRow,
    PrefetchHooks Function()> {
  $$FollowersTableTableManager(_$AppDatabase db, $FollowersTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$FollowersTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$FollowersTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$FollowersTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<String> uuid = const Value.absent(),
            Value<String> deviceId = const Value.absent(),
            Value<String> latitude = const Value.absent(),
            Value<String> longitude = const Value.absent(),
            Value<String> sectionNumber = const Value.absent(),
            Value<String> street = const Value.absent(),
            Value<String> externalNumber = const Value.absent(),
            Value<String?> internalNumber = const Value.absent(),
            Value<String?> zipCode = const Value.absent(),
            Value<String?> neighborhood = const Value.absent(),
            Value<String?> sectionNumberIne = const Value.absent(),
            Value<int?> residenceEntityId = const Value.absent(),
            Value<int?> residenceMunicipalityId = const Value.absent(),
            Value<bool?> isAdult = const Value.absent(),
            Value<String?> names = const Value.absent(),
            Value<String?> lastName = const Value.absent(),
            Value<String?> surName = const Value.absent(),
            Value<String?> curp = const Value.absent(),
            Value<String?> ine = const Value.absent(),
            Value<String?> validity = const Value.absent(),
            Value<String?> birthdate = const Value.absent(),
            Value<String?> cellPhoneNumber = const Value.absent(),
            Value<String?> supportsWhatsapp = const Value.absent(),
            Value<String?> cellPhoneNumberOther = const Value.absent(),
            Value<String?> email = const Value.absent(),
            Value<int?> catSexId = const Value.absent(),
            Value<String?> catSexNombre = const Value.absent(),
            Value<int?> birthEntityId = const Value.absent(),
            Value<bool> isInSync = const Value.absent(),
            Value<String> supportsMovement = const Value.absent(),
            Value<String?> committeeParticipation = const Value.absent(),
            Value<String?> pollinStationRepresentativeParticipation =
                const Value.absent(),
            Value<String?> invitingOthersToParticipate = const Value.absent(),
            Value<String?> numberOfPeopleInvited = const Value.absent(),
            Value<DateTime?> created = const Value.absent(),
            Value<int?> positionId = const Value.absent(),
            Value<String> syncStatus = const Value.absent(),
            Value<String?> syncError = const Value.absent(),
            Value<int?> membershipId = const Value.absent(),
            Value<String?> positionLabel = const Value.absent(),
            Value<DateTime?> syncedAt = const Value.absent(),
          }) =>
              FollowersCompanion(
            id: id,
            uuid: uuid,
            deviceId: deviceId,
            latitude: latitude,
            longitude: longitude,
            sectionNumber: sectionNumber,
            street: street,
            externalNumber: externalNumber,
            internalNumber: internalNumber,
            zipCode: zipCode,
            neighborhood: neighborhood,
            sectionNumberIne: sectionNumberIne,
            residenceEntityId: residenceEntityId,
            residenceMunicipalityId: residenceMunicipalityId,
            isAdult: isAdult,
            names: names,
            lastName: lastName,
            surName: surName,
            curp: curp,
            ine: ine,
            validity: validity,
            birthdate: birthdate,
            cellPhoneNumber: cellPhoneNumber,
            supportsWhatsapp: supportsWhatsapp,
            cellPhoneNumberOther: cellPhoneNumberOther,
            email: email,
            catSexId: catSexId,
            catSexNombre: catSexNombre,
            birthEntityId: birthEntityId,
            isInSync: isInSync,
            supportsMovement: supportsMovement,
            committeeParticipation: committeeParticipation,
            pollinStationRepresentativeParticipation:
                pollinStationRepresentativeParticipation,
            invitingOthersToParticipate: invitingOthersToParticipate,
            numberOfPeopleInvited: numberOfPeopleInvited,
            created: created,
            positionId: positionId,
            syncStatus: syncStatus,
            syncError: syncError,
            membershipId: membershipId,
            positionLabel: positionLabel,
            syncedAt: syncedAt,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            required String uuid,
            required String deviceId,
            required String latitude,
            required String longitude,
            required String sectionNumber,
            required String street,
            required String externalNumber,
            Value<String?> internalNumber = const Value.absent(),
            Value<String?> zipCode = const Value.absent(),
            Value<String?> neighborhood = const Value.absent(),
            Value<String?> sectionNumberIne = const Value.absent(),
            Value<int?> residenceEntityId = const Value.absent(),
            Value<int?> residenceMunicipalityId = const Value.absent(),
            Value<bool?> isAdult = const Value.absent(),
            Value<String?> names = const Value.absent(),
            Value<String?> lastName = const Value.absent(),
            Value<String?> surName = const Value.absent(),
            Value<String?> curp = const Value.absent(),
            Value<String?> ine = const Value.absent(),
            Value<String?> validity = const Value.absent(),
            Value<String?> birthdate = const Value.absent(),
            Value<String?> cellPhoneNumber = const Value.absent(),
            Value<String?> supportsWhatsapp = const Value.absent(),
            Value<String?> cellPhoneNumberOther = const Value.absent(),
            Value<String?> email = const Value.absent(),
            Value<int?> catSexId = const Value.absent(),
            Value<String?> catSexNombre = const Value.absent(),
            Value<int?> birthEntityId = const Value.absent(),
            Value<bool> isInSync = const Value.absent(),
            required String supportsMovement,
            Value<String?> committeeParticipation = const Value.absent(),
            Value<String?> pollinStationRepresentativeParticipation =
                const Value.absent(),
            Value<String?> invitingOthersToParticipate = const Value.absent(),
            Value<String?> numberOfPeopleInvited = const Value.absent(),
            Value<DateTime?> created = const Value.absent(),
            Value<int?> positionId = const Value.absent(),
            Value<String> syncStatus = const Value.absent(),
            Value<String?> syncError = const Value.absent(),
            Value<int?> membershipId = const Value.absent(),
            Value<String?> positionLabel = const Value.absent(),
            Value<DateTime?> syncedAt = const Value.absent(),
          }) =>
              FollowersCompanion.insert(
            id: id,
            uuid: uuid,
            deviceId: deviceId,
            latitude: latitude,
            longitude: longitude,
            sectionNumber: sectionNumber,
            street: street,
            externalNumber: externalNumber,
            internalNumber: internalNumber,
            zipCode: zipCode,
            neighborhood: neighborhood,
            sectionNumberIne: sectionNumberIne,
            residenceEntityId: residenceEntityId,
            residenceMunicipalityId: residenceMunicipalityId,
            isAdult: isAdult,
            names: names,
            lastName: lastName,
            surName: surName,
            curp: curp,
            ine: ine,
            validity: validity,
            birthdate: birthdate,
            cellPhoneNumber: cellPhoneNumber,
            supportsWhatsapp: supportsWhatsapp,
            cellPhoneNumberOther: cellPhoneNumberOther,
            email: email,
            catSexId: catSexId,
            catSexNombre: catSexNombre,
            birthEntityId: birthEntityId,
            isInSync: isInSync,
            supportsMovement: supportsMovement,
            committeeParticipation: committeeParticipation,
            pollinStationRepresentativeParticipation:
                pollinStationRepresentativeParticipation,
            invitingOthersToParticipate: invitingOthersToParticipate,
            numberOfPeopleInvited: numberOfPeopleInvited,
            created: created,
            positionId: positionId,
            syncStatus: syncStatus,
            syncError: syncError,
            membershipId: membershipId,
            positionLabel: positionLabel,
            syncedAt: syncedAt,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$FollowersTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $FollowersTable,
    FollowerRow,
    $$FollowersTableFilterComposer,
    $$FollowersTableOrderingComposer,
    $$FollowersTableAnnotationComposer,
    $$FollowersTableCreateCompanionBuilder,
    $$FollowersTableUpdateCompanionBuilder,
    (FollowerRow, BaseReferences<_$AppDatabase, $FollowersTable, FollowerRow>),
    FollowerRow,
    PrefetchHooks Function()>;
typedef $$UserSettingsTableTableCreateCompanionBuilder
    = UserSettingsTableCompanion Function({
  Value<int> id,
  Value<int?> municipalityId,
  Value<String?> municipalityName,
  Value<String> sectionsSelected,
});
typedef $$UserSettingsTableTableUpdateCompanionBuilder
    = UserSettingsTableCompanion Function({
  Value<int> id,
  Value<int?> municipalityId,
  Value<String?> municipalityName,
  Value<String> sectionsSelected,
});

class $$UserSettingsTableTableFilterComposer
    extends Composer<_$AppDatabase, $UserSettingsTableTable> {
  $$UserSettingsTableTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get municipalityId => $composableBuilder(
      column: $table.municipalityId,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get municipalityName => $composableBuilder(
      column: $table.municipalityName,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get sectionsSelected => $composableBuilder(
      column: $table.sectionsSelected,
      builder: (column) => ColumnFilters(column));
}

class $$UserSettingsTableTableOrderingComposer
    extends Composer<_$AppDatabase, $UserSettingsTableTable> {
  $$UserSettingsTableTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get municipalityId => $composableBuilder(
      column: $table.municipalityId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get municipalityName => $composableBuilder(
      column: $table.municipalityName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get sectionsSelected => $composableBuilder(
      column: $table.sectionsSelected,
      builder: (column) => ColumnOrderings(column));
}

class $$UserSettingsTableTableAnnotationComposer
    extends Composer<_$AppDatabase, $UserSettingsTableTable> {
  $$UserSettingsTableTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get municipalityId => $composableBuilder(
      column: $table.municipalityId, builder: (column) => column);

  GeneratedColumn<String> get municipalityName => $composableBuilder(
      column: $table.municipalityName, builder: (column) => column);

  GeneratedColumn<String> get sectionsSelected => $composableBuilder(
      column: $table.sectionsSelected, builder: (column) => column);
}

class $$UserSettingsTableTableTableManager extends RootTableManager<
    _$AppDatabase,
    $UserSettingsTableTable,
    UserSettingsRow,
    $$UserSettingsTableTableFilterComposer,
    $$UserSettingsTableTableOrderingComposer,
    $$UserSettingsTableTableAnnotationComposer,
    $$UserSettingsTableTableCreateCompanionBuilder,
    $$UserSettingsTableTableUpdateCompanionBuilder,
    (
      UserSettingsRow,
      BaseReferences<_$AppDatabase, $UserSettingsTableTable, UserSettingsRow>
    ),
    UserSettingsRow,
    PrefetchHooks Function()> {
  $$UserSettingsTableTableTableManager(
      _$AppDatabase db, $UserSettingsTableTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$UserSettingsTableTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$UserSettingsTableTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$UserSettingsTableTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<int?> municipalityId = const Value.absent(),
            Value<String?> municipalityName = const Value.absent(),
            Value<String> sectionsSelected = const Value.absent(),
          }) =>
              UserSettingsTableCompanion(
            id: id,
            municipalityId: municipalityId,
            municipalityName: municipalityName,
            sectionsSelected: sectionsSelected,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<int?> municipalityId = const Value.absent(),
            Value<String?> municipalityName = const Value.absent(),
            Value<String> sectionsSelected = const Value.absent(),
          }) =>
              UserSettingsTableCompanion.insert(
            id: id,
            municipalityId: municipalityId,
            municipalityName: municipalityName,
            sectionsSelected: sectionsSelected,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$UserSettingsTableTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $UserSettingsTableTable,
    UserSettingsRow,
    $$UserSettingsTableTableFilterComposer,
    $$UserSettingsTableTableOrderingComposer,
    $$UserSettingsTableTableAnnotationComposer,
    $$UserSettingsTableTableCreateCompanionBuilder,
    $$UserSettingsTableTableUpdateCompanionBuilder,
    (
      UserSettingsRow,
      BaseReferences<_$AppDatabase, $UserSettingsTableTable, UserSettingsRow>
    ),
    UserSettingsRow,
    PrefetchHooks Function()>;
typedef $$IssuesTableCreateCompanionBuilder = IssuesCompanion Function({
  Value<int> id,
  required String uuid,
  required String registrationDate,
  required String fullName,
  required String phone,
  required int municipalityId,
  required String municipalityName,
  Value<int> localityId,
  Value<String?> localityName,
  Value<String?> localityOther,
  Value<bool> isInSync,
  Value<DateTime?> created,
});
typedef $$IssuesTableUpdateCompanionBuilder = IssuesCompanion Function({
  Value<int> id,
  Value<String> uuid,
  Value<String> registrationDate,
  Value<String> fullName,
  Value<String> phone,
  Value<int> municipalityId,
  Value<String> municipalityName,
  Value<int> localityId,
  Value<String?> localityName,
  Value<String?> localityOther,
  Value<bool> isInSync,
  Value<DateTime?> created,
});

class $$IssuesTableFilterComposer
    extends Composer<_$AppDatabase, $IssuesTable> {
  $$IssuesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get registrationDate => $composableBuilder(
      column: $table.registrationDate,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get fullName => $composableBuilder(
      column: $table.fullName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get phone => $composableBuilder(
      column: $table.phone, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get municipalityId => $composableBuilder(
      column: $table.municipalityId,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get municipalityName => $composableBuilder(
      column: $table.municipalityName,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get localityId => $composableBuilder(
      column: $table.localityId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get localityName => $composableBuilder(
      column: $table.localityName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get localityOther => $composableBuilder(
      column: $table.localityOther, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get isInSync => $composableBuilder(
      column: $table.isInSync, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get created => $composableBuilder(
      column: $table.created, builder: (column) => ColumnFilters(column));
}

class $$IssuesTableOrderingComposer
    extends Composer<_$AppDatabase, $IssuesTable> {
  $$IssuesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get registrationDate => $composableBuilder(
      column: $table.registrationDate,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get fullName => $composableBuilder(
      column: $table.fullName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get phone => $composableBuilder(
      column: $table.phone, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get municipalityId => $composableBuilder(
      column: $table.municipalityId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get municipalityName => $composableBuilder(
      column: $table.municipalityName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get localityId => $composableBuilder(
      column: $table.localityId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get localityName => $composableBuilder(
      column: $table.localityName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get localityOther => $composableBuilder(
      column: $table.localityOther,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get isInSync => $composableBuilder(
      column: $table.isInSync, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get created => $composableBuilder(
      column: $table.created, builder: (column) => ColumnOrderings(column));
}

class $$IssuesTableAnnotationComposer
    extends Composer<_$AppDatabase, $IssuesTable> {
  $$IssuesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get uuid =>
      $composableBuilder(column: $table.uuid, builder: (column) => column);

  GeneratedColumn<String> get registrationDate => $composableBuilder(
      column: $table.registrationDate, builder: (column) => column);

  GeneratedColumn<String> get fullName =>
      $composableBuilder(column: $table.fullName, builder: (column) => column);

  GeneratedColumn<String> get phone =>
      $composableBuilder(column: $table.phone, builder: (column) => column);

  GeneratedColumn<int> get municipalityId => $composableBuilder(
      column: $table.municipalityId, builder: (column) => column);

  GeneratedColumn<String> get municipalityName => $composableBuilder(
      column: $table.municipalityName, builder: (column) => column);

  GeneratedColumn<int> get localityId => $composableBuilder(
      column: $table.localityId, builder: (column) => column);

  GeneratedColumn<String> get localityName => $composableBuilder(
      column: $table.localityName, builder: (column) => column);

  GeneratedColumn<String> get localityOther => $composableBuilder(
      column: $table.localityOther, builder: (column) => column);

  GeneratedColumn<bool> get isInSync =>
      $composableBuilder(column: $table.isInSync, builder: (column) => column);

  GeneratedColumn<DateTime> get created =>
      $composableBuilder(column: $table.created, builder: (column) => column);
}

class $$IssuesTableTableManager extends RootTableManager<
    _$AppDatabase,
    $IssuesTable,
    IssueRow,
    $$IssuesTableFilterComposer,
    $$IssuesTableOrderingComposer,
    $$IssuesTableAnnotationComposer,
    $$IssuesTableCreateCompanionBuilder,
    $$IssuesTableUpdateCompanionBuilder,
    (IssueRow, BaseReferences<_$AppDatabase, $IssuesTable, IssueRow>),
    IssueRow,
    PrefetchHooks Function()> {
  $$IssuesTableTableManager(_$AppDatabase db, $IssuesTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$IssuesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$IssuesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$IssuesTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<String> uuid = const Value.absent(),
            Value<String> registrationDate = const Value.absent(),
            Value<String> fullName = const Value.absent(),
            Value<String> phone = const Value.absent(),
            Value<int> municipalityId = const Value.absent(),
            Value<String> municipalityName = const Value.absent(),
            Value<int> localityId = const Value.absent(),
            Value<String?> localityName = const Value.absent(),
            Value<String?> localityOther = const Value.absent(),
            Value<bool> isInSync = const Value.absent(),
            Value<DateTime?> created = const Value.absent(),
          }) =>
              IssuesCompanion(
            id: id,
            uuid: uuid,
            registrationDate: registrationDate,
            fullName: fullName,
            phone: phone,
            municipalityId: municipalityId,
            municipalityName: municipalityName,
            localityId: localityId,
            localityName: localityName,
            localityOther: localityOther,
            isInSync: isInSync,
            created: created,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            required String uuid,
            required String registrationDate,
            required String fullName,
            required String phone,
            required int municipalityId,
            required String municipalityName,
            Value<int> localityId = const Value.absent(),
            Value<String?> localityName = const Value.absent(),
            Value<String?> localityOther = const Value.absent(),
            Value<bool> isInSync = const Value.absent(),
            Value<DateTime?> created = const Value.absent(),
          }) =>
              IssuesCompanion.insert(
            id: id,
            uuid: uuid,
            registrationDate: registrationDate,
            fullName: fullName,
            phone: phone,
            municipalityId: municipalityId,
            municipalityName: municipalityName,
            localityId: localityId,
            localityName: localityName,
            localityOther: localityOther,
            isInSync: isInSync,
            created: created,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$IssuesTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $IssuesTable,
    IssueRow,
    $$IssuesTableFilterComposer,
    $$IssuesTableOrderingComposer,
    $$IssuesTableAnnotationComposer,
    $$IssuesTableCreateCompanionBuilder,
    $$IssuesTableUpdateCompanionBuilder,
    (IssueRow, BaseReferences<_$AppDatabase, $IssuesTable, IssueRow>),
    IssueRow,
    PrefetchHooks Function()>;
typedef $$IssueDetailsTableCreateCompanionBuilder = IssueDetailsCompanion
    Function({
  Value<int> id,
  required String uuid,
  required String issueUuid,
  required String comments,
  Value<bool> urgent,
  required int issueTypeId,
  required String issueTypeName,
  required int issueCategoryId,
  required String issueCategoryName,
  required int issueStatusId,
  required String issueStatusName,
  Value<String?> name,
  Value<String?> lastName,
  Value<String?> surName,
  Value<String?> curp,
  Value<int?> resourceId,
  Value<String?> resourceName,
  Value<int?> docCategoryId,
  Value<String?> docCategoryName,
  Value<String?> localFilePath,
});
typedef $$IssueDetailsTableUpdateCompanionBuilder = IssueDetailsCompanion
    Function({
  Value<int> id,
  Value<String> uuid,
  Value<String> issueUuid,
  Value<String> comments,
  Value<bool> urgent,
  Value<int> issueTypeId,
  Value<String> issueTypeName,
  Value<int> issueCategoryId,
  Value<String> issueCategoryName,
  Value<int> issueStatusId,
  Value<String> issueStatusName,
  Value<String?> name,
  Value<String?> lastName,
  Value<String?> surName,
  Value<String?> curp,
  Value<int?> resourceId,
  Value<String?> resourceName,
  Value<int?> docCategoryId,
  Value<String?> docCategoryName,
  Value<String?> localFilePath,
});

class $$IssueDetailsTableFilterComposer
    extends Composer<_$AppDatabase, $IssueDetailsTable> {
  $$IssueDetailsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get issueUuid => $composableBuilder(
      column: $table.issueUuid, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get comments => $composableBuilder(
      column: $table.comments, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get urgent => $composableBuilder(
      column: $table.urgent, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get issueTypeId => $composableBuilder(
      column: $table.issueTypeId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get issueTypeName => $composableBuilder(
      column: $table.issueTypeName, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get issueCategoryId => $composableBuilder(
      column: $table.issueCategoryId,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get issueCategoryName => $composableBuilder(
      column: $table.issueCategoryName,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get issueStatusId => $composableBuilder(
      column: $table.issueStatusId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get issueStatusName => $composableBuilder(
      column: $table.issueStatusName,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get lastName => $composableBuilder(
      column: $table.lastName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get surName => $composableBuilder(
      column: $table.surName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get curp => $composableBuilder(
      column: $table.curp, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get resourceId => $composableBuilder(
      column: $table.resourceId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get resourceName => $composableBuilder(
      column: $table.resourceName, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get docCategoryId => $composableBuilder(
      column: $table.docCategoryId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get docCategoryName => $composableBuilder(
      column: $table.docCategoryName,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get localFilePath => $composableBuilder(
      column: $table.localFilePath, builder: (column) => ColumnFilters(column));
}

class $$IssueDetailsTableOrderingComposer
    extends Composer<_$AppDatabase, $IssueDetailsTable> {
  $$IssueDetailsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get uuid => $composableBuilder(
      column: $table.uuid, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get issueUuid => $composableBuilder(
      column: $table.issueUuid, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get comments => $composableBuilder(
      column: $table.comments, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get urgent => $composableBuilder(
      column: $table.urgent, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get issueTypeId => $composableBuilder(
      column: $table.issueTypeId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get issueTypeName => $composableBuilder(
      column: $table.issueTypeName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get issueCategoryId => $composableBuilder(
      column: $table.issueCategoryId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get issueCategoryName => $composableBuilder(
      column: $table.issueCategoryName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get issueStatusId => $composableBuilder(
      column: $table.issueStatusId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get issueStatusName => $composableBuilder(
      column: $table.issueStatusName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get lastName => $composableBuilder(
      column: $table.lastName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get surName => $composableBuilder(
      column: $table.surName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get curp => $composableBuilder(
      column: $table.curp, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get resourceId => $composableBuilder(
      column: $table.resourceId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get resourceName => $composableBuilder(
      column: $table.resourceName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get docCategoryId => $composableBuilder(
      column: $table.docCategoryId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get docCategoryName => $composableBuilder(
      column: $table.docCategoryName,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get localFilePath => $composableBuilder(
      column: $table.localFilePath,
      builder: (column) => ColumnOrderings(column));
}

class $$IssueDetailsTableAnnotationComposer
    extends Composer<_$AppDatabase, $IssueDetailsTable> {
  $$IssueDetailsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get uuid =>
      $composableBuilder(column: $table.uuid, builder: (column) => column);

  GeneratedColumn<String> get issueUuid =>
      $composableBuilder(column: $table.issueUuid, builder: (column) => column);

  GeneratedColumn<String> get comments =>
      $composableBuilder(column: $table.comments, builder: (column) => column);

  GeneratedColumn<bool> get urgent =>
      $composableBuilder(column: $table.urgent, builder: (column) => column);

  GeneratedColumn<int> get issueTypeId => $composableBuilder(
      column: $table.issueTypeId, builder: (column) => column);

  GeneratedColumn<String> get issueTypeName => $composableBuilder(
      column: $table.issueTypeName, builder: (column) => column);

  GeneratedColumn<int> get issueCategoryId => $composableBuilder(
      column: $table.issueCategoryId, builder: (column) => column);

  GeneratedColumn<String> get issueCategoryName => $composableBuilder(
      column: $table.issueCategoryName, builder: (column) => column);

  GeneratedColumn<int> get issueStatusId => $composableBuilder(
      column: $table.issueStatusId, builder: (column) => column);

  GeneratedColumn<String> get issueStatusName => $composableBuilder(
      column: $table.issueStatusName, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get lastName =>
      $composableBuilder(column: $table.lastName, builder: (column) => column);

  GeneratedColumn<String> get surName =>
      $composableBuilder(column: $table.surName, builder: (column) => column);

  GeneratedColumn<String> get curp =>
      $composableBuilder(column: $table.curp, builder: (column) => column);

  GeneratedColumn<int> get resourceId => $composableBuilder(
      column: $table.resourceId, builder: (column) => column);

  GeneratedColumn<String> get resourceName => $composableBuilder(
      column: $table.resourceName, builder: (column) => column);

  GeneratedColumn<int> get docCategoryId => $composableBuilder(
      column: $table.docCategoryId, builder: (column) => column);

  GeneratedColumn<String> get docCategoryName => $composableBuilder(
      column: $table.docCategoryName, builder: (column) => column);

  GeneratedColumn<String> get localFilePath => $composableBuilder(
      column: $table.localFilePath, builder: (column) => column);
}

class $$IssueDetailsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $IssueDetailsTable,
    IssueDetailRow,
    $$IssueDetailsTableFilterComposer,
    $$IssueDetailsTableOrderingComposer,
    $$IssueDetailsTableAnnotationComposer,
    $$IssueDetailsTableCreateCompanionBuilder,
    $$IssueDetailsTableUpdateCompanionBuilder,
    (
      IssueDetailRow,
      BaseReferences<_$AppDatabase, $IssueDetailsTable, IssueDetailRow>
    ),
    IssueDetailRow,
    PrefetchHooks Function()> {
  $$IssueDetailsTableTableManager(_$AppDatabase db, $IssueDetailsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$IssueDetailsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$IssueDetailsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$IssueDetailsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<String> uuid = const Value.absent(),
            Value<String> issueUuid = const Value.absent(),
            Value<String> comments = const Value.absent(),
            Value<bool> urgent = const Value.absent(),
            Value<int> issueTypeId = const Value.absent(),
            Value<String> issueTypeName = const Value.absent(),
            Value<int> issueCategoryId = const Value.absent(),
            Value<String> issueCategoryName = const Value.absent(),
            Value<int> issueStatusId = const Value.absent(),
            Value<String> issueStatusName = const Value.absent(),
            Value<String?> name = const Value.absent(),
            Value<String?> lastName = const Value.absent(),
            Value<String?> surName = const Value.absent(),
            Value<String?> curp = const Value.absent(),
            Value<int?> resourceId = const Value.absent(),
            Value<String?> resourceName = const Value.absent(),
            Value<int?> docCategoryId = const Value.absent(),
            Value<String?> docCategoryName = const Value.absent(),
            Value<String?> localFilePath = const Value.absent(),
          }) =>
              IssueDetailsCompanion(
            id: id,
            uuid: uuid,
            issueUuid: issueUuid,
            comments: comments,
            urgent: urgent,
            issueTypeId: issueTypeId,
            issueTypeName: issueTypeName,
            issueCategoryId: issueCategoryId,
            issueCategoryName: issueCategoryName,
            issueStatusId: issueStatusId,
            issueStatusName: issueStatusName,
            name: name,
            lastName: lastName,
            surName: surName,
            curp: curp,
            resourceId: resourceId,
            resourceName: resourceName,
            docCategoryId: docCategoryId,
            docCategoryName: docCategoryName,
            localFilePath: localFilePath,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            required String uuid,
            required String issueUuid,
            required String comments,
            Value<bool> urgent = const Value.absent(),
            required int issueTypeId,
            required String issueTypeName,
            required int issueCategoryId,
            required String issueCategoryName,
            required int issueStatusId,
            required String issueStatusName,
            Value<String?> name = const Value.absent(),
            Value<String?> lastName = const Value.absent(),
            Value<String?> surName = const Value.absent(),
            Value<String?> curp = const Value.absent(),
            Value<int?> resourceId = const Value.absent(),
            Value<String?> resourceName = const Value.absent(),
            Value<int?> docCategoryId = const Value.absent(),
            Value<String?> docCategoryName = const Value.absent(),
            Value<String?> localFilePath = const Value.absent(),
          }) =>
              IssueDetailsCompanion.insert(
            id: id,
            uuid: uuid,
            issueUuid: issueUuid,
            comments: comments,
            urgent: urgent,
            issueTypeId: issueTypeId,
            issueTypeName: issueTypeName,
            issueCategoryId: issueCategoryId,
            issueCategoryName: issueCategoryName,
            issueStatusId: issueStatusId,
            issueStatusName: issueStatusName,
            name: name,
            lastName: lastName,
            surName: surName,
            curp: curp,
            resourceId: resourceId,
            resourceName: resourceName,
            docCategoryId: docCategoryId,
            docCategoryName: docCategoryName,
            localFilePath: localFilePath,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$IssueDetailsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $IssueDetailsTable,
    IssueDetailRow,
    $$IssueDetailsTableFilterComposer,
    $$IssueDetailsTableOrderingComposer,
    $$IssueDetailsTableAnnotationComposer,
    $$IssueDetailsTableCreateCompanionBuilder,
    $$IssueDetailsTableUpdateCompanionBuilder,
    (
      IssueDetailRow,
      BaseReferences<_$AppDatabase, $IssueDetailsTable, IssueDetailRow>
    ),
    IssueDetailRow,
    PrefetchHooks Function()>;
typedef $$CaptureTargetsTableCreateCompanionBuilder = CaptureTargetsCompanion
    Function({
  Value<int> positionId,
  required String level,
  required String label,
  Value<String?> holderName,
  Value<int?> sectionGeoId,
  Value<int?> municipalityGeoId,
  Value<bool> own,
  required DateTime updated,
});
typedef $$CaptureTargetsTableUpdateCompanionBuilder = CaptureTargetsCompanion
    Function({
  Value<int> positionId,
  Value<String> level,
  Value<String> label,
  Value<String?> holderName,
  Value<int?> sectionGeoId,
  Value<int?> municipalityGeoId,
  Value<bool> own,
  Value<DateTime> updated,
});

class $$CaptureTargetsTableFilterComposer
    extends Composer<_$AppDatabase, $CaptureTargetsTable> {
  $$CaptureTargetsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get positionId => $composableBuilder(
      column: $table.positionId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get level => $composableBuilder(
      column: $table.level, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get label => $composableBuilder(
      column: $table.label, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get holderName => $composableBuilder(
      column: $table.holderName, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get sectionGeoId => $composableBuilder(
      column: $table.sectionGeoId, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get municipalityGeoId => $composableBuilder(
      column: $table.municipalityGeoId,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get own => $composableBuilder(
      column: $table.own, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updated => $composableBuilder(
      column: $table.updated, builder: (column) => ColumnFilters(column));
}

class $$CaptureTargetsTableOrderingComposer
    extends Composer<_$AppDatabase, $CaptureTargetsTable> {
  $$CaptureTargetsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get positionId => $composableBuilder(
      column: $table.positionId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get level => $composableBuilder(
      column: $table.level, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get label => $composableBuilder(
      column: $table.label, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get holderName => $composableBuilder(
      column: $table.holderName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get sectionGeoId => $composableBuilder(
      column: $table.sectionGeoId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get municipalityGeoId => $composableBuilder(
      column: $table.municipalityGeoId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get own => $composableBuilder(
      column: $table.own, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updated => $composableBuilder(
      column: $table.updated, builder: (column) => ColumnOrderings(column));
}

class $$CaptureTargetsTableAnnotationComposer
    extends Composer<_$AppDatabase, $CaptureTargetsTable> {
  $$CaptureTargetsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get positionId => $composableBuilder(
      column: $table.positionId, builder: (column) => column);

  GeneratedColumn<String> get level =>
      $composableBuilder(column: $table.level, builder: (column) => column);

  GeneratedColumn<String> get label =>
      $composableBuilder(column: $table.label, builder: (column) => column);

  GeneratedColumn<String> get holderName => $composableBuilder(
      column: $table.holderName, builder: (column) => column);

  GeneratedColumn<int> get sectionGeoId => $composableBuilder(
      column: $table.sectionGeoId, builder: (column) => column);

  GeneratedColumn<int> get municipalityGeoId => $composableBuilder(
      column: $table.municipalityGeoId, builder: (column) => column);

  GeneratedColumn<bool> get own =>
      $composableBuilder(column: $table.own, builder: (column) => column);

  GeneratedColumn<DateTime> get updated =>
      $composableBuilder(column: $table.updated, builder: (column) => column);
}

class $$CaptureTargetsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $CaptureTargetsTable,
    CaptureTargetRow,
    $$CaptureTargetsTableFilterComposer,
    $$CaptureTargetsTableOrderingComposer,
    $$CaptureTargetsTableAnnotationComposer,
    $$CaptureTargetsTableCreateCompanionBuilder,
    $$CaptureTargetsTableUpdateCompanionBuilder,
    (
      CaptureTargetRow,
      BaseReferences<_$AppDatabase, $CaptureTargetsTable, CaptureTargetRow>
    ),
    CaptureTargetRow,
    PrefetchHooks Function()> {
  $$CaptureTargetsTableTableManager(
      _$AppDatabase db, $CaptureTargetsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$CaptureTargetsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$CaptureTargetsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$CaptureTargetsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> positionId = const Value.absent(),
            Value<String> level = const Value.absent(),
            Value<String> label = const Value.absent(),
            Value<String?> holderName = const Value.absent(),
            Value<int?> sectionGeoId = const Value.absent(),
            Value<int?> municipalityGeoId = const Value.absent(),
            Value<bool> own = const Value.absent(),
            Value<DateTime> updated = const Value.absent(),
          }) =>
              CaptureTargetsCompanion(
            positionId: positionId,
            level: level,
            label: label,
            holderName: holderName,
            sectionGeoId: sectionGeoId,
            municipalityGeoId: municipalityGeoId,
            own: own,
            updated: updated,
          ),
          createCompanionCallback: ({
            Value<int> positionId = const Value.absent(),
            required String level,
            required String label,
            Value<String?> holderName = const Value.absent(),
            Value<int?> sectionGeoId = const Value.absent(),
            Value<int?> municipalityGeoId = const Value.absent(),
            Value<bool> own = const Value.absent(),
            required DateTime updated,
          }) =>
              CaptureTargetsCompanion.insert(
            positionId: positionId,
            level: level,
            label: label,
            holderName: holderName,
            sectionGeoId: sectionGeoId,
            municipalityGeoId: municipalityGeoId,
            own: own,
            updated: updated,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$CaptureTargetsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $CaptureTargetsTable,
    CaptureTargetRow,
    $$CaptureTargetsTableFilterComposer,
    $$CaptureTargetsTableOrderingComposer,
    $$CaptureTargetsTableAnnotationComposer,
    $$CaptureTargetsTableCreateCompanionBuilder,
    $$CaptureTargetsTableUpdateCompanionBuilder,
    (
      CaptureTargetRow,
      BaseReferences<_$AppDatabase, $CaptureTargetsTable, CaptureTargetRow>
    ),
    CaptureTargetRow,
    PrefetchHooks Function()>;
typedef $$SectionGeometriesTableCreateCompanionBuilder
    = SectionGeometriesCompanion Function({
  Value<int> sectionGeoId,
  required String geoJson,
  required DateTime updated,
});
typedef $$SectionGeometriesTableUpdateCompanionBuilder
    = SectionGeometriesCompanion Function({
  Value<int> sectionGeoId,
  Value<String> geoJson,
  Value<DateTime> updated,
});

class $$SectionGeometriesTableFilterComposer
    extends Composer<_$AppDatabase, $SectionGeometriesTable> {
  $$SectionGeometriesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get sectionGeoId => $composableBuilder(
      column: $table.sectionGeoId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get geoJson => $composableBuilder(
      column: $table.geoJson, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updated => $composableBuilder(
      column: $table.updated, builder: (column) => ColumnFilters(column));
}

class $$SectionGeometriesTableOrderingComposer
    extends Composer<_$AppDatabase, $SectionGeometriesTable> {
  $$SectionGeometriesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get sectionGeoId => $composableBuilder(
      column: $table.sectionGeoId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get geoJson => $composableBuilder(
      column: $table.geoJson, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updated => $composableBuilder(
      column: $table.updated, builder: (column) => ColumnOrderings(column));
}

class $$SectionGeometriesTableAnnotationComposer
    extends Composer<_$AppDatabase, $SectionGeometriesTable> {
  $$SectionGeometriesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get sectionGeoId => $composableBuilder(
      column: $table.sectionGeoId, builder: (column) => column);

  GeneratedColumn<String> get geoJson =>
      $composableBuilder(column: $table.geoJson, builder: (column) => column);

  GeneratedColumn<DateTime> get updated =>
      $composableBuilder(column: $table.updated, builder: (column) => column);
}

class $$SectionGeometriesTableTableManager extends RootTableManager<
    _$AppDatabase,
    $SectionGeometriesTable,
    SectionGeometryRow,
    $$SectionGeometriesTableFilterComposer,
    $$SectionGeometriesTableOrderingComposer,
    $$SectionGeometriesTableAnnotationComposer,
    $$SectionGeometriesTableCreateCompanionBuilder,
    $$SectionGeometriesTableUpdateCompanionBuilder,
    (
      SectionGeometryRow,
      BaseReferences<_$AppDatabase, $SectionGeometriesTable, SectionGeometryRow>
    ),
    SectionGeometryRow,
    PrefetchHooks Function()> {
  $$SectionGeometriesTableTableManager(
      _$AppDatabase db, $SectionGeometriesTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$SectionGeometriesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$SectionGeometriesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$SectionGeometriesTableAnnotationComposer(
                  $db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> sectionGeoId = const Value.absent(),
            Value<String> geoJson = const Value.absent(),
            Value<DateTime> updated = const Value.absent(),
          }) =>
              SectionGeometriesCompanion(
            sectionGeoId: sectionGeoId,
            geoJson: geoJson,
            updated: updated,
          ),
          createCompanionCallback: ({
            Value<int> sectionGeoId = const Value.absent(),
            required String geoJson,
            required DateTime updated,
          }) =>
              SectionGeometriesCompanion.insert(
            sectionGeoId: sectionGeoId,
            geoJson: geoJson,
            updated: updated,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$SectionGeometriesTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $SectionGeometriesTable,
    SectionGeometryRow,
    $$SectionGeometriesTableFilterComposer,
    $$SectionGeometriesTableOrderingComposer,
    $$SectionGeometriesTableAnnotationComposer,
    $$SectionGeometriesTableCreateCompanionBuilder,
    $$SectionGeometriesTableUpdateCompanionBuilder,
    (
      SectionGeometryRow,
      BaseReferences<_$AppDatabase, $SectionGeometriesTable, SectionGeometryRow>
    ),
    SectionGeometryRow,
    PrefetchHooks Function()>;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$CanvasWallsTableTableManager get canvasWalls =>
      $$CanvasWallsTableTableManager(_db, _db.canvasWalls);
  $$VisitPeoplesTableTableManager get visitPeoples =>
      $$VisitPeoplesTableTableManager(_db, _db.visitPeoples);
  $$FollowersTableTableManager get followers =>
      $$FollowersTableTableManager(_db, _db.followers);
  $$UserSettingsTableTableTableManager get userSettingsTable =>
      $$UserSettingsTableTableTableManager(_db, _db.userSettingsTable);
  $$IssuesTableTableManager get issues =>
      $$IssuesTableTableManager(_db, _db.issues);
  $$IssueDetailsTableTableManager get issueDetails =>
      $$IssueDetailsTableTableManager(_db, _db.issueDetails);
  $$CaptureTargetsTableTableManager get captureTargets =>
      $$CaptureTargetsTableTableManager(_db, _db.captureTargets);
  $$SectionGeometriesTableTableManager get sectionGeometries =>
      $$SectionGeometriesTableTableManager(_db, _db.sectionGeometries);
}
