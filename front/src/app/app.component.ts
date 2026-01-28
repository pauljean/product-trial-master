import {
  Component,
  OnInit,
  inject,
  computed,
  effect
} from "@angular/core";
import { RouterModule } from "@angular/router";
import { SplitterModule } from 'primeng/splitter';
import { ToolbarModule } from 'primeng/toolbar';
import { BadgeModule } from 'primeng/badge';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { TooltipModule } from 'primeng/tooltip';
import { PanelMenuComponent } from "./shared/ui/panel-menu/panel-menu.component";
import { CartService } from "./shared/data-access/cart.service";
import { AuthService } from "./shared/data-access/auth.service";
import { WishlistService } from "./shared/data-access/wishlist.service";
import { LoginComponent } from "./shared/features/login/login.component";
import { DialogModule } from "primeng/dialog";
import { MenuItem } from 'primeng/api';

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.scss"],
  standalone: true,
  imports: [
    RouterModule, 
    SplitterModule, 
    ToolbarModule, 
    PanelMenuComponent,
    BadgeModule,
    ButtonModule,
    MenuModule,
    TooltipModule,
    LoginComponent,
    DialogModule
  ],
})
export class AppComponent implements OnInit {
  private readonly cartService = inject(CartService);
  private readonly authService = inject(AuthService);
  private readonly wishlistService = inject(WishlistService);
  
  title = "ALTEN SHOP";
  
  public readonly cartItemCount = this.cartService.cartItemCount;
  public readonly wishlistItemCount = this.wishlistService.wishlistItemCount;
  public readonly isAuthenticated = this.authService.isAuthenticated;
  public readonly userEmail = computed(() => this.authService.getCurrentUserEmail());
  public showLoginDialog = false;
  public userMenuItems: MenuItem[] = [];

  ngOnInit() {
    // Charger le panier et les favoris (local ou serveur selon l'authentification)
    this.cartService.getCartItems().subscribe();
    this.wishlistService.getWishlistItems().subscribe();
    this.updateUserMenu();
    
    // Mettre à jour le menu quand l'authentification change
    effect(() => {
      this.isAuthenticated();
      this.userEmail();
      this.updateUserMenu();
    });
  }

  private updateUserMenu() {
    this.userMenuItems = [
      {
        label: 'Profil',
        icon: 'pi pi-user',
        items: [
          {
            label: this.userEmail() || 'Utilisateur',
            icon: 'pi pi-envelope',
            disabled: true
          },
          {
            separator: true
          },
          {
            label: 'Déconnexion',
            icon: 'pi pi-sign-out',
            command: () => this.logout()
          }
        ]
      }
    ];
  }

  public openLogin() {
    this.showLoginDialog = true;
  }

  public closeLogin() {
    this.showLoginDialog = false;
  }

  public logout() {
    this.authService.logout();
    this.cartService.clearCart().subscribe();
    this.showLoginDialog = false;
    this.updateUserMenu();
  }
}
