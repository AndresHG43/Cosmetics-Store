export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
  search?: string;
  active?: boolean | string;
}

export interface AuthLoginRequest {
  email: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  email: string;
  roles: string[];
}

export interface User {
  id?: number;
  email: string;
  name?: string;
  lastname?: string;
  active?: boolean;
  dateCreated?: string;
  dateUpdated?: string;
  roles?: Role[];
}

export interface UserCreateRequest {
  name: string;
  lastname: string;
  email: string;
  password?: string;
  roleIds?: number[];
}

export interface UserUpdateRequest {
  name: string;
  lastname: string;
  email: string;
}

export interface Role {
  id?: number;
  name: string;
  description?: string;
}

export interface UserRole {
  id?: number;
  userId: number;
  roleId: number;
  userName?: string;
  roleName?: string;
}

export interface Product {
  id?: number;
  code?: string;
  name: string;
  description?: string;
  price: number;
  stock?: number;
  status?: string | StatusEntity;
  active?: boolean;
}

export interface StockResponse {
  productId: number;
  productName: string;
  stock: number;
}

export interface StatusEntity {
  id: number;
  status: string;
  dateCreated?: string;
}

export interface EntryDetail {
  id?: number;
  entryId?: number;
  productId: number;
  productName?: string;
  priceEntry?: number;
  quantity: number;
  unitPrice?: number;
  subtotal?: number;
  active?: boolean;
}

export interface EntryDetailCreate {
  productId: number;
  priceEntry: number;
  quantity: number;
}

export interface EntryCreateRequest {
  date?: string | null;
  isInitial?: boolean;
  statusId: number;
  details: EntryDetailCreate[];
}

export interface StatusUpdateRequest {
  statusId: number;
}

export interface Entry {
  id?: number;
  date?: string | null;
  total?: number;
  isInitial?: boolean;
  status?: StatusEntity | string;
  active?: boolean;
  dateCreated?: string;
  dateUpdated?: string;
  userCreated?: number;
  userUpdated?: number;
  details?: EntryDetail[];
}

export interface SaleDetailCreate {
  productId: number;
  priceSale: number;
  quantity: number;
}

export interface SaleCreateRequest {
  date?: string | null;
  statusId: number;
  details: SaleDetailCreate[];
}

export interface SaleDetail {
  id?: number;
  saleId?: number;
  productId: number;
  productName?: string;
  priceSale?: number;
  quantity: number;
  unitPrice?: number;
  subtotal?: number;
  active?: boolean;
}

export interface Sale {
  id?: number;
  date?: string | null;
  total?: number;
  status?: StatusEntity | string;
  active?: boolean;
  dateCreated?: string;
  dateUpdated?: string;
  userCreated?: number;
  userUpdated?: number | null;
  details?: SaleDetail[];
}

export interface InventoryAnalytics {
  totalEntries: number;
  totalEntryExpenditure: number;
  entryStatusCounts: Record<string, number>;
  saleStatusCounts: Record<string, number>;
}

export interface TopProduct {
  productId: number;
  productName: string;
  totalQuantitySold: number;
  totalRevenue: number;
}

export interface SalesAnalytics {
  totalSalesCount: number;
  grossRevenue: number;
  averageSaleAmount: number;
}

export interface DashboardAnalytics {
  totalProducts?: number;
  totalSales?: number;
  totalEntries?: number;
  revenue?: number;
}
