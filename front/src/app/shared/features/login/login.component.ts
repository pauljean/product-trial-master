import { Component, inject, output } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { ButtonModule } from "primeng/button";
import { InputTextModule } from "primeng/inputtext";
import { PasswordModule } from "primeng/password";
import { DialogModule } from "primeng/dialog";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { AuthService } from "../../data-access/auth.service";
import { CartService } from "../../data-access/cart.service";
import { DividerModule } from "primeng/divider";

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    PasswordModule,
    DialogModule,
    ToastModule,
    CommonModule,
    DividerModule
  ],
  providers: [MessageService]
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly messageService = inject(MessageService);
  private readonly router = inject(Router);
  private readonly cartService = inject(CartService);
  private readonly fb = inject(FormBuilder);
  
  public readonly closeDialog = output<void>();

  public isRegisterMode = false;
  public loginForm: FormGroup;
  public registerForm: FormGroup;

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      firstname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  public get currentForm(): FormGroup {
    return this.isRegisterMode ? this.registerForm : this.loginForm;
  }

  public get email() {
    return this.currentForm.get('email');
  }

  public get password() {
    return this.currentForm.get('password');
  }

  public get username() {
    return this.registerForm.get('username');
  }

  public get firstname() {
    return this.registerForm.get('firstname');
  }

  public getPasswordStrength(): 'weak' | 'medium' | 'strong' | null {
    const pwd = this.password?.value || '';
    if (!pwd || pwd.length < 6) return null;
    if (pwd.length < 8) return 'weak';
    if (pwd.length < 12 && /[A-Z]/.test(pwd) && /[0-9]/.test(pwd)) return 'medium';
    if (pwd.length >= 12 && /[A-Z]/.test(pwd) && /[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) return 'strong';
    return 'medium';
  }

  public toggleMode() {
    this.isRegisterMode = !this.isRegisterMode;
    this.loginForm.reset();
    this.registerForm.reset();
  }

  public onSubmit() {
    if (this.currentForm.invalid) {
      this.currentForm.markAllAsTouched();
      return;
    }

    if (this.isRegisterMode) {
      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Compte créé avec succès. Vous pouvez maintenant vous connecter.'
          });
          this.toggleMode();
        },
        error: (error) => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: error.error?.message || 'Impossible de créer le compte'
          });
        }
      });
    } else {
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Connexion réussie'
          });
          this.cartService.getCartItems().subscribe();
          this.closeDialog.emit();
          this.router.navigate(['/products/list']);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Email ou mot de passe incorrect'
          });
        }
      });
    }
  }

  public logout() {
    this.authService.logout();
    this.messageService.add({
      severity: 'info',
      summary: 'Info',
      detail: 'Déconnexion réussie'
    });
    this.router.navigate(['/home']);
  }
}
