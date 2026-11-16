import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="flex h-screen bg-[#F9F7F5] overflow-hidden text-[#362D29]">
      <!-- Sidebar -->
      <aside class="w-64 bg-white border-r border-[#E8E2DC] flex flex-col justify-between shadow-sm">
        <div>
          <!-- Boutique Brand -->
          <div class="p-6 border-b border-[#E8E2DC]">
            <h1 class="text-2xl font-serif text-[#A52A2A] tracking-wider flex items-center gap-2">
              <span>✦</span> BOUTIQUE
            </h1>
            <p class="text-xs text-[#7E726B] tracking-widest uppercase mt-1">Cosmetics Management</p>
          </div>

          <!-- Nav Links -->
          <nav class="p-4 space-y-1.5 font-medium text-sm">
            <a routerLink="/dashboard" routerLinkActive="bg-[#A52A2A]/10 text-[#A52A2A] font-semibold"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#362D29] hover:bg-[#F9F7F5] transition-colors">
              <span class="text-lg">📊</span> Dashboard
            </a>
            <a routerLink="/products" routerLinkActive="bg-[#A52A2A]/10 text-[#A52A2A] font-semibold"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#362D29] hover:bg-[#F9F7F5] transition-colors">
              <span class="text-lg">💄</span> Catálogo de Productos
            </a>
            <a routerLink="/entries" routerLinkActive="bg-[#A52A2A]/10 text-[#A52A2A] font-semibold"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#362D29] hover:bg-[#F9F7F5] transition-colors">
              <span class="text-lg">📦</span> Entradas / Compras
            </a>
            <a routerLink="/sales" routerLinkActive="bg-[#A52A2A]/10 text-[#A52A2A] font-semibold"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#362D29] hover:bg-[#F9F7F5] transition-colors">
              <span class="text-lg">🛍️</span> Ventas
            </a>

            @if (authService.isAdmin()) {
              <div class="pt-4 pb-1 px-3 text-xs font-semibold text-[#7E726B] tracking-wider uppercase">
                Administración
              </div>
              <a routerLink="/users" routerLinkActive="bg-[#A52A2A]/10 text-[#A52A2A] font-semibold"
                 class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#362D29] hover:bg-[#F9F7F5] transition-colors">
                <span class="text-lg">👥</span> Usuarios
              </a>
              <a routerLink="/roles" routerLinkActive="bg-[#A52A2A]/10 text-[#A52A2A] font-semibold"
                 class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#362D29] hover:bg-[#F9F7F5] transition-colors">
                <span class="text-lg">🛡️</span> Roles & Permisos
              </a>
            }
          </nav>
        </div>

        <!-- User profile & Logout -->
        <div class="p-4 border-t border-[#E8E2DC] bg-[#FAF8F6]">
          <div class="flex items-center justify-between">
            <div class="truncate pr-2">
              <p class="text-xs text-[#7E726B]">Conectado como</p>
              <p class="text-sm font-medium text-[#362D29] truncate">{{ authService.currentUser()?.email || 'Usuario' }}</p>
            </div>
            <button (click)="logout()" title="Cerrar sesión"
                    class="p-2 text-[#7E726B] hover:text-[#A52A2A] hover:bg-white rounded-lg border border-transparent hover:border-[#E8E2DC] transition-all">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </div>
      </aside>

      <!-- Main Content Container -->
      <div class="flex-1 flex flex-col overflow-hidden">
        <!-- Top Header -->
        <header class="h-16 bg-white border-b border-[#E8E2DC] flex items-center justify-between px-8">
          <div class="flex items-center gap-2">
            <span class="text-sm text-[#7E726B]">Cosmetics Store System</span>
          </div>
          <div class="flex items-center gap-4">
            <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-[#A52A2A]/10 text-[#A52A2A]">
              ● Sistema Conectado
            </span>
          </div>
        </header>

        <!-- Page View Outlet -->
        <main class="flex-1 overflow-y-auto p-8">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `
})
export class MainLayoutComponent {
  authService = inject(AuthService);

  logout(): void {
    this.authService.logout();
    window.location.href = '/login';
  }
}
