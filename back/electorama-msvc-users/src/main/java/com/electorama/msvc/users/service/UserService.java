package com.electorama.msvc.users.service;

import com.electorama.msvc.users.model.dto.User;
import com.electorama.msvc.users.model.mapper.UserMapper;
import com.electorama.msvc.users.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El perfil de una cuenta: sus datos del modelo viejo (si los tiene) más los de Keycloak. */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final KeycloakUserService keycloak;
  private final UserMapper userMapper = new UserMapper();

  /**
   * Las cuentas creadas para la estructura 2027 no tienen fila en {@code users}: el perfil sale
   * sólo de Keycloak. Si la cuenta no existe en Keycloak, 404.
   */
  @Transactional(readOnly = true)
  public User findByAuthId(String authId) {
    UserRepresentation account = keycloak.readUser(authId);
    User user = userMapper.convertToDto(userRepository.findByAuthId(authId));
    user.setAuthId(authId);
    user.setEnabled(account.isEnabled());
    user.setEmail(account.getEmail());
    user.setFirstName(account.getFirstName());
    user.setLastName(account.getLastName());
    return user;
  }
}
