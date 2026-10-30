import { AuthService } from '@/app/core/auth/auth.service';
import { Component, inject, signal } from '@angular/core';
import {
  email,
  form,
  FormField,
  required,
} from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'auth-sign-in',
  templateUrl: './sign-in.html',
  imports: [
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    FormField,
  ],
})
export default class AuthSignIn {
  // Dependencies
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  // State
  protected loading = signal(false);
  protected errorMessage = signal<string | null>(null);

  protected signInFormModel = signal({
    email: '',
    password: '',
  });
  protected signInForm = form(this.signInFormModel, (form) => {
    required(form.email, { message: 'Debes ingresar un correo electrónico' });
    email(form.email, { message: 'Ingresa un correo electrónico válido' });
    required(form.password, { message: 'Debes ingresar una contraseña' });
  });

  signIn(event: Event) {
    event.preventDefault();

    if (this.signInForm().invalid() || this.loading()) {
      return;
    }

    this.errorMessage.set(null);
    this.loading.set(true);

    const { email, password } = this.signInFormModel();

    this.authService.signIn({ email, password }).subscribe({
      next: () => {
        const redirectURL =
          this.route.snapshot.queryParamMap.get('redirectURL') ?? '/admin';
        this.router.navigateByUrl(redirectURL);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set(
          'Correo o contraseña incorrectos. Inténtalo de nuevo.'
        );
      },
    });
  }
}
