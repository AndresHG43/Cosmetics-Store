import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, of, Subject, Subscription } from 'rxjs';
import { catchError, map, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ProductService } from '../../core/services/product.service';
import { Product } from '../../core/models/api.models';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="space-y-6">
      <!-- Header with Action -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">Catálogo de Productos</h2>
          <p class="text-sm text-[#7E726B]">Inventario, precios y existencias disponibles</p>
        </div>
        <a routerLink="/products/new"
          class="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
          <span>+</span> Nuevo Producto
        </a>
      </div>

      <!-- Search and Filter Bar -->
      <div class="bg-white p-4 rounded-xl border border-[#E8E2DC] shadow-xs flex flex-col sm:flex-row items-center gap-4">
        <div class="relative flex-1 w-full">
          <span class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-[#7E726B]">
            🔍
          </span>
          <input
            type="text"
            [ngModel]="searchTerm"
            (ngModelChange)="onSearchChange($event)"
            placeholder="Buscar productos en tiempo real por nombre o descripción..."
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
        <div class="flex items-center gap-2 w-full sm:w-auto">
          <select
            [(ngModel)]="activeFilter"
            (change)="onFilterChange()"
            class="px-3 py-2 bg-[#FAF8F6] border border-[#E8E2DC] rounded-lg text-sm text-[#362D29] focus:bg-white focus:outline-none"
          >
            <option value="true">Solo Activos</option>
            <option value="false">Inactivos</option>
          </select>
        </div>
      </div>

      <!-- Products Table -->
      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
        @if (isLoading()) {
          <div class="p-12 text-center text-[#7E726B] flex flex-col items-center justify-center">
            <span class="w-8 h-8 border-3 border-[#A52A2A] border-t-transparent rounded-full animate-spin mb-3"></span>
            <p>Consultando catálogo...</p>
          </div>
        } @else if (products().length === 0) {
          <div class="p-12 text-center text-[#7E726B]">
            <span class="text-4xl block mb-2">💄</span>
            <p class="font-medium">No se encontraron productos en esta página o búsqueda.</p>
            @if (searchTerm) {
              <button (click)="clearSearch()" class="text-sm text-[#A52A2A] hover:underline mt-2 inline-block cursor-pointer">
                Limpiar búsqueda
              </button>
            }
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm text-[#362D29]">
              <thead class="bg-[#FAF8F6] border-b border-[#E8E2DC] text-xs uppercase text-[#7E726B] tracking-wider font-semibold">
                <tr>
                  <th class="py-3 px-4">ID</th>
                  <th class="py-3 px-4">Producto</th>
                  <th class="py-3 px-4">Descripción</th>
                  <th class="py-3 px-4 text-right">Precio</th>
                  <th class="py-3 px-4 text-center">Stock</th>
                  <th class="py-3 px-4 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[#E8E2DC]">
                @for (product of products(); track product.id) {
                  <tr class="hover:bg-[#FAF8F6]/60 transition-colors">
                    <td class="py-3.5 px-4 font-mono text-xs text-[#7E726B]">
                      #{{ product.id }}
                    </td>
                    <td class="py-3.5 px-4 font-medium text-[#362D29]">
                      <a [routerLink]="['/products', product.id]" class="hover:text-[#A52A2A] hover:underline">
                        {{ product.name }}
                      </a>
                    </td>
                    <td class="py-3.5 px-4 text-[#7E726B] max-w-xs truncate">
                      {{ product.description || '—' }}
                    </td>
                    <td class="py-3.5 px-4 text-right font-semibold">
                      \${{ product.price | number:'1.2-2' }}
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                            [ngClass]="{
                              'bg-emerald-100 text-emerald-800': (product.stock || 0) > 10,
                              'bg-amber-100 text-amber-800': (product.stock || 0) > 0 && (product.stock || 0) <= 10,
                              'bg-rose-100 text-rose-800': (product.stock || 0) === 0
                            }">
                        {{ product.stock || 0 }} uds
                      </span>
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <div class="inline-flex items-center gap-2">
                        <a [routerLink]="['/products', product.id]" class="text-xs text-[#7E726B] hover:text-[#362D29] font-medium">
                          Detalle
                        </a>
                        <span class="text-[#E8E2DC]">|</span>
                        <a [routerLink]="['/products/edit', product.id]" class="text-xs text-[#A52A2A] hover:underline font-medium">
                          Editar
                        </a>
                        <span class="text-[#E8E2DC]">|</span>
                        <button (click)="confirmDelete(product)" class="text-xs text-rose-600 hover:text-rose-800 font-medium cursor-pointer">
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <!-- Pagination Controls -->
          <div class="p-4 border-t border-[#E8E2DC] flex items-center justify-between bg-[#FAF8F6] text-xs text-[#7E726B]">
            <div>
              Mostrando página <span class="font-bold text-[#362D29]">{{ currentPage() + 1 }}</span> de
              <span class="font-bold text-[#362D29]">{{ totalPages() || 1 }}</span>
              (Total: <span class="font-bold text-[#362D29]">{{ totalElements() }}</span> productos)
            </div>
            <div class="flex items-center gap-2">
              <button
                (click)="goToPage(currentPage() - 1)"
                [disabled]="isFirst()"
                class="px-3 py-1.5 rounded border border-[#E8E2DC] bg-white text-[#362D29] hover:bg-[#F9F7F5] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer font-medium"
              >
                &larr; Anterior
              </button>
              <button
                (click)="goToPage(currentPage() + 1)"
                [disabled]="isLast()"
                class="px-3 py-1.5 rounded border border-[#E8E2DC] bg-white text-[#362D29] hover:bg-[#F9F7F5] disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer font-medium"
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
export class ProductListComponent implements OnInit, OnDestroy {
  private productService = inject(ProductService);

  products = signal<Product[]>([]);
  isLoading = signal(true);

  currentPage = signal(0);
  pageSize = signal(20);
  totalPages = signal(0);
  totalElements = signal(0);
  isFirst = signal(true);
  isLast = signal(true);

  searchTerm = '';
  activeFilter = 'true';

  private searchSubject = new Subject<string>();
  private searchSubscription?: Subscription;

  ngOnInit(): void {
    this.searchSubscription = this.searchSubject.pipe(
      debounceTime(800),
      distinctUntilChanged()
    ).subscribe(term => {
      this.searchTerm = term;
      this.loadProducts(0);
    });

    this.loadProducts();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.searchSubject.next(term);
  }

  onFilterChange(): void {
    this.loadProducts(0);
  }

  loadProducts(page = 0): void {
    this.isLoading.set(true);
    this.productService.getAll({
      page,
      size: this.pageSize(),
      sort: 'id,desc',
      search: this.searchTerm || undefined,
      active: this.activeFilter !== '' ? this.activeFilter : undefined
    }).subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          const pageData = resp.data;
          const content = pageData.content || [];
          this.currentPage.set(pageData.page);
          this.totalPages.set(pageData.totalPages);
          this.totalElements.set(pageData.totalElements);
          this.isFirst.set(pageData.first);
          this.isLast.set(pageData.last);

          if (content.length === 0) {
            this.products.set([]);
            this.isLoading.set(false);
            return;
          }

          // Fetch real stock for each product on the current page in parallel
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
              this.products.set(productsWithStock);
              this.isLoading.set(false);
            },
            error: () => {
              this.products.set(content);
              this.isLoading.set(false);
            }
          });
        } else {
          this.products.set([]);
          this.isLoading.set(false);
        }
      },
      error: () => this.isLoading.set(false)
    });
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.searchSubject.next('');
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages()) {
      this.loadProducts(page);
    }
  }

  confirmDelete(product: Product): void {
    if (!product.id) return;
    const confirmed = window.confirm(`¿Estás seguro de que deseas eliminar el producto "${product.name}" (#${product.id})?`);
    if (confirmed) {
      this.productService.delete(product.id).subscribe({
        next: (resp) => {
          if (resp.success) {
            this.loadProducts(this.currentPage());
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
