import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductService } from '../../core/services/product.service';
import { Product } from '../../core/models/api.models';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="max-w-3xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <a routerLink="/products" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver al catálogo
        </a>
        @if (product()) {
          <div class="flex items-center gap-2">
            <a [routerLink]="['/products/edit', product()?.id]"
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
          Cargando detalle del producto...
        </div>
      } @else if (product()) {
        <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
          <div class="p-6 md:p-8 border-b border-[#E8E2DC] flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-[#FAF8F6]">
            <div>
              <span class="text-xs font-mono text-[#7E726B]">#{{ product()?.id }}</span>
              <h2 class="text-2xl font-serif font-bold text-[#A52A2A] mt-1">{{ product()?.name }}</h2>
            </div>
            <div class="text-right">
              <p class="text-xs text-[#7E726B] uppercase font-semibold">Precio al Público</p>
              <p class="text-3xl font-serif font-bold text-[#362D29]">\${{ product()?.price | number:'1.2-2' }}</p>
            </div>
          </div>

          <div class="p-6 md:p-8 space-y-6">
            <div>
              <h3 class="text-xs font-semibold uppercase tracking-wider text-[#7E726B] mb-2">Descripción</h3>
              <p class="text-sm text-[#362D29] leading-relaxed">
                {{ product()?.description || 'Sin descripción disponible.' }}
              </p>
            </div>

            <div class="grid grid-cols-2 gap-4 pt-4 border-t border-[#E8E2DC]">
              <div class="bg-[#FAF8F6] p-4 rounded-lg">
                <p class="text-xs text-[#7E726B] uppercase font-semibold">Existencias en Bodega</p>
                <p class="text-xl font-bold text-[#362D29] mt-1">{{ product()?.stock || 0 }} unidades</p>
              </div>
              <div class="bg-[#FAF8F6] p-4 rounded-lg">
                <p class="text-xs text-[#7E726B] uppercase font-semibold">Estado del Producto</p>
                <p class="text-xl font-bold text-emerald-700 mt-1">Activo</p>
              </div>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productService = inject(ProductService);

  product = signal<Product | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.productService.getById(id).subscribe({
        next: (resp) => {
          if (resp.success && resp.data) {
            const prod = resp.data;
            this.productService.getStock(id).subscribe({
              next: (stockResp) => {
                this.isLoading.set(false);
                if (stockResp.success && stockResp.data) {
                  this.product.set({ ...prod, stock: stockResp.data.stock });
                } else {
                  this.product.set(prod);
                }
              },
              error: () => {
                this.isLoading.set(false);
                this.product.set(prod);
              }
            });
          } else {
            this.isLoading.set(false);
          }
        },
        error: () => this.isLoading.set(false)
      });
    }
  }

  onDelete(): void {
    const prod = this.product();
    if (!prod || !prod.id) return;

    const confirmed = window.confirm(`¿Estás seguro de que deseas eliminar el producto "${prod.name}" (#${prod.id})?`);
    if (confirmed) {
      this.productService.delete(prod.id).subscribe({
        next: (resp) => {
          if (resp.success) {
            this.router.navigate(['/products']);
          } else {
            alert(resp.message || 'No se pudo eliminar el producto.');
          }
        },
        error: (err) => {
          alert(err.error?.message || 'Error al conectar con el servidor.');
        }
      });
    }
  }
}
