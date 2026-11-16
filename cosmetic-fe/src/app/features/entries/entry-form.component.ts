import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EntryService } from '../../core/services/entry.service';
import { ProductService } from '../../core/services/product.service';
import { Product, StatusEntity, EntryCreateRequest } from '../../core/models/api.models';

@Component({
  selector: 'app-entry-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="max-w-4xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">Nueva Entrada de Inventario</h2>
          <p class="text-sm text-[#7E726B]">Registra un lote de abastecimiento o corte inicial de inventario</p>
        </div>
        <a routerLink="/entries" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver
        </a>
      </div>

      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs p-6 md:p-8">
        @if (errorMessage()) {
          <div class="mb-6 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm">
            {{ errorMessage() }}
          </div>
        }

        <form [formGroup]="entryForm" (ngSubmit)="onSubmit()" class="space-y-6">
          <!-- Header controls: Date, Status, isInitial -->
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 p-4 bg-[#FAF8F6] rounded-xl border border-[#E8E2DC]">
            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                Fecha Operativa (Opcional)
              </label>
              <input
                type="datetime-local"
                formControlName="date"
                class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
              />
              <p class="text-[11px] text-[#7E726B] mt-1">Si está vacío y el estado es DONE, toma la fecha actual.</p>
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                Estado de la Entrada *
              </label>
              <select
                formControlName="statusId"
                class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
              >
                @for (st of statuses(); track st.id) {
                  <option [value]="st.id">{{ st.status }}</option>
                }
                @if (statuses().length === 0) {
                  <option [value]="2">DONE (Completada - Afecta stock)</option>
                  <option [value]="1">IN PROGRESS (En proceso)</option>
                }
              </select>
              <p class="text-[11px] text-[#7E726B] mt-1">Solo entradas DONE suman existencias al stock.</p>
            </div>

            <div class="flex flex-col justify-center">
              <label class="text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-2">
                Tipo de Entrada
              </label>
              <label class="inline-flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  formControlName="isInitial"
                  class="w-4 h-4 text-[#A52A2A] rounded border-[#E8E2DC] focus:ring-[#A52A2A]"
                />
                <span class="text-sm font-medium text-[#362D29]">¿Es Inventario Inicial?</span>
              </label>
              <p class="text-[11px] text-[#7E726B] mt-1">Marca el punto de partida (baseline) del stock.</p>
            </div>
          </div>

          <!-- Line items -->
          <div>
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-semibold uppercase tracking-wider text-[#7E726B]">
                Detalle de Productos Recibidos
              </h3>
              <button type="button" (click)="addItem()"
                      class="text-xs text-[#A52A2A] hover:underline font-semibold cursor-pointer">
                + Añadir Producto
              </button>
            </div>

            <div class="hidden sm:grid grid-cols-12 gap-3 px-3 py-1.5 text-xs uppercase font-semibold text-[#7E726B]">
              <div class="col-span-6">Producto</div>
              <div class="col-span-3">Cantidad</div>
              <div class="col-span-2">Precio de Compra ($)</div>
              <div class="col-span-1 text-center">Quitar</div>
            </div>

            <div formArrayName="details" class="space-y-3">
              @for (item of details.controls; track $index; let i = $index) {
                <div [formGroupName]="i" class="p-3 bg-[#FAF8F6] rounded-lg border border-[#E8E2DC] space-y-2 sm:space-y-0 sm:grid sm:grid-cols-12 sm:gap-3 sm:items-center">
                  <div class="sm:col-span-6">
                    <label class="block sm:hidden text-xs font-semibold text-[#7E726B] mb-1">Producto</label>
                    <select formControlName="productId" class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm">
                      <option value="" disabled>Selecciona un producto</option>
                      @for (p of availableProducts(); track p.id) {
                        <option [value]="p.id">{{ p.name }} (#{{ p.id }})</option>
                      }
                    </select>
                  </div>
                  <div class="sm:col-span-3">
                    <label class="block sm:hidden text-xs font-semibold text-[#7E726B] mb-1">Cantidad</label>
                    <input type="number" formControlName="quantity" placeholder="Cant." min="1"
                           class="w-full px-3 py-2 rounded-lg border border-[#E8E2DC] bg-white text-sm" />
                  </div>
                  <div class="sm:col-span-2">
                    <label class="block sm:hidden text-xs font-semibold text-[#7E726B] mb-1">Precio de Compra ($)</label>
                    <input type="number" step="0.01" formControlName="priceEntry" placeholder="0.00" min="0.01"
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
            <a routerLink="/entries" class="px-5 py-2.5 text-sm font-medium text-[#7E726B] hover:text-[#362D29]">
              Cancelar
            </a>
            <button
              type="submit"
              [disabled]="entryForm.invalid || isSubmitting()"
              class="px-6 py-2.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors disabled:opacity-50 cursor-pointer"
            >
              @if (isSubmitting()) { Guardando... } @else { Confirmar e Ingresar Lote }
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class EntryFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private entryService = inject(EntryService);
  private productService = inject(ProductService);
  private router = inject(Router);

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);
  availableProducts = signal<Product[]>([]);
  statuses = signal<StatusEntity[]>([]);

  entryForm = this.fb.group({
    date: [''],
    isInitial: [false],
    statusId: [2, Validators.required], // Default to 2 (DONE)
    details: this.fb.array([])
  });

  get details(): FormArray {
    return this.entryForm.get('details') as FormArray;
  }

  ngOnInit(): void {
    this.productService.getAll({ size: 100, active: true }).subscribe(resp => {
      if (resp.success && resp.data) {
        this.availableProducts.set(resp.data.content || []);
      }
    });

    this.entryService.getStatuses().subscribe(resp => {
      if (resp.success && resp.data) {
        this.statuses.set(resp.data);
      }
    });

    this.addItem();
  }

  addItem(): void {
    this.details.push(this.fb.group({
      productId: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      priceEntry: [null, [Validators.required, Validators.min(0.01)]]
    }));
  }

  removeItem(index: number): void {
    if (this.details.length > 1) {
      this.details.removeAt(index);
    }
  }

  onSubmit(): void {
    if (this.entryForm.invalid) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const val = this.entryForm.value;

    let formattedDate: string | null = null;
    if (val.date) {
      formattedDate = new Date(val.date).toISOString().replace('Z', '');
    }

    const payload: EntryCreateRequest = {
      date: formattedDate || undefined,
      isInitial: !!val.isInitial,
      statusId: Number(val.statusId),
      details: (val.details as any[]).map(d => ({
        productId: Number(d.productId),
        quantity: Number(d.quantity),
        priceEntry: Number(d.priceEntry)
      }))
    };

    this.entryService.create(payload).subscribe({
      next: (resp) => {
        this.isSubmitting.set(false);
        if (resp.success) {
          this.router.navigate(['/entries']);
        } else {
          this.errorMessage.set(resp.message || 'Error al registrar la entrada');
        }
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err.error?.message || 'Error al conectar con el servidor.');
      }
    });
  }
}
