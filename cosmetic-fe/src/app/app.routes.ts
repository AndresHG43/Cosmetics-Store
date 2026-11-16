import { Routes } from '@angular/router';
import { AuthLayoutComponent } from './layout/auth-layout.component';
import { MainLayoutComponent } from './layout/main-layout.component';
import { authGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  // Auth routes
  {
    path: 'login',
    component: AuthLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
      }
    ]
  },

  // Protected application routes
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'products',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'products',
        children: [
          {
            path: '',
            loadComponent: () => import('./features/products/product-list.component').then(m => m.ProductListComponent)
          },
          {
            path: 'new',
            loadComponent: () => import('./features/products/product-form.component').then(m => m.ProductFormComponent)
          },
          {
            path: 'edit/:id',
            loadComponent: () => import('./features/products/product-form.component').then(m => m.ProductFormComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./features/products/product-detail.component').then(m => m.ProductDetailComponent)
          }
        ]
      },
      {
        path: 'entries',
        children: [
          {
            path: '',
            loadComponent: () => import('./features/entries/entry-list.component').then(m => m.EntryListComponent)
          },
          {
            path: 'new',
            loadComponent: () => import('./features/entries/entry-form.component').then(m => m.EntryFormComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./features/entries/entry-detail.component').then(m => m.EntryDetailComponent)
          }
        ]
      },
      {
        path: 'sales',
        children: [
          {
            path: '',
            loadComponent: () => import('./features/sales/sale-list.component').then(m => m.SaleListComponent)
          },
          {
            path: 'new',
            loadComponent: () => import('./features/sales/sale-form.component').then(m => m.SaleFormComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./features/sales/sale-detail.component').then(m => m.SaleDetailComponent)
          }
        ]
      },
      {
        path: 'users',
        canActivate: [adminGuard],
        children: [
          {
            path: '',
            loadComponent: () => import('./features/users/user-list.component').then(m => m.UserListComponent)
          },
          {
            path: 'new',
            loadComponent: () => import('./features/users/user-form.component').then(m => m.UserFormComponent)
          },
          {
            path: 'edit/:id',
            loadComponent: () => import('./features/users/user-form.component').then(m => m.UserFormComponent)
          },
          {
            path: ':id',
            loadComponent: () => import('./features/users/user-detail.component').then(m => m.UserDetailComponent)
          }
        ]
      },
      {
        path: 'roles',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/roles/role-list.component').then(m => m.RoleListComponent)
      }
    ]
  },

  {
    path: '**',
    redirectTo: 'products'
  }
];
