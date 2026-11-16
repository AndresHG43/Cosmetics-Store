import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { ProductService } from '../../core/services/product.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="max-w-2xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">
            {{ isEditMode() ? 'Editar Producto #' + productId() : 'Registrar Nuevo Producto' }}
          </h2>
          <p class="text-sm text-[#7E726B]">
            {{ isEditMode() ? 'Actualiza los datos del producto en el catálogo' : 'Añade un nuevo artículo cosmético al catálogo' }}
          </p>
        </div>
        <a routerLink="/products" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver al catálogo
        </a>
      </div>

      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs p-6 md:p-8">
        @if (isLoading()) {
          <div class="py-12 text-center text-[#7E726B]">
            <span class="inline-block w-6 h-6 border-2 border-[#A52A2A] border-t-transparent rounded-full animate-spin mb-2"></span>
            <p>Cargando información del producto...</p>
          </div>
        } @else {
          @if (errorMessage()) {
            <div class="mb-6 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm">
              {{ errorMessage() }}
            </div>
          }

          <form [formGroup]="productForm" (ngSubmit)="onSubmit()" class="space-y-5">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                  Nombre del Producto *
                </label>
                <input
                  type="text"
                  formControlName="name"
                  placeholder="Labial Mate Larga Duración - Tono Rubí"
                  class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                  Precio Unitario (\$) *
                </label>
                <input
                  type="number"
                  step="0.01"
                  formControlName="price"
                  placeholder="19.99"
                  class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
                />
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                Descripción & Ingredientes *
              </label>
              <textarea
                rows="3"
                formControlName="description"
                placeholder="Descripción del acabado, textura y formulación..."
                class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
              ></textarea>
            </div>

            <div class="pt-4 border-t border-[#E8E2DC] flex items-center justify-end gap-3">
              <a routerLink="/products" class="px-5 py-2.5 text-sm font-medium text-[#7E726B] hover:text-[#362D29]">
                Cancelar
              </a>
              <button
                type="submit"
                [disabled]="productForm.invalid || isSubmitting()"
                class="px-6 py-2.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors disabled:opacity-50 cursor-pointer"
              >
                @if (isSubmitting()) {
                  Guardando...
                } @else {
                  {{ isEditMode() ? 'Actualizar Producto' : 'Guardar Producto' }}
                }
              </button>
            </div>
          </form>
        }
      </div>
    </div>
  `
})
export class ProductFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isEditMode = signal(false);
  productId = signal<number | null>(null);
  isLoading = signal(false);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  productForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.maxLength(255)]],
    price: [null as number | null, [Validators.required, Validators.min(0.01)]]
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.isEditMode.set(true);
      this.productId.set(id);
      this.loadProduct(id);
    }
  }

  loadProduct(id: number): void {
    this.isLoading.set(true);
    this.productService.getById(id).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          this.productForm.patchValue({
            name: resp.data.name,
            description: resp.data.description,
            price: resp.data.price
          });
        } else {
          this.errorMessage.set('No se pudo encontrar el producto.');
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Error al cargar el producto.');
      }
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const val = this.productForm.value;
    const payload = {
      name: val.name!,
      description: val.description!,
      price: val.price!
    };

    if (this.isEditMode() && this.productId()) {
      this.productService.update(this.productId()!, payload).subscribe({
        next: (resp) => {
          this.isSubmitting.set(false);
          if (resp.success) {
            this.router.navigate(['/products']);
          } else {
            this.errorMessage.set(resp.message || 'Error al actualizar producto');
          }
        },
        error: (err) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(err.error?.message || 'Error al conectar con el servidor.');
        }
      });
    } else {
      this.productService.create(payload).subscribe({
        next: (resp) => {
          this.isSubmitting.set(false);
          if (resp.success) {
            this.router.navigate(['/products']);
          } else {
            this.errorMessage.set(resp.message || 'Error al guardar producto');
          }
        },
        error: (err) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(err.error?.message || 'Error al conectar con el servidor.');
        }
      });
    }
  }
}
