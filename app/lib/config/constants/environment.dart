import 'package:flutter_dotenv/flutter_dotenv.dart';

class Environment {

  /// `flutter run --dart-define=ENV=local` carga `.env.local` (el gateway de
  /// la máquina de desarrollo); sin él, `.env` (producción).
  static const String name = String.fromEnvironment('ENV', defaultValue: 'prod');

  static Future<void> initEnvironment() async {
    await dotenv.load(fileName: name == 'local' ? '.env.local' : '.env');
  }

    static String apiUrl =
      dotenv.env['API_URL'] ?? 'No está configurado el API URL';
  
  static String authId =
      dotenv.env['AUTHID_TEST'] ?? 'No está configurado el Authid';

  static String tokenTest = 
    dotenv.env['TOKEN_TEST'] ?? 'Token no configurado';

  static String urlUser = 
    dotenv.env['URL_USER'] ?? 'UserUrl no configurado';

  static String urlUserProfile = 
    dotenv.env['URL_USER_PROFILE'] ?? 'UrlUserProfile no configurado';

  static String urlCalendar = 
    dotenv.env['URL_CALENDAR'] ?? 'CalendarUrl no configurado';

  static String urlGeoServer = 
    dotenv.env['URL_GEOSERVER'] ?? 'GeoUrl no configurado';
  
  static String urlGeoServerOws = 
    dotenv.env['URL_GEOSERVER_OWS'] ?? 'urlGeoServerOws no configurado';

  static String urlAuthGoogle = 
    dotenv.env['URL_AUTH_GOOGLE'] ?? 'urlAuthGoogle no configurado';

  static String urlGeoElections = 
    dotenv.env['URL_GEO_ELECTIONS'] ?? 'urlGeoElections no configurado';

  static String urlGeography = 
    dotenv.env['URL_GEOGRAPHY'] ?? 'urlGeography no configurado';

  static String urlLog = 
    dotenv.env['URL_LOG'] ?? 'urlLog no configurado';

  static String urlFollowers = 
    dotenv.env['URL_FOLLOWERS'] ?? 'urlFollowers no configurado';

  /// msvc-structure: captura de convencidos y puestos (capture-targets).
  static String urlStructure =
    dotenv.env['URL_STRUCTURE'] ?? 'urlStructure no configurado';

  static String urlCanvasWall =
    dotenv.env['URL_CANVASWALL'] ?? 'urlCanvasWall no configurado';

  static String urlCitizenIssues =
    dotenv.env['URL_CITIZEN_ISSUES'] ?? 'urlCitizenIssues no configurado';

  static String clientId = 
    dotenv.env['CLIENT_ID'] ?? 'clientId no configurado';

  static String clientSecret = 
    dotenv.env['CLIENT_SECRET'] ?? 'clientSecret no configurado';

}