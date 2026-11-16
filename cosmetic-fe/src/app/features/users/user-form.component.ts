import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { UserService, RoleService } from '../../core/services/admin.service';
import { Role, UserCreateRequest, UserUpdateRequest } from '../../core/models/api.models';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="max-w-2xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-serif font-bold text-[#A52A2A]">
            {{ isEditMode() ? 'Editar Usuario #' + userId() : 'Registrar Nuevo Usuario' }}
          </h2>
          <p class="text-sm text-[#7E726B]">
            {{ isEditMode() ? 'Actualiza los datos personales del usuario' : 'Crea una nueva cuenta de acceso al sistema' }}
          </p>
        </div>
        <a routerLink="/users" class="text-sm text-[#7E726B] hover:text-[#362D29]">
          &larr; Volver a usuarios
        </a>
      </div>

      <div class="bg-white rounded-xl border border-[#E8E2DC] shadow-xs p-6 md:p-8">
        @if (isLoading()) {
          <div class="py-12 text-center text-[#7E726B]">
            <span class="inline-block w-6 h-6 border-2 border-[#A52A2A] border-t-transparent rounded-full animate-spin mb-2"></span>
            <p>Cargando información del usuario...</p>
          </div>
        } @else {
          @if (errorMessage()) {
            <div class="mb-6 p-3 rounded-lg bg-red-50 border border-red-200 text-red-700 text-sm">
              {{ errorMessage() }}
            </div>
          }

          <form [formGroup]="userForm" (ngSubmit)="onSubmit()" class="space-y-5">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                  Nombre *
                </label>
                <input
                  type="text"
                  formControlName="name"
                  placeholder="Ej. Juan"
                  class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                  Apellido *
                </label>
                <input
                  type="text"
                  formControlName="lastname"
                  placeholder="Ej. Pérez"
                  class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
                />
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                Correo Electrónico *
              </label>
              <input
                type="email"
                formControlName="email"
                placeholder="usuario@gmail.com"
                class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
              />
            </div>

            @if (!isEditMode()) {
              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-1.5">
                  Contraseña *
                </label>
                <input
                  type="password"
                  formControlName="password"
                  placeholder="Mínimo 6 caracteres"
                  class="w-full px-4 py-2.5 rounded-lg border border-[#E8E2DC] bg-[#FAF8F6] text-sm focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#A52A2A]/20 focus:border-[#A52A2A]"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold uppercase tracking-wider text-[#362D29] mb-2">
                  Roles Asignados
                </label>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 p-3 bg-[#FAF8F6] rounded-lg border border-[#E8E2DC]">
                  @for (role of availableRoles(); track role.id) {
                    <label class="inline-flex items-center gap-2 text-sm text-[#362D29] cursor-pointer">
                      <input
                        type="checkbox"
                        [checked]="isRoleSelected(role.id!)"
                        (change)="toggleRole(role.id!, $event)"
                        class="w-4 h-4 text-[#A52A2A] rounded border-[#E8E2DC] focus:ring-[#A52A2A]"
                      />
                      <span>{{ role.name }}</span>
                    </label>
                  }
                </div>
              </div>
            }

            <div class="pt-4 border-t border-[#E8E2DC] flex items-center justify-end gap-3">
              <a routerLink="/users" class="px-5 py-2.5 text-sm font-medium text-[#7E726B] hover:text-[#362D29]">
                Cancelar
              </a>
              <button
                type="submit"
                [disabled]="userForm.invalid || isSubmitting()"
                class="px-6 py-2.5 bg-[#A52A2A] hover:bg-[#8B2323] text-white text-sm font-medium rounded-lg shadow-sm transition-colors disabled:opacity-50 cursor-pointer"
              >
                @if (isSubmitting()) {
                  Guardando...
                } @else {
                  {{ isEditMode() ? 'Actualizar Usuario' : 'Crear Usuario' }}
                }
              </button>
            </div>
          </form>
        }
      </div>
    </div>
  `
})
export class UserFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private roleService = inject(RoleService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isEditMode = signal(false);
  userId = signal<number | null>(null);
  isLoading = signal(false);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  availableRoles = signal<Role[]>([]);
  selectedRoleIds = signal<number[]>([]);

  userForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(80)]],
    lastname: ['', [Validators.required, Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    password: ['']
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.isEditMode.set(true);
      this.userId.set(id);
      this.loadUser(id);
    } else {
      this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6), Validators.maxLength(500)]);
      this.userForm.get('password')?.updateValueAndValidity();
      this.loadRoles();
    }
  }

  loadRoles(): void {
    this.roleService.getAll({ size: 100 }).subscribe({
      next: (resp) => {
        if (resp.success && resp.data) {
          this.availableRoles.set(resp.data.content || []);
        }
      }
    });
  }

  loadUser(id: number): void {
    this.isLoading.set(true);
    this.userService.getById(id).subscribe({
      next: (resp) => {
        this.isLoading.set(false);
        if (resp.success && resp.data) {
          this.userForm.patchValue({
            name: resp.data.name || '',
            lastname: resp.data.lastname || '',
            email: resp.data.email
          });
        } else {
          this.errorMessage.set('No se pudo encontrar el usuario.');
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Error al cargar el usuario.');
      }
    });
  }

  isRoleSelected(roleId: number): boolean {
    return this.selectedRoleIds().includes(roleId);
  }

  toggleRole(roleId: number, event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedRoleIds.set([...this.selectedRoleIds(), roleId]);
    } else {
      this.selectedRoleIds.set(this.selectedRoleIds().filter(id => id !== roleId));
    }
  }

  onSubmit(): void {
    if (this.userForm.invalid) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const val = this.userForm.value;

    if (this.isEditMode() && this.userId()) {
      const updatePayload: UserUpdateRequest = {
        name: val.name!,
        lastname: val.lastname!,
        email: val.email!
      };

      this.userService.update(this.userId()!, updatePayload).subscribe({
        next: (resp) => {
          this.isSubmitting.set(false);
          if (resp.success) {
            this.router.navigate(['/users']);
          } else {
            this.errorMessage.set(resp.message || 'Error al actualizar usuario');
          }
        },
        error: (err) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(err.error?.message || 'Error al conectar con el servidor.');
        }
      });
    } else {
      const createPayload: UserCreateRequest = {
        name: val.name!,
        lastname: val.lastname!,
        email: val.email!,
        password: val.password!,
        roleIds: this.selectedRoleIds()
      };

      this.userService.create(createPayload).subscribe({
        next: (resp) => {
          this.isSubmitting.set(false);
          if (resp.success) {
            this.router.navigate(['/users']);
          } else {
            this.errorMessage.set(resp.message || 'Error al crear usuario');
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
