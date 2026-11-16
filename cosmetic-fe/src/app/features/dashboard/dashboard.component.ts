import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../core/services/product.service';
import { AnalyticsService } from '../../core/services/analytics.service';
import { InventoryAnalytics, TopProduct, SalesAnalytics } from '../../core/models/api.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="space-y-8">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 class="text-3xl font-serif font-bold text-[#A52A2A]">Panel General & Analíticas</h2>
          <p class="text-sm text-[#7E726B] mt-1">Métricas de inventario, productos más vendidos y rendimiento financiero</p>
        </div>
        <button
          (click)="reloadAll()"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white border border-[#E8E2DC] text-xs font-medium text-[#362D29] hover:bg-[#FAF8F6] rounded-lg transition-colors cursor-pointer self-start sm:self-auto"
        >
          🔄 Actualizar datos
        </button>
      </div>

      <!-- Quick KPI Cards from /analytics/inventory & /analytics/sales -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Total Products in catalog -->
        <div class="bg-white p-5 rounded-xl border border-[#E8E2DC] shadow-xs">
          <div class="flex items-center justify-between text-[#7E726B]">
            <span class="text-xs uppercase font-semibold">Productos Registrados</span>
            <span class="text-lg">💄</span>
          </div>
          <p class="text-3xl font-serif font-bold text-[#362D29] mt-2">{{ totalProducts() }}</p>
          <a routerLink="/products" class="text-xs text-[#A52A2A] hover:underline mt-2 inline-block">Ver catálogo &rarr;</a>
        </div>

        <!-- Gross Revenue from Sales Analytics -->
        <div class="bg-white p-5 rounded-xl border border-[#E8E2DC] shadow-xs">
          <div class="flex items-center justify-between text-[#7E726B]">
            <span class="text-xs uppercase font-semibold">Ingresos Brutos (Ventas)</span>
            <span class="text-lg">💵</span>
          </div>
          <p class="text-3xl font-serif font-bold text-emerald-700 mt-2">
            \${{ (salesAnalytics()?.grossRevenue || 0) | number:'1.2-2' }}
          </p>
          <span class="text-xs text-[#7E726B] mt-2 inline-block">
            Ticket Promedio: \${{ (salesAnalytics()?.averageSaleAmount || 0) | number:'1.2-2' }}
          </span>
        </div>

        <!-- Total Sales Count -->
        <div class="bg-white p-5 rounded-xl border border-[#E8E2DC] shadow-xs">
          <div class="flex items-center justify-between text-[#7E726B]">
            <span class="text-xs uppercase font-semibold">Ventas Concretadas</span>
            <span class="text-lg">🛍️</span>
          </div>
          <p class="text-3xl font-serif font-bold text-[#362D29] mt-2">
            {{ salesAnalytics()?.totalSalesCount || 0 }}
          </p>
          <a routerLink="/sales" class="text-xs text-[#A52A2A] hover:underline mt-2 inline-block">Ver ventas &rarr;</a>
        </div>

        <!-- Inventory Expenditure -->
        <div class="bg-white p-5 rounded-xl border border-[#E8E2DC] shadow-xs">
          <div class="flex items-center justify-between text-[#7E726B]">
            <span class="text-xs uppercase font-semibold">Gasto en Compras / Entradas</span>
            <span class="text-lg">📦</span>
          </div>
          <p class="text-3xl font-serif font-bold text-[#A52A2A] mt-2">
            \${{ (inventoryAnalytics()?.totalEntryExpenditure || 0) | number:'1.2-2' }}
          </p>
          <span class="text-xs text-[#7E726B] mt-2 inline-block">
            Lotes procesados: {{ inventoryAnalytics()?.totalEntries || 0 }}
          </span>
        </div>
      </div>

      <!-- Sales Filter Toolbar -->
      <div class="bg-white p-5 rounded-xl border border-[#E8E2DC] shadow-xs space-y-4">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h3 class="font-serif font-bold text-lg text-[#362D29]">Filtro de Período para Analítica de Ventas</h3>
            <p class="text-xs text-[#7E726B]">Consulta el ingreso bruto y ticket promedio por rango de fechas</p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <div>
              <label class="block text-[10px] font-semibold uppercase text-[#7E726B] mb-0.5">Fecha Inicio</label>
              <input
                type="datetime-local"
                [(ngModel)]="startDate"
                class="px-2.5 py-1.5 text-xs bg-[#FAF8F6] border border-[#E8E2DC] rounded-lg focus:outline-none focus:ring-1 focus:ring-[#A52A2A]"
              />
            </div>
            <div>
              <label class="block text-[10px] font-semibold uppercase text-[#7E726B] mb-0.5">Fecha Fin</label>
              <input
                type="datetime-local"
                [(ngModel)]="endDate"
                class="px-2.5 py-1.5 text-xs bg-[#FAF8F6] border border-[#E8E2DC] rounded-lg focus:outline-none focus:ring-1 focus:ring-[#A52A2A]"
              />
            </div>
            <button
              (click)="filterSalesAnalytics()"
              class="self-end px-3.5 py-1.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-xs font-medium rounded-lg transition-colors cursor-pointer"
            >
              Filtrar
            </button>
            <button
              (click)="resetSalesFilter()"
              class="self-end px-3.5 py-1.5 bg-[#FAF8F6] hover:bg-[#E8E2DC] text-[#362D29] border border-[#E8E2DC] text-xs font-medium rounded-lg transition-colors cursor-pointer"
            >
              Limpiar
            </button>
          </div>
        </div>
      </div>

      <!-- Inventory Status & Lifecycle Breakdown Cards -->
      @if (inventoryAnalytics()) {
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Entry Status Counts -->
          <div class="bg-white p-6 rounded-xl border border-[#E8E2DC] shadow-xs">
            <h3 class="font-serif font-bold text-base text-[#362D29] mb-1">
              Desglose de Estados de Entradas
            </h3>
            <p class="text-xs text-[#7E726B] mb-4">Conteo de lotes según su estado actual en el sistema</p>
            <div class="space-y-2.5">
              @for (item of getStatusEntries(inventoryAnalytics()?.entryStatusCounts); track item.status) {
                <div class="flex items-center justify-between p-3 rounded-lg bg-[#FAF8F6] border border-[#E8E2DC]/60">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold"
                        [ngClass]="getEntryStatusBadge(item.status)">
                    {{ item.status }}
                  </span>
                  <span class="font-bold text-sm text-[#362D29]">{{ item.count }} lotes</span>
                </div>
              }
              @if (getStatusEntries(inventoryAnalytics()?.entryStatusCounts).length === 0) {
                <p class="text-xs text-[#7E726B] italic">No hay registros de entradas.</p>
              }
            </div>
          </div>

          <!-- Sale Status Counts -->
          <div class="bg-white p-6 rounded-xl border border-[#E8E2DC] shadow-xs">
            <h3 class="font-serif font-bold text-base text-[#362D29] mb-1">
              Desglose de Estados de Ventas
            </h3>
            <p class="text-xs text-[#7E726B] mb-4">Conteo de transacciones por etapa de cobro o entrega</p>
            <div class="space-y-2.5">
              @for (item of getStatusEntries(inventoryAnalytics()?.saleStatusCounts); track item.status) {
                <div class="flex items-center justify-between p-3 rounded-lg bg-[#FAF8F6] border border-[#E8E2DC]/60">
                  <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold"
                        [ngClass]="getSaleStatusBadge(item.status)">
                    {{ item.status }}
                  </span>
                  <span class="font-bold text-sm text-[#362D29]">{{ item.count }} ventas</span>
                </div>
              }
              @if (getStatusEntries(inventoryAnalytics()?.saleStatusCounts).length === 0) {
                <p class="text-xs text-[#7E726B] italic">No hay registros de ventas.</p>
              }
            </div>
          </div>
        </div>
      }

      <!-- Top Selling Products Table -->
      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs overflow-hidden">
        <div class="p-6 border-b border-[#E8E2DC] flex items-center justify-between">
          <div>
            <h3 class="font-serif font-bold text-lg text-[#A52A2A]">Productos Más Vendidos (Top Products)</h3>
            <p class="text-xs text-[#7E726B]">Ranking de productos por unidades vendidas e ingresos generados</p>
          </div>
          <span class="text-xs bg-[#FAF8F6] border border-[#E8E2DC] px-2.5 py-1 rounded-md text-[#7E726B]">
            Total: {{ topProducts().length }} artículos
          </span>
        </div>

        @if (isLoadingTopProducts()) {
          <div class="p-12 text-center text-[#7E726B]">Cargando ranking de productos...</div>
        } @else if (topProducts().length === 0) {
          <div class="p-12 text-center text-[#7E726B]">
            <p class="font-medium">No se han registrado ventas de productos todavía.</p>
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm text-[#362D29]">
              <thead class="bg-[#FAF8F6] border-b border-[#E8E2DC] text-xs uppercase text-[#7E726B] tracking-wider font-semibold">
                <tr>
                  <th class="py-3 px-4 w-16 text-center">#</th>
                  <th class="py-3 px-4">Producto</th>
                  <th class="py-3 px-4 text-center">Unidades Vendidas</th>
                  <th class="py-3 px-4 text-right">Ingresos Totales</th>
                  <th class="py-3 px-4 text-center">Acción</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-[#E8E2DC]">
                @for (item of topProducts(); track item.productId; let idx = $index) {
                  <tr class="hover:bg-[#FAF8F6]/60 transition-colors">
                    <td class="py-3.5 px-4 text-center font-bold text-[#A52A2A]">
                      @if (idx === 0) { 🥇 }
                      @else if (idx === 1) { 🥈 }
                      @else if (idx === 2) { 🥉 }
                      @else { #{{ idx + 1 }} }
                    </td>
                    <td class="py-3.5 px-4 font-medium text-[#362D29]">
                      <a [routerLink]="['/products', item.productId]" class="hover:text-[#A52A2A] hover:underline">
                        {{ item.productName }}
                      </a>
                      <span class="block font-mono text-xs text-[#7E726B]">ID: {{ item.productId }}</span>
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">
                        {{ item.totalQuantitySold }} unidades
                      </span>
                    </td>
                    <td class="py-3.5 px-4 text-right font-serif font-bold text-[#362D29]">
                      \${{ item.totalRevenue | number:'1.2-2' }}
                    </td>
                    <td class="py-3.5 px-4 text-center">
                      <a [routerLink]="['/products', item.productId]" class="text-xs text-[#A52A2A] hover:underline font-medium">
                        Ver Producto
                      </a>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      <!-- Quick Action shortcuts -->
      <div class="bg-[#FAF8F6] p-6 rounded-2xl border border-[#E8E2DC] flex flex-wrap items-center justify-between gap-4">
        <div>
          <h3 class="font-serif font-bold text-lg text-[#362D29]">Acciones Rápidas</h3>
          <p class="text-xs text-[#7E726B]">Operaciones frecuentes de mostrador e inventario</p>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          <a routerLink="/sales/new" class="px-4 py-2 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
            + Cobrar Venta
          </a>
          <a routerLink="/entries/new" class="px-4 py-2 bg-white hover:bg-[#F9F7F5] border border-[#E8E2DC] text-[#362D29] text-sm font-medium rounded-lg transition-colors">
            + Ingresar Lote
          </a>
          <a routerLink="/products/new" class="px-4 py-2 bg-white hover:bg-[#F9F7F5] border border-[#E8E2DC] text-[#362D29] text-sm font-medium rounded-lg transition-colors">
            + Crear Producto
          </a>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  private productService = inject(ProductService);
  private analyticsService = inject(AnalyticsService);

  totalProducts = signal(0);
  inventoryAnalytics = signal<InventoryAnalytics | null>(null);
  salesAnalytics = signal<SalesAnalytics | null>(null);
  topProducts = signal<TopProduct[]>([]);
  isLoadingTopProducts = signal(true);

  startDate = '';
  endDate = '';

  ngOnInit(): void {
    this.reloadAll();
  }

  reloadAll(): void {
    this.loadCatalogCount();
    this.loadInventoryAnalytics();
    this.loadSalesAnalytics();
    this.loadTopProducts();
  }

  loadCatalogCount(): void {
    this.productService.getAll({ size: 1, active: true }).subscribe(resp => {
      if (resp.success && resp.data) {
        this.totalProducts.set(resp.data.totalElements);
      }
    });
  }

  loadInventoryAnalytics(): void {
    this.analyticsService.getInventory().subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          this.inventoryAnalytics.set(resp.data);
        }
      }
    });
  }

  loadSalesAnalytics(): void {
    const sDate = this.startDate ? `${this.startDate}:00` : undefined;
    const eDate = this.endDate ? `${this.endDate}:00` : undefined;

    this.analyticsService.getSales(sDate, eDate).subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          this.salesAnalytics.set(resp.data);
        }
      }
    });
  }

  loadTopProducts(): void {
    this.isLoadingTopProducts.set(true);
    this.analyticsService.getTopProducts({ page: 0, size: 20 }).subscribe({
      next: (resp) => {
        this.isLoadingTopProducts.set(false);
        if (resp.success && resp.data) {
          this.topProducts.set(resp.data.content || []);
        }
      },
      error: () => this.isLoadingTopProducts.set(false)
    });
  }

  filterSalesAnalytics(): void {
    this.loadSalesAnalytics();
  }

  resetSalesFilter(): void {
    this.startDate = '';
    this.endDate = '';
    this.loadSalesAnalytics();
  }

  getStatusEntries(countsMap: Record<string, number> | undefined): { status: string; count: number }[] {
    if (!countsMap) return [];
    return Object.entries(countsMap).map(([status, count]) => ({ status, count }));
  }

  getEntryStatusBadge(status: string): string {
    const s = status.toUpperCase();
    if (s.includes('DONE')) return 'bg-emerald-100 text-emerald-800';
    if (s.includes('PROGRESS')) return 'bg-amber-100 text-amber-800';
    if (s.includes('CANCEL') || s.includes('DELETE')) return 'bg-rose-100 text-rose-800';
    return 'bg-blue-100 text-blue-800';
  }

  getSaleStatusBadge(status: string): string {
    const s = status.toUpperCase();
    if (s.includes('DONE')) return 'bg-emerald-100 text-emerald-800';
    if (s.includes('PENDING') || s.includes('PROGRESS')) return 'bg-amber-100 text-amber-800';
    if (s.includes('CANCEL') || s.includes('DELETE')) return 'bg-rose-100 text-rose-800';
    return 'bg-blue-100 text-blue-800';
  }
}
