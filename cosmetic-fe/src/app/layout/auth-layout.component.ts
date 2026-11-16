import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <main class="min-h-screen bg-[#F9F7F5] flex items-center justify-center p-4">
      <router-outlet></router-outlet>
    </main>
  `
})
export class AuthLayoutComponent {}
