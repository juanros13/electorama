import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../config/constants/environment.dart';
import '../../domain/entities/auth/user.dart';

/// No hay red: la sesión sigue siendo válida para capturar, pero no se puede
/// hablar con el servidor.
class OfflineException implements Exception {
  final String message;
  const OfflineException([this.message = 'Sin conexión']);
  @override
  String toString() => message;
}

/// Keycloak ya no acepta la sesión (refresh vencido o revocado): hay que
/// volver a iniciar sesión.
class SessionExpiredException implements Exception {
  final String message;
  const SessionExpiredException(
      [this.message = 'Tu sesión venció: vuelve a iniciar sesión']);
  @override
  String toString() => message;
}

/// La sesión del dispositivo, para capturar sin señal.
///
/// Guarda el access token, el refresh token y el usuario. Sin red, la app
/// sigue con el usuario guardado; con red, [validToken] renueva el access
/// token cuando está por vencer. Sólo se cierra la sesión cuando Keycloak la
/// rechaza, nunca por falta de red.
class AuthSession {
  static const _tokenKey = 'token';
  static const _refreshKey = 'refresh_token';
  static const _userKey = 'session_user';

  /// Antes de este margen el access token ya se considera vencido.
  static const _margin = Duration(seconds: 60);

  static Future<SharedPreferences> get _prefs => SharedPreferences.getInstance();

  static Future<void> saveTokens(String accessToken, String? refreshToken) async {
    final prefs = await _prefs;
    await prefs.setString(_tokenKey, accessToken);
    if (refreshToken != null && refreshToken.isNotEmpty) {
      await prefs.setString(_refreshKey, refreshToken);
    }
  }

  static Future<void> saveUser(User user) async {
    final prefs = await _prefs;
    await prefs.setString(
        _userKey,
        jsonEncode({
          'authId': user.authId,
          'email': user.email,
          'firstName': user.firstName,
          'lastName': user.lastName,
          'roles': user.roles,
          'authIdReference': user.authIdReference,
        }));
  }

  /// El usuario guardado con el token guardado (puede estar vencido: sirve
  /// para capturar offline, no para hablar con el servidor).
  static Future<User?> loadUser() async {
    final prefs = await _prefs;
    final raw = prefs.getString(_userKey);
    final token = prefs.getString(_tokenKey);
    if (raw == null || token == null) {
      return null;
    }
    final m = jsonDecode(raw) as Map<String, dynamic>;
    return User(
      authId: m['authId'] ?? '',
      email: m['email'] ?? '',
      firstName: m['firstName'] ?? '',
      lastName: m['lastName'] ?? '',
      roles: List<String>.from(m['roles'] ?? const []),
      token: token,
      authIdReference: m['authIdReference'] ?? '',
    );
  }

  static Future<String?> storedToken() async => (await _prefs).getString(_tokenKey);

  static Future<void> clear() async {
    final prefs = await _prefs;
    await prefs.remove(_tokenKey);
    await prefs.remove(_refreshKey);
    await prefs.remove(_userKey);
  }

  static bool _expiringSoon(String token) {
    try {
      return JwtDecoder.getExpirationDate(token)
          .isBefore(DateTime.now().add(_margin));
    } catch (_) {
      return true;
    }
  }

  /// Un access token vigente para llamar al servidor; lo renueva si hace falta.
  ///
  /// Lanza [OfflineException] sin red y [SessionExpiredException] si Keycloak
  /// ya no acepta la sesión.
  static Future<String> validToken({bool forceRefresh = false}) async {
    final prefs = await _prefs;
    final token = prefs.getString(_tokenKey);
    if (token == null) {
      throw const SessionExpiredException('Inicia sesión para sincronizar');
    }
    if (!forceRefresh && !_expiringSoon(token)) {
      return token;
    }
    final refresh = prefs.getString(_refreshKey);
    if (refresh == null) {
      throw const SessionExpiredException();
    }
    final dio = Dio(BaseOptions(
      baseUrl: Environment.apiUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
    ));
    try {
      final response = await dio.post(
        '/protocol/openid-connect/token',
        data: {
          'client_id': Environment.clientId,
          'client_secret': Environment.clientSecret,
          'grant_type': 'refresh_token',
          'refresh_token': refresh,
        },
        options: Options(contentType: 'application/x-www-form-urlencoded'),
      );
      final access = response.data['access_token'] as String;
      await saveTokens(access, response.data['refresh_token'] as String?);
      return access;
    } on DioException catch (e) {
      if (isOffline(e)) {
        throw const OfflineException();
      }
      // 400 invalid_grant / 401: el refresh venció o lo revocaron
      throw const SessionExpiredException();
    }
  }

  /// Si el error de Dio es por falta de red (no por respuesta del servidor).
  static bool isOffline(DioException e) =>
      e.error is SocketException ||
      e.type == DioExceptionType.connectionTimeout ||
      e.type == DioExceptionType.connectionError ||
      e.type == DioExceptionType.receiveTimeout ||
      e.type == DioExceptionType.sendTimeout;
}
