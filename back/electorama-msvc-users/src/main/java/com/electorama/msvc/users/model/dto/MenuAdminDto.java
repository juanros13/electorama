package com.electorama.msvc.users.model.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Una entrada de menú tal como la edita la pantalla de accesos.
 *
 * <p>Lleva los roles dentro —no en otra llamada— porque en esa pantalla la entrada y quién la ve
 * son la misma decisión: separarlas obligaría a dos peticiones para cada cambio y a inventar qué
 * hacer si la segunda falla.
 */
@Getter
@Setter
public class MenuAdminDto {

  private Long pk;

  /** Identificador estable del item (el que usa el front del theme). */
  private String id;

  private String title;
  private String subtitle;

  /** Nombre del icono en el set del front. */
  private String icon;

  /** Ruta del admin a la que lleva. Vacía si es un grupo que sólo agrupa. */
  private String link;

  private TypeMenu type;

  private Integer order;

  /** Roles del realm que ven esta entrada. */
  private List<String> roles;
}
