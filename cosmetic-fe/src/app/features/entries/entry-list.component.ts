import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { EntryService } from '../../core/services/entry.service';
import { Entry, StatusEntity } from '../../core/models/api.models';

@Component({
  selector: 'app-entry-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">Historial de Entradas</h2>
          <p class="text-sm text-[#7E726B]">Recepción de lotes, abastecimiento y compras a proveedores</p>
        </div>
        <a routerLink="/entries/new"
           class="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
          <span>+</span> Registrar Entrada
        </a>
      </div>

      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
        @if (isLoading()) {
          <div class="p-12 text-center text-[#7E726B]">Cargando entradas...</div>
        } @else if (entries().length === 0) {
          <div class="p-12 text-center text-[#7E726B]">
            <span class="text-4xl block mb-2">📦</span>
            <p class="font-medium">No se han registrado entradas de inventario aún.</p>
            <a routerLink="/entries/new" class="text-sm text-[#A52A2A] hover:underline mt-2 inline-block">
              Registrar la primera compra
            </a>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm text-[#362D29]">
              <thead class="bg-[#FAF8F6] border-b border-[#E8E2DC] text-xs uppercase text-[#7E726B] tracking-wider font-semibold">
                <tr>
                  <th class="py-3 px-4">Lote / ID</th>
                  <th class="py-3 px-4">Tipo de Entrada</th>
                  <th class="py-3 px-4">Fecha</th>
                  <th class="py-3 px-4 text-center">Estado</th>
                  <th class="py-3 px-4 text-right">Total</th>
                  <th class="py-3 px-4 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[#E8E2DC]">
                @for (entry of entries(); track entry.id) {
                  <tr class="hover:bg-[#FAF8F6]/60 transition-colors">
                    <td class="py-3.5 px-4 font-mono text-xs text-[#7E726B]">
                      <a [routerLink]="['/entries', entry.id]" class="hover:text-[#A52A2A] hover:underline">
                        #ENT-{{ entry.id }}
                      </a>
                    </td>
                    <td class="py-3.5 px-4 font-medium">
                      @if (entry.isInitial) {
                        <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-[#D2B48C]/30 text-[#362D29]">
                          ✦ Inventario Inicial
                        </span>
                      } @else {
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-[#7E726B]">
                          Abastecimiento / Lote
                        </span>
                      }
                    </td>
                    <td class="py-3.5 px-4 text-[#7E726B]">
                      {{ (entry.date | date:'dd/MM/yyyy HH:mm') || (entry.dateCreated | date:'dd/MM/yyyy HH:mm') || 'Reciente' }}
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                            [ngClass]="getStatusBadgeClass(entry.status)">
                        {{ getStatusText(entry.status) }}
                      </span>
                    </td>
                    <td class="py-3.5 px-4 text-right font-semibold">
                      \${{ (entry.total || 0) | number:'1.2-2' }}
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <div class="inline-flex items-center gap-2">
                        <a [routerLink]="['/entries', entry.id]" class="text-xs text-[#7E726B] hover:text-[#362D29] font-medium">
                          Detalle
                        </a>
                        <span class="text-[#E8E2DC]">|</span>
                        <button
                          (click)="deleteEntry(entry)"
                          class="text-xs text-rose-600 hover:text-rose-800 font-medium cursor-pointer"
                        >
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
              (Total: <span class="font-bold text-[#362D29]">{{ totalElements() }}</span> lotes)
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
export class EntryListComponent implements OnInit {
  private entryService = inject(EntryService);

  entries = signal<Entry[]>([]);
  isLoading = signal(true);

  currentPage = signal(0);
  pageSize = signal(20);
  totalPages = signal(0);
  totalElements = signal(0);
  isFirst = signal(true);
  isLast = signal(true);

  ngOnInit(): void {
    this.loadEntries();
  }

  loadEntries(page = 0): void {
    this.isLoading.set(true);
    this.entryService.getAll({
      page,
      size: this.pageSize(),
      sort: 'id,desc'
    }).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          const pageData = resp.data;
          this.entries.set(pageData.content || []);
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

  getStatusText(status: StatusEntity | string | undefined): string {
    if (!status) return 'COMPLETADO';
    if (typeof status === 'string') return status;
    return status.status || 'DONE';
  }

  getStatusBadgeClass(status: StatusEntity | string | undefined): string {
    const text = this.getStatusText(status).toUpperCase();
    if (text.includes('DONE') || text.includes('COMPLET')) {
      return 'bg-emerald-100 text-emerald-800';
    }
    if (text.includes('PENDING') || text.includes('WAIT')) {
      return 'bg-amber-100 text-amber-800';
    }
    if (text.includes('CANCEL') || text.includes('FAIL')) {
      return 'bg-rose-100 text-rose-800';
    }
    return 'bg-blue-100 text-blue-800';
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.loadEntries(page);
    }
  }

  deleteEntry(entry: Entry): void {
    if (!entry.id) return;
    const confirmed = window.confirm(`¿Estás seguro de que deseas eliminar la entrada #ENT-${entry.id}?`);
    if (confirmed) {
      this.entryService.delete(entry.id).subscribe({
        next: (resp) => {
          if (resp.success) {
            this.loadEntries(this.currentPage());
          } else {
            alert(resp.message || 'No se pudo eliminar la entrada.');
          }
        },
        error: (err) => {
          alert(err.error?.message || 'Error al eliminar la entrada.');
        }
      });
    }
  }
}
