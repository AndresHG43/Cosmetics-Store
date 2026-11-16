import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { UserService } from '../../core/services/admin.service';
import { User } from '../../core/models/api.models';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">Usuarios del Sistema</h2>
          <p class="text-sm text-[#7E726B]">Gestión de accesos, roles y cuentas autorizadas</p>
        </div>
        <a routerLink="/users/new"
           class="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
          <span>+</span> Nuevo Usuario
        </a>
      </div>

      <!-- Search Bar -->
      <div class="bg-white p-4 rounded-xl border border-[#E8E2DC] shadow-xs flex items-center gap-3">
        <div class="relative flex-1">
          <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#7E726B]">
            🔍
          </span>
          <input
            type="text"
            [ngModel]="searchTerm"
            (ngModelChange)="onSearchChange($event)"
            placeholder="Buscar usuario automáticamente por correo o nombre..."
            class="w-full pl-9 pr-9 py-2 bg-[#FAF8F6] border border-[#E8E2DC] rounded-lg text-sm text-[#362D29] focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
          />
          @if (searchTerm) {
            <button
              (click)="clearSearch()"
              title="Borrar búsqueda"
              class="absolute inset-y-0 right-0 pr-3 flex items-center text-[#7E726B] hover:text-[#362D29] cursor-pointer"
            >
              ✕
            </button>
          }
        </div>
      </div>

      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
        @if (isLoading()) {
          <div class="p-12 text-center text-[#7E726B]">Cargando usuarios...</div>
        } @else if (users().length === 0) {
          <div class="p-12 text-center text-[#7E726B]">
            <p class="font-medium">No se encontraron usuarios.</p>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm text-[#362D29]">
              <thead class="bg-[#FAF8F6] border-b border-[#E8E2DC] text-xs uppercase text-[#7E726B] tracking-wider font-semibold">
                <tr>
                  <th class="py-3 px-4">ID</th>
                  <th class="py-3 px-4">Nombre y Apellido</th>
                  <th class="py-3 px-4">Correo Electrónico</th>
                  <th class="py-3 px-4">Roles Asignados</th>
                  <th class="py-3 px-4 text-center">Estado</th>
                  <th class="py-3 px-4 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[#E8E2DC]">
                @for (user of users(); track user.id) {
                  <tr class="hover:bg-[#FAF8F6]/60 transition-colors">
                    <td class="py-3.5 px-4 font-mono text-xs text-[#7E726B]">
                      <a [routerLink]="['/users', user.id]" class="hover:text-[#A52A2A] hover:underline">
                        #{{ user.id }}
                      </a>
                    </td>
                    <td class="py-3.5 px-4 font-medium text-[#362D29]">
                      <a [routerLink]="['/users', user.id]" class="hover:text-[#A52A2A] hover:underline">
                        {{ user.name }} {{ user.lastname }}
                      </a>
                    </td>
                    <td class="py-3.5 px-4 text-[#7E726B]">{{ user.email }}</td>
                    <td class="py-3.5 px-4">
                      <div class="flex flex-wrap gap-1">
                        @for (r of user.roles; track r.id) {
                          <span class="px-2 py-0.5 text-xs rounded bg-[#D2B48C]/30 text-[#362D29] font-medium">
                            {{ r.name }}
                          </span>
                        }
                      </div>
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                            [ngClass]="user.active ? 'bg-emerald-100 text-emerald-800' : 'bg-gray-100 text-gray-800'">
                        {{ user.active ? 'Activo' : 'Inactivo' }}
                      </span>
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <div class="inline-flex items-center gap-2">
                        <a [routerLink]="['/users', user.id]" class="text-xs text-[#7E726B] hover:text-[#362D29] font-medium">
                          Detalle
                        </a>
                        <span class="text-[#E8E2DC]">|</span>
                        <a [routerLink]="['/users/edit', user.id]" class="text-xs text-[#A52A2A] hover:underline font-medium">
                          Editar
                        </a>
                        <span class="text-[#E8E2DC]">|</span>
                        <button (click)="confirmDelete(user)" class="text-xs text-rose-600 hover:text-rose-800 font-medium cursor-pointer">
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div class="p-4 border-t border-[#E8E2DC] flex items-center justify-between bg-[#FAF8F6] text-xs text-[#7E726B]">
            <div>
              Página <span class="font-bold text-[#362D29]">{{ currentPage() + 1 }}</span> de
              <span class="font-bold text-[#362D29]">{{ totalPages() || 1 }}</span>
              (Total: <span class="font-bold text-[#362D29]">{{ totalElements() }}</span> usuarios)
            </div>
            <div class="flex items-center gap-2">
              <button
                (click)="goToPage(currentPage() - 1)"
                [disabled]="isFirst()"
                class="px-3 py-1.5 rounded border border-[#E8E2DC] bg-white text-[#362D29] hover:bg-[#F9F7F5] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
              >
                &larr; Anterior
              </button>
              <button
                (click)="goToPage(currentPage() + 1)"
                [disabled]="isLast()"
                class="px-3 py-1.5 rounded border border-[#E8E2DC] bg-white text-[#362D29] hover:bg-[#F9F7F5] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
              >
                Siguiente &rarr;
              </button>
            </div>
          </div>
        }
      </div>
    </div>
  `
})
export class UserListComponent implements OnInit, OnDestroy {
  private userService = inject(UserService);

  users = signal<User[]>([]);
  isLoading = signal(true);

  currentPage = signal(0);
  pageSize = signal(20);
  totalPages = signal(0);
  totalElements = signal(0);
  isFirst = signal(true);
  isLast = signal(true);

  searchTerm = '';
  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  ngOnInit(): void {
    this.searchSubscription = this.searchSubject
      .pipe(
        debounceTime(800),
        distinctUntilChanged()
      )
      .subscribe((term) => {
        this.searchTerm = term;
        this.loadUsers(0);
      });

    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.searchSubject.next(term);
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }

  loadUsers(page = 0): void {
    this.isLoading.set(true);
    this.userService.getAll({
      page,
      size: this.pageSize(),
      sort: 'id,desc',
      search: this.searchTerm.trim() || undefined
    }).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          const pageData = resp.data;
          this.users.set(pageData.content || []);
          this.currentPage.set(pageData.page);
          this.totalPages.set(pageData.totalPages);
          this.totalElements.set(pageData.totalElements);
          this.isFirst.set(pageData.first);
          this.isLast.set(pageData.last);
        }
      },
      error: () => this.isLoading.set(false)
    });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.loadUsers(page);
    }
  }

  confirmDelete(user: User): void {
    if (!user.id) return;
    const confirmed = window.confirm(`¿Estás seguro de que deseas eliminar al usuario ${user.email} (#${user.id})?`);
    if (confirmed) {
      this.userService.delete(user.id).subscribe({
        next: (resp) => {
          if (resp.success) {
            this.loadUsers(this.currentPage());
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
