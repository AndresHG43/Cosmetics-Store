import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EntryService } from '../../core/services/entry.service';
import { Entry, StatusEntity } from '../../core/models/api.models';

@Component({
  selector: 'app-entry-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="max-w-4xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <a routerLink="/entries" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver a Entradas
        </a>
        @if (entry()) {
          <div class="flex items-center gap-2">
            <button
              (click)="onDelete()"
              [disabled]="isDeleting()"
              class="px-3.5 py-1.5 bg-rose-50 border border-rose-200 text-xs font-medium text-rose-700 hover:bg-rose-100 disabled:opacity-50 rounded-lg transition-colors cursor-pointer"
            >
              {{ isDeleting() ? 'Eliminando...' : 'Eliminar Entrada' }}
            </button>
          </div>
        }
      </div>

      @if (isLoading()) {
        <div class="bg-white rounded-xl border border-[#E8E2DC] p-12 text-center text-[#7E726B]">
          Cargando detalle de la entrada...
        </div>
      } @else if (entry()) {
        <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
          <!-- Header -->
          <div class="p-6 md:p-8 border-b border-[#E8E2DC] flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-[#FAF8F6]">
            <div>
              <div class="flex items-center gap-2">
                <span class="font-mono text-xs text-[#7E726B]">#ENT-{{ entry()?.id }}</span>
                @if (entry()?.isInitial) {
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-[#D2B48C]/30 text-[#362D29]">
                    ✦ Inventario Inicial
                  </span>
                } @else {
                  <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-[#7E726B]">
                    Abastecimiento / Lote
                  </span>
                }
              </div>
              <h2 class="text-2xl font-serif font-bold text-[#A52A2A] mt-1">
                Lote de Entrada #{{ entry()?.id }}
              </h2>
              <p class="text-xs text-[#7E726B] mt-0.5">
                Registrado el {{ (entry()?.date || entry()?.dateCreated) | date:'dd/MM/yyyy HH:mm' }}
              </p>
            </div>

            <div class="text-right">
              <p class="text-xs text-[#7E726B] uppercase font-semibold">Total de Entrada</p>
              <p class="text-3xl font-serif font-bold text-[#362D29]">
                \${{ (entry()?.total || 0) | number:'1.2-2' }}
              </p>
            </div>
          </div>

          <!-- Status & Update Card -->
          <div class="p-6 border-b border-[#E8E2DC] bg-[#FAF8F6]/50">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <p class="text-xs font-semibold uppercase tracking-wider text-[#7E726B] mb-1">Estado Actual</p>
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold"
                      [ngClass]="getStatusBadgeClass(entry()?.status)">
                  {{ getStatusText(entry()?.status) }}
                </span>
              </div>

              <!-- Update Status Section -->
              <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                <label for="statusSelect" class="text-xs font-medium text-[#7E726B]">Cambiar Estado:</label>
                <div class="flex items-center gap-2">
                  <select
                    id="statusSelect"
                    [(ngModel)]="selectedStatusId"
                    class="px-3 py-1.5 bg-white border border-[#E8E2DC] rounded-lg text-xs font-medium text-[#362D29] focus:outline-none focus:ring-1 focus:ring-[#A52A2A]"
                  >
                    @for (st of statuses(); track st.id) {
                      <option [value]="st.id">{{ st.status }}</option>
                    }
                  </select>
                  <button
                    (click)="onUpdateStatus()"
                    [disabled]="isUpdatingStatus() || selectedStatusId === currentStatusId()"
                    class="px-3.5 py-1.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-xs font-medium rounded-lg disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
                  >
                    {{ isUpdatingStatus() ? 'Guardando...' : 'Actualizar' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Details Table -->
          <div class="p-6 md:p-8 space-y-4">
            <h3 class="text-xs font-semibold uppercase tracking-wider text-[#7E726B]">
              Productos y Cantidades ({{ (entry()?.details || []).length }})
            </h3>

            <div class="overflow-x-auto border border-[#E8E2DC] rounded-lg">
              <table class="w-full text-left text-sm text-[#362D29]">
                <thead class="bg-[#FAF8F6] border-b border-[#E8E2DC] text-xs uppercase text-[#7E726B] tracking-wider font-semibold">
                  <tr>
                    <th class="py-2.5 px-4">Producto</th>
                    <th class="py-2.5 px-4 text-center">Cantidad</th>
                    <th class="py-2.5 px-4 text-right">Precio Entrada</th>
                    <th class="py-2.5 px-4 text-right">Subtotal</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#E8E2DC]">
                  @for (item of entry()?.details; track item.productId) {
                    <tr class="hover:bg-[#FAF8F6]/60">
                      <td class="py-3 px-4">
                        <span class="font-medium text-[#362D29]">
                          {{ item.productName || 'Producto #' + item.productId }}
                        </span>
                        <span class="block text-xs font-mono text-[#7E726B]">ID: {{ item.productId }}</span>
                      </td>
                      <td class="py-3 px-4 text-center font-medium">
                        {{ item.quantity }}
                      </td>
                      <td class="py-3 px-4 text-right text-[#7E726B]">
                        \${{ (item.priceEntry ?? item.unitPrice ?? 0) | number:'1.2-2' }}
                      </td>
                      <td class="py-3 px-4 text-right font-semibold">
                        \${{ (item.subtotal ?? ((item.priceEntry ?? item.unitPrice ?? 0) * item.quantity)) | number:'1.2-2' }}
                      </td>
                    </tr>
                  }
                </tbody>
                <tfoot class="bg-[#FAF8F6] font-semibold border-t border-[#E8E2DC]">
                  <tr>
                    <td colspan="3" class="py-3 px-4 text-right text-xs uppercase text-[#7E726B]">Total</td>
                    <td class="py-3 px-4 text-right font-serif text-base text-[#A52A2A]">
                      \${{ (entry()?.total || 0) | number:'1.2-2' }}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class EntryDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private entryService = inject(EntryService);

  entry = signal<Entry | null>(null);
  statuses = signal<StatusEntity[]>([]);
  selectedStatusId: number | null = null;
  currentStatusId = signal<number | null>(null);

  isLoading = signal(true);
  isUpdatingStatus = signal(false);
  isDeleting = signal(false);

  ngOnInit(): void {
    this.loadStatuses();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadEntry(id);
    }
  }

  loadStatuses(): void {
    this.entryService.getStatuses().subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          this.statuses.set(resp.data);
        }
      }
    });
  }

  loadEntry(id: number): void {
    this.isLoading.set(true);
    this.entryService.getById(id).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          const e = resp.data;
          this.entry.set(e);
          const stId = typeof e.status === 'object' && e.status ? e.status.id : null;
          this.selectedStatusId = stId;
          this.currentStatusId.set(stId);
        }
      },
      error: () => this.isLoading.set(false)
    });
  }

  onUpdateStatus(): void {
    const ent = this.entry();
    if (!ent || !ent.id || !this.selectedStatusId) return;

    this.isUpdatingStatus.set(true);
    this.entryService.updateStatus(ent.id, { statusId: Number(this.selectedStatusId) }).subscribe({
      next: (resp) => {
        this.isUpdatingStatus.set(false);
        if (resp.success && resp.data) {
          this.entry.set(resp.data);
          const stId = typeof resp.data.status === 'object' && resp.data.status ? resp.data.status.id : null;
          this.selectedStatusId = stId;
          this.currentStatusId.set(stId);
          alert('Estado de la entrada actualizado exitosamente.');
        }
      },
      error: (err) => {
        this.isUpdatingStatus.set(false);
        alert(err.error?.message || 'Error al actualizar el estado de la entrada.');
      }
    });
  }

  onDelete(): void {
    const ent = this.entry();
    if (!ent || !ent.id) return;

    const confirmed = window.confirm(`¿Estás seguro de que deseas eliminar la entrada #ENT-${ent.id}?`);
    if (confirmed) {
      this.isDeleting.set(true);
      this.entryService.delete(ent.id).subscribe({
        next: (resp) => {
          this.isDeleting.set(false);
          if (resp.success) {
            this.router.navigate(['/entries']);
          } else {
            alert(resp.message || 'No se pudo eliminar la entrada.');
          }
        },
        error: (err) => {
          this.isDeleting.set(false);
          alert(err.error?.message || 'Error al eliminar la entrada.');
        }
      });
    }
  }

  getStatusText(status: StatusEntity | string | undefined): string {
    if (!status) return 'DONE';
    if (typeof status === 'string') return status;
    return status.status || 'DONE';
  }

  getStatusBadgeClass(status: StatusEntity | string | undefined): string {
    const text = this.getStatusText(status).toUpperCase();
    if (text.includes('DONE') || text.includes('COMPLET')) {
      return 'bg-emerald-100 text-emerald-800';
    }
    if (text.includes('PENDING') || text.includes('WAIT') || text.includes('PROGRESS')) {
      return 'bg-amber-100 text-amber-800';
    }
    if (text.includes('CANCEL') || text.includes('FAIL') || text.includes('DELETE')) {
      return 'bg-rose-100 text-rose-800';
    }
    return 'bg-blue-100 text-blue-800';
  }
}
