import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UserService } from '../../core/services/admin.service';
import { User } from '../../core/models/api.models';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="max-w-3xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <a routerLink="/users" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver a usuarios
        </a>
        @if (user()) {
          <div class="flex items-center gap-2">
            <a [routerLink]="['/users/edit', user()?.id]"
               class="px-3.5 py-1.5 bg-white border border-[#E8E2DC] text-xs font-medium text-[#362D29] hover:bg-[#FAF8F6] rounded-lg transition-colors">
              Editar
            </a>
            <button (click)="onDelete()"
                    class="px-3.5 py-1.5 bg-rose-50 border border-rose-200 text-xs font-medium text-rose-700 hover:bg-rose-100 rounded-lg transition-colors cursor-pointer">
              Eliminar
            </button>
          </div>
        }
      </div>

      @if (isLoading()) {
        <div class="bg-white rounded-xl border border-[#E8E2DC] p-12 text-center text-[#7E726B]">
          Cargando detalle del usuario...
        </div>
      } @else if (user()) {
        <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
          <div class="p-6 md:p-8 border-b border-[#E8E2DC] flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-[#FAF8F6]">
            <div>
              <span class="text-xs font-mono text-[#7E726B]">#USR-{{ user()?.id }}</span>
              <h2 class="text-2xl font-serif font-bold text-[#A52A2A] mt-1">
                {{ user()?.name }} {{ user()?.lastname }}
              </h2>
              <p class="text-sm text-[#7E726B]">{{ user()?.email }}</p>
            </div>
            <div>
              <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold"
                    [ngClass]="user()?.active ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-100 text-gray-800'">
                {{ user()?.active ? 'Cuenta Activa' : 'Cuenta Inactiva' }}
              </span>
            </div>
          </div>

          <div class="p-6 md:p-8 space-y-6">
            <div>
              <h3 class="text-xs font-semibold uppercase tracking-wider text-[#7E726B] mb-3">Roles y Permisos</h3>
              <div class="flex flex-wrap gap-2">
                @for (role of user()?.roles; track role.id) {
                  <span class="px-3 py-1 rounded-lg bg-[#D2B48C]/30 text-[#362D29] text-xs font-medium">
                    {{ role.name }}
                  </span>
                }
                @if ((user()?.roles || []).length === 0) {
                  <p class="text-sm text-[#7E726B] italic">No tiene roles asignados</p>
                }
              </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-4 border-t border-[#E8E2DC]">
              <div class="bg-[#FAF8F6] p-4 rounded-lg">
                <p class="text-xs text-[#7E726B] uppercase font-semibold">Fecha de Creación</p>
                <p class="text-sm font-medium text-[#362D29] mt-1">
                  {{ (user()?.dateCreated | date:'dd/MM/yyyy HH:mm') || 'No disponible' }}
                </p>
              </div>
              <div class="bg-[#FAF8F6] p-4 rounded-lg">
                <p class="text-xs text-[#7E726B] uppercase font-semibold">Última Actualización</p>
                <p class="text-sm font-medium text-[#362D29] mt-1">
                  {{ (user()?.dateUpdated | date:'dd/MM/yyyy HH:mm') || 'Sin modificaciones' }}
                </p>
              </div>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class UserDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private userService = inject(UserService);

  user = signal<User | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadUser(id);
    }
  }

  loadUser(id: number): void {
    this.isLoading.set(true);
    this.userService.getById(id).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          this.user.set(resp.data);
        }
      },
      error: () => this.isLoading.set(false)
    });
  }

  onDelete(): void {
    const u = this.user();
    if (!u || !u.id) return;

    const confirmed = window.confirm(`¿Estás seguro de que deseas eliminar al usuario ${u.email} (#${u.id})?`);
    if (confirmed) {
      this.userService.delete(u.id).subscribe({
        next: (resp) => {
          if (resp.success) {
            this.router.navigate(['/users']);
          } else {
            alert(resp.message || 'No se pudo eliminar el usuario.');
          }
        },
        error: (err) => {
          alert(err.error?.message || 'Error al conectar con el servidor.');
        }
      });
    }
  }
}
