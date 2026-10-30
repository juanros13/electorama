import Swal from 'sweetalert2';

/**
 * Helpers de diálogos (SweetAlert2) estandarizados para todo el negocio.
 * Reemplaza el `SwalTypes`/`@fuse/confirmation` del proyecto viejo.
 */

const PRIMARY = '#1565C0';

/** Confirmación sí/no. Resuelve true si el usuario confirma. */
export function confirmDialog(title: string, text?: string): Promise<boolean> {
  return Swal.fire({
    title,
    text,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Sí',
    cancelButtonText: 'Cancelar',
    confirmButtonColor: PRIMARY,
  }).then((result) => result.isConfirmed);
}

/** Spinner bloqueante mientras dura una operación. Cerrar con {@link closeLoading}. */
export function showLoading(
  title = 'Procesando…',
  text = 'Por favor espera…'
): void {
  Swal.fire({
    title,
    text,
    allowOutsideClick: false,
    didOpen: () => Swal.showLoading(),
  });
}

export function closeLoading(): void {
  Swal.close();
}

export function showSuccess(title: string, text?: string): Promise<unknown> {
  return Swal.fire({ title, text, icon: 'success', confirmButtonColor: PRIMARY });
}

export function showError(
  title = '¡Ups! Ocurrió un error',
  text = 'Inténtalo de nuevo. Si el problema continúa, comunícate con soporte.'
): Promise<unknown> {
  return Swal.fire({ title, text, icon: 'error', confirmButtonColor: PRIMARY });
}

export function showWarning(title: string, text?: string): Promise<unknown> {
  return Swal.fire({ title, text, icon: 'warning', confirmButtonColor: PRIMARY });
}
