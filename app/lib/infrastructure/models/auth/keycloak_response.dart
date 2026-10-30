// To parse this JSON data, do
//
//     final keycloakResponse = keycloakResponseFromJson(jsonString);

import 'dart:convert';

KeycloakResponse keycloakResponseFromJson(String str) => KeycloakResponse.fromJson(json.decode(str));

String keycloakResponseToJson(KeycloakResponse data) => json.encode(data.toJson());

class KeycloakResponse {
    final String accessToken;
    final int expiresIn;
    final int refreshExpiresIn;
    final String refreshToken;
    final String tokenType;
    final String idToken;
    final int notBeforePolicy;
    final String sessionState;
    final String scope;

    KeycloakResponse({
        required this.accessToken,
        required this.expiresIn,
        required this.refreshExpiresIn,
        required this.refreshToken,
        required this.tokenType,
        required this.idToken,
        required this.notBeforePolicy,
        required this.sessionState,
        required this.scope,
    });

    factory KeycloakResponse.fromJson(Map<String, dynamic> json) => KeycloakResponse(
        accessToken: json["access_token"],
        expiresIn: json["expires_in"],
        refreshExpiresIn: json["refresh_expires_in"],
        refreshToken: json["refresh_token"],
        tokenType: json["token_type"],
        idToken: json["id_token"],
        notBeforePolicy: json["not-before-policy"],
        sessionState: json["session_state"],
        scope: json["scope"],
    );

    Map<String, dynamic> toJson() => {
        "access_token": accessToken,
        "expires_in": expiresIn,
        "refresh_expires_in": refreshExpiresIn,
        "refresh_token": refreshToken,
        "token_type": tokenType,
        "id_token": idToken,
        "not-before-policy": notBeforePolicy,
        "session_state": sessionState,
        "scope": scope,
    };
}
