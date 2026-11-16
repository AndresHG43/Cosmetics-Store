import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RoleService } from '../../core/services/admin.service';
import { Role } from '../../core/models/api.models';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">Roles & Permisos</h2>
          <p class="text-sm text-[#7E726B]">Perfiles de autorización y permisos de negocio</p>
        </div>
      </div>

      @if (isLoading()) {
        <div class="p-12 text-center text-[#7E726B]">Cargando roles...</div>
      } @else {
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          @for (role of roles(); track role.id) {
            <div class="bg-white p-6 rounded-xl border border-[#E8E2DC] shadow-xs flex items-start justify-between">
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-xl">🛡️</span>
                  <h3 class="text-lg font-serif font-bold text-[#362D29]">{{ role.name }}</h3>
                </div>
                <p class="text-sm text-[#7E726B] mt-2">
                  {{ role.description || 'Perfil de seguridad y acceso operativo en el CRM.' }}
                </p>
              </div>
              <span class="px-2.5 py-1 text-xs rounded-full bg-[#A52A2A]/10 text-[#A52A2A] font-medium">
                Activo
              </span>
            </div>
          }
        </div>

        <!-- Pagination -->
        <div class="p-4 rounded-xl border border-[#E8E2DC] flex items-center justify-between bg-white text-xs text-[#7E726B]">
          <div>
            Página <span class="font-bold text-[#362D29]">{{ currentPage() + 1 }}</span> de
            <span class="font-bold text-[#362D29]">{{ totalPages() || 1 }}</span>
            (Total: <span class="font-bold text-[#362D29]">{{ totalElements() }}</span> roles)
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
  `
})
export class RoleListComponent implements OnInit {
  private roleService = inject(RoleService);

  roles = signal<Role[]>([]);
  isLoading = signal(true);

  currentPage = signal(0);
  pageSize = signal(20);
  totalPages = signal(0);
  totalElements = signal(0);
  isFirst = signal(true);
  isLast = signal(true);

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(page = 0): void {
    this.isLoading.set(true);
    this.roleService.getAll({
      page,
      size: this.pageSize(),
      sort: 'id,desc'
    }).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          const pageData = resp.data;
          this.roles.set(pageData.content || []);
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
      this.loadRoles(page);
    }
  }
}
