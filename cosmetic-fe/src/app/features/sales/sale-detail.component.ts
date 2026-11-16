import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SaleService } from '../../core/services/sale.service';
import { Sale, StatusEntity } from '../../core/models/api.models';

@Component({
  selector: 'app-sale-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="max-w-4xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <a routerLink="/sales" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver a Ventas
        </a>
        @if (sale()) {
          <div class="flex items-center gap-2">
            <button
              (click)="onDelete()"
              [disabled]="isDeleting()"
              class="px-3.5 py-1.5 bg-rose-50 border border-rose-200 text-xs font-medium text-rose-700 hover:bg-rose-100 disabled:opacity-50 rounded-lg transition-colors cursor-pointer"
            >
              {{ isDeleting() ? 'Eliminando...' : 'Eliminar Venta' }}
            </button>
          </div>
        }
      </div>

      @if (isLoading()) {
        <div class="bg-white rounded-xl border border-[#E8E2DC] p-12 text-center text-[#7E726B]">
          Cargando detalle de la venta...
        </div>
      } @else if (sale()) {
        <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
          <!-- Header -->
          <div class="p-6 md:p-8 border-b border-[#E8E2DC] flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-[#FAF8F6]">
            <div>
              <span class="font-mono text-xs text-[#7E726B]">#FAC-{{ sale()?.id }}</span>
              <h2 class="text-2xl font-serif font-bold text-[#A52A2A] mt-1">
                Comprobante de Venta #{{ sale()?.id }}
              </h2>
              <p class="text-xs text-[#7E726B] mt-0.5">
                Registrado el {{ (sale()?.date || sale()?.dateCreated) | date:'dd/MM/yyyy HH:mm' }}
              </p>
            </div>

            <div class="text-right">
              <p class="text-xs text-[#7E726B] uppercase font-semibold">Total Venta</p>
              <p class="text-3xl font-serif font-bold text-[#362D29]">
                \${{ (sale()?.total || 0) | number:'1.2-2' }}
              </p>
            </div>
          </div>

          <!-- Status & Update Card -->
          <div class="p-6 border-b border-[#E8E2DC] bg-[#FAF8F6]/50">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <p class="text-xs font-semibold uppercase tracking-wider text-[#7E726B] mb-1">Estado de la Venta</p>
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold"
                      [ngClass]="getStatusBadgeClass(sale()?.status)">
                  {{ getStatusText(sale()?.status) }}
                </span>
              </div>

              <!-- Update Status Section -->
              <div class="flex flex-col sm:flex-row sm:items-center gap-2">
                <label for="saleStatusSelect" class="text-xs font-medium text-[#7E726B]">Cambiar Estado:</label>
                <div class="flex items-center gap-2">
                  <select
                    id="saleStatusSelect"
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
              Artículos Vendidos ({{ (sale()?.details || []).length }})
            </h3>

            <div class="overflow-x-auto border border-[#E8E2DC] rounded-lg">
              <table class="w-full text-left text-sm text-[#362D29]">
                <thead class="bg-[#FAF8F6] border-b border-[#E8E2DC] text-xs uppercase text-[#7E726B] tracking-wider font-semibold">
                  <tr>
                    <th class="py-2.5 px-4">Producto</th>
                    <th class="py-2.5 px-4 text-center">Cantidad</th>
                    <th class="py-2.5 px-4 text-right">Precio Venta</th>
                    <th class="py-2.5 px-4 text-right">Subtotal</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#E8E2DC]">
                  @for (item of sale()?.details; track item.productId) {
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
                        \${{ (item.priceSale ?? item.unitPrice ?? 0) | number:'1.2-2' }}
                      </td>
                      <td class="py-3 px-4 text-right font-semibold">
                        \${{ (item.subtotal ?? ((item.priceSale ?? item.unitPrice ?? 0) * item.quantity)) | number:'1.2-2' }}
                      </td>
                    </tr>
                  }
                </tbody>
                <tfoot class="bg-[#FAF8F6] font-semibold border-t border-[#E8E2DC]">
                  <tr>
                    <td colspan="3" class="py-3 px-4 text-right text-xs uppercase text-[#7E726B]">Total</td>
                    <td class="py-3 px-4 text-right font-serif text-base text-[#A52A2A]">
                      \${{ (sale()?.total || 0) | number:'1.2-2' }}
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
export class SaleDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private saleService = inject(SaleService);

  sale = signal<Sale | null>(null);
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
      this.loadSale(id);
    }
  }

  loadStatuses(): void {
    this.saleService.getStatuses().subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          this.statuses.set(resp.data);
        }
      }
    });
  }

  loadSale(id: number): void {
    this.isLoading.set(true);
    this.saleService.getById(id).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          const s = resp.data;
          this.sale.set(s);
          const stId = typeof s.status === 'object' && s.status ? s.status.id : null;
          this.selectedStatusId = stId;
          this.currentStatusId.set(stId);
        }
      },
      error: () => this.isLoading.set(false)
    });
  }

  onUpdateStatus(): void {
    const s = this.sale();
    if (!s || !s.id || !this.selectedStatusId) return;

    this.isUpdatingStatus.set(true);
    this.saleService.updateStatus(s.id, { statusId: Number(this.selectedStatusId) }).subscribe({
      next: (resp) => {
        this.isUpdatingStatus.set(false);
        if (resp.success && resp.data) {
          this.sale.set(resp.data);
          const stId = typeof resp.data.status === 'object' && resp.data.status ? resp.data.status.id : null;
          this.selectedStatusId = stId;
          this.currentStatusId.set(stId);
          alert('Estado de la venta actualizado exitosamente.');
        }
      },
      error: (err) => {
        this.isUpdatingStatus.set(false);
        alert(err.error?.message || 'Error al actualizar el estado de la venta.');
      }
    });
  }

  onDelete(): void {
    const s = this.sale();
    if (!s || !s.id) return;

    const confirmed = window.confirm(`¿Estás seguro de que deseas eliminar la venta #FAC-${s.id}?`);
    if (confirmed) {
      this.isDeleting.set(true);
      this.saleService.delete(s.id).subscribe({
        next: (resp) => {
          this.isDeleting.set(false);
          if (resp.success) {
            this.router.navigate(['/sales']);
          } else {
            alert(resp.message || 'No se pudo eliminar la venta.');
          }
        },
        error: (err) => {
          this.isDeleting.set(false);
          alert(err.error?.message || 'Error al eliminar la venta.');
        }
      });
    }
  }

  getStatusText(status: StatusEntity | string | undefined): string {
    if (!status) return 'COMPLETADO';
    if (typeof status === 'string') return status;
    return status.status || 'COMPLETADO';
  }

  getStatusBadgeClass(status: StatusEntity | string | undefined): string {
    const text = this.getStatusText(status).toUpperCase();
    if (text.includes('DELIVERED') || text.includes('DONE') || text.includes('COMPLET')) {
      return 'bg-emerald-100 text-emerald-800';
    }
    if (text.includes('PENDING') || text.includes('PROGRESS')) {
      return 'bg-amber-100 text-amber-800';
    }
    if (text.includes('CANCEL') || text.includes('FAIL') || text.includes('DELETE')) {
      return 'bg-rose-100 text-rose-800';
    }
    return 'bg-blue-100 text-blue-800';
  }
}
