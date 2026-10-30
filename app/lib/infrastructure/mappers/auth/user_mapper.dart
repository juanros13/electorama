import 'package:jwt_decoder/jwt_decoder.dart';

import '../../../domain/entities/auth/user.dart';

class UserMapper {


  static User userJsonToEntity(String jwtToken ){
    Map<String, dynamic>? decodedToken = JwtDecoder.decode(jwtToken);

    
    List<dynamic> roles = decodedToken['realm_access']['roles'];
    List<String> rolesAsString = roles.map((role) => role.toString()).toList();

    // Accede a la información del token decodificado
    return User(
      authId: decodedToken['sub'], 
      email: decodedToken['email'], 
      firstName: decodedToken['given_name'], 
      lastName: decodedToken['family_name']??'', 
      roles: rolesAsString, 
      token: jwtToken
    );
  }

}

