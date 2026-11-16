import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { SaleService } from '../../core/services/sale.service';
import { ProductService } from '../../core/services/product.service';
import { Product, StatusEntity, SaleCreateRequest } from '../../core/models/api.models';

@Component({
  selector: 'app-sale-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="max-w-4xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">Nueva Venta</h2>
          <p class="text-sm text-[#7E726B]">Registra una orden de venta o pedido de mostrador</p>
        </div>
        <a routerLink="/sales" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver
        </a>
      </div>

      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs p-6 md:p-8">
        @if (errorMessage()) {
          <div class="mb-6 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm">
            {{ errorMessage() }}
          </div>
        }

        <form [formGroup]="saleForm" (ngSubmit)="onSubmit()" class="space-y-6">
          <!-- Header controls: Date, Status -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 p-4 bg-[#FAF8F6] rounded-xl border border-[#E8E2DC]">
            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                Fecha de Venta (Opcional)
              </label>
              <input
                type="datetime-local"
                formControlName="date"
                class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
              />
              <p class="text-[11px] text-[#7E726B] mt-1">Si está vacío y el estado es PENDING DELIVERY o DONE, se autoasigna la fecha actual.</p>
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                Estado de la Venta *
              </label>
              <select
                formControlName="statusId"
                class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
              >
                @for (st of statuses(); track st.id) {
                  <option [value]="st.id">{{ st.status }}</option>
                }
                @if (statuses().length === 0) {
                  <option [value]="1">IN PROGRESS</option>
                  <option [value]="2">PENDING DELIVERY</option>
                  <option [value]="3">DONE</option>
                }
              </select>
              <p class="text-[11px] text-[#7E726B] mt-1">Define el ciclo de entrega y confirmación de la venta.</p>
            </div>
          </div>

          <!-- Items list -->
          <div>
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-semibold uppercase tracking-wider text-[#7E726B]">
                Detalle de Productos a Vender
              </h3>
              <button type="button" (click)="addItem()"
                      class="text-xs text-[#A52A2A] hover:underline font-semibold cursor-pointer">
                + Añadir Producto
              </button>
            </div>

            <!-- Column headers for desktop -->
            <div class="hidden sm:grid grid-cols-12 gap-3 px-3 py-1.5 text-xs uppercase font-semibold text-[#7E726B]">
              <div class="col-span-6">Producto</div>
              <div class="col-span-3">Cantidad</div>
              <div class="col-span-2">Precio de Venta ($)</div>
              <div class="col-span-1 text-center">Quitar</div>
            </div>

            <div formArrayName="details" class="space-y-3">
              @for (item of details.controls; track $index; let i = $index) {
                <div [formGroupName]="i" class="p-3 bg-[#FAF8F6] rounded-lg border border-[#E8E2DC] space-y-2 sm:space-y-0 sm:grid sm:grid-cols-12 sm:gap-3 sm:items-center">
                  <div class="sm:col-span-6">
                    <label class="block sm:hidden text-xs font-semibold text-[#7E726B] mb-1">Producto</label>
                    <select formControlName="productId" (change)="onProductChange(i)"
                            class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm">
                      <option value="" disabled>Selecciona producto</option>
                      @for (p of availableProducts(); track p.id) {
                        <option [value]="p.id">{{ p.name }} (Stock: {{ p.stock || 0 }})</option>
                      }
                    </select>
                  </div>
                  <div class="sm:col-span-3">
                    <label class="block sm:hidden text-xs font-semibold text-[#7E726B] mb-1">Cantidad</label>
                    <input type="number" formControlName="quantity" placeholder="Cant." min="1"
                           class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm" />
                  </div>
                  <div class="sm:col-span-2">
                    <label class="block sm:hidden text-xs font-semibold text-[#7E726B] mb-1">Precio de Venta ($)</label>
                    <input type="number" step="0.01" formControlName="priceSale" placeholder="Precio" min="0.01"
                           class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm" />
                  </div>
                  <div class="sm:col-span-1 text-right sm:text-center">
                    <button type="button" (click)="removeItem(i)" class="text-rose-600 hover:text-rose-800 p-2 cursor-pointer font-bold" title="Eliminar fila">
                      ✕
                    </button>
                  </div>
                </div>
              }
            </div>
          </div>

          <div class="pt-4 border-t border-[#E8E2DC] flex items-center justify-end gap-3">
            <a routerLink="/sales" class="px-5 py-2.5 text-sm font-medium text-[#7E726B] hover:text-[#362D29]">
              Cancelar
            </a>
            <button
              type="submit"
              [disabled]="saleForm.invalid || isSubmitting()"
              class="px-6 py-2.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors disabled:opacity-50 cursor-pointer"
            >
              @if (isSubmitting()) { Procesando... } @else { Registrar Venta }
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class SaleFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private saleService = inject(SaleService);
  private productService = inject(ProductService);
  private router = inject(Router);

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);
  availableProducts = signal<Product[]>([]);
  statuses = signal<StatusEntity[]>([]);

  saleForm = this.fb.group({
    date: [''],
    statusId: [1, Validators.required],
    details: this.fb.array([])
  });

  get details(): FormArray {
    return this.saleForm.get('details') as FormArray;
  }

  ngOnInit(): void {
    this.loadStatuses();
    this.loadProducts();
    this.addItem();
  }

  loadStatuses(): void {
    this.saleService.getStatuses().subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          this.statuses.set(resp.data);
          if (resp.data.length > 0 && !this.saleForm.get('statusId')?.value) {
            this.saleForm.patchValue({ statusId: resp.data[0].id });
          }
        }
      }
    });
  }

  loadProducts(): void {
    this.productService.getAll({ size: 100, active: true }).subscribe(resp => {
      if (resp.success && resp.data) {
        const content = resp.data.content || [];
        if (content.length === 0) {
          this.availableProducts.set([]);
          return;
        }

        const stockObservables = content.map(p =>
          p.id
            ? this.productService.getStock(p.id).pipe(
                map(stockResp => ({
                  ...p,
                  stock: stockResp.success && stockResp.data ? stockResp.data.stock : 0
                })),
                catchError(() => of({ ...p, stock: 0 }))
              )
            : of({ ...p, stock: 0 })
        );

        forkJoin(stockObservables).subscribe({
          next: (productsWithStock) => {
            this.availableProducts.set(productsWithStock);
          },
          error: () => {
            this.availableProducts.set(content);
          }
        });
      }
    });
  }

  addItem(): void {
    this.details.push(this.fb.group({
      productId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      priceSale: [null, [Validators.required, Validators.min(0.01)]]
    }));
  }

  removeItem(index: number): void {
    if (this.details.length > 1) {
      this.details.removeAt(index);
    }
  }

  onProductChange(index: number): void {
    const row = this.details.at(index);
    const prodId = Number(row.get('productId')?.value);
    const selected = this.availableProducts().find(p => p.id === prodId);
    if (selected) {
      row.patchValue({ priceSale: selected.price });
    }
  }

  onSubmit(): void {
    if (this.saleForm.invalid) {
      this.errorMessage.set('Por favor completa todos los campos requeridos correctamente.');
      return;
    }

    if (this.details.length === 0) {
      this.errorMessage.set('Debes agregar al menos un producto a la venta.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const val = this.saleForm.value;

    const payload: SaleCreateRequest = {
      date: val.date ? `${val.date}:00.000` : null,
      statusId: Number(val.statusId),
      details: (val.details as any[]).map(d => ({
        productId: Number(d.productId),
        quantity: Number(d.quantity),
        priceSale: Number(d.priceSale)
      }))
    };

    this.saleService.create(payload).subscribe({
      next: (resp) => {
        this.isSubmitting.set(false);
        if (resp.success) {
          this.router.navigate(['/sales']);
        } else {
          this.errorMessage.set(resp.message || 'No se pudo registrar la venta.');
        }
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err.error?.message || 'Error al conectar con el servidor.');
      }
    });
  }
}
