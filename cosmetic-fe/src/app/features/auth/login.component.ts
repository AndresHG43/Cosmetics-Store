import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="w-full max-w-md bg-white rounded-2xl shadow-xl shadow-[#A52A2A]/5 border border-[#E8E2DC] p-8 md:p-10 text-center">
      <!-- Boutique Logo / Header -->
      <div class="mb-6">
        <span class="text-4xl text-[#A52A2A]">✦</span>
        <h1 class="text-3xl font-serif text-[#A52A2A] mt-2 tracking-wide">Boutique</h1>
        <p class="text-sm text-[#7E726B] mt-1">Gestión de Cosméticos & Inventario</p>
      </div>

      @if (errorMessage()) {
        <div class="mb-6 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm text-left flex items-start gap-2">
          <span>⚠️</span>
          <span>{{ errorMessage() }}</span>
        </div>
      }

      <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="text-left space-y-4">
        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
            Correo Electrónico
          </label>
          <input
            type="email"
            formControlName="email"
            placeholder="user@cosmetic.com"
            class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-[#362D29] placeholder-[#A89F98] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A] transition-all text-sm"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
            Contraseña
          </label>
          <input
            type="password"
            formControlName="password"
            placeholder="••••••••"
            class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-[#362D29] placeholder-[#A89F98] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A] transition-all text-sm"
          />
        </div>

        <button
          type="submit"
          [disabled]="loginForm.invalid || isLoading()"
          class="w-full mt-6 py-3 px-4 bg-[#A52A2A] hover:bg-[#8B2323] text-white font-medium rounded-lg shadow-md hover:shadow-lg transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
        >
          @if (isLoading()) {
            <span class="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
            <span>Verificando...</span>
          } @else {
            <span>Iniciar Sesión</span>
          }
        </button>
      </form>

      <div class="mt-8 pt-6 border-t border-[#E8E2DC] text-xs text-[#7E726B]">
        CRM Boutique &bull; Conexión Segura con Spring Boot API
      </div>
    </div>
  `
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(4)]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { email, password } = this.loginForm.value;
    this.authService.login({ email: email!, password: password! }).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success) {
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/products';
          this.router.navigateByUrl(returnUrl);
        } else {
          this.errorMessage.set(resp.message || 'Credenciales incorrectas');
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Error al conectar con el servidor.');
      }
    });
  }
}
