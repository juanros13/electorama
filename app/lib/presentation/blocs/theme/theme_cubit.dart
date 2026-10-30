import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:electorama_app/infrastructure/services/key_value_storage_service.dart';

const String themeModeKey = 'theme_mode';

/// Maneja el modo de tema (claro/oscuro/sistema) y lo persiste como String
/// vía [KeyValueStorageService] (el service no soporta bool).
class ThemeCubit extends Cubit<ThemeMode> {
  final KeyValueStorageService _storage;

  ThemeCubit(this._storage) : super(ThemeMode.system) {
    _load();
  }

  Future<void> _load() async {
    final stored = await _storage.getValue<String>(themeModeKey);
    emit(_fromString(stored));
  }

  Future<void> setMode(ThemeMode mode) async {
    emit(mode);
    await _storage.setKeyValue<String>(themeModeKey, _toString(mode));
  }

  void toggle() =>
      setMode(state == ThemeMode.dark ? ThemeMode.light : ThemeMode.dark);

  static ThemeMode _fromString(String? v) {
    switch (v) {
      case 'light':
        return ThemeMode.light;
      case 'dark':
        return ThemeMode.dark;
      default:
        return ThemeMode.system;
    }
  }

  static String _toString(ThemeMode mode) {
    switch (mode) {
      case ThemeMode.light:
        return 'light';
      case ThemeMode.dark:
        return 'dark';
      case ThemeMode.system:
        return 'system';
    }
  }
}
