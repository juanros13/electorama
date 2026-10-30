class User {

  final String authId;
  final String email;
  final String firstName;
  final String lastName;
  final List<String> roles;
  final String token;
  final String authIdReference;

  User({
    required this.authId,
    required this.email,
    required this.firstName,
    required this.lastName,
    required this.roles,
    required this.token,
    this.authIdReference = ''
  });
  User copyWith({
    String? authId,
    String? email,
    String? firstName,
    String? lastName,
    List<String>? roles,
    String? token,
    String? authIdReference,
  }) => User(
    authId: authId ?? this.authId,
    email: email ?? this.email,
    firstName: firstName ?? this.firstName,
    lastName: lastName ?? this.lastName,
    roles: roles ?? this.roles,
    token: token ?? this.token,
    authIdReference: authIdReference ?? this.authIdReference
  );

  bool get isAdmin {
    return roles.contains('admin');
  }

}
