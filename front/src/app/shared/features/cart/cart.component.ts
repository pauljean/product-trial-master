import { Component, OnInit, inject, computed } from "@angular/core";
import { CartService, CartItem } from "../../data-access/cart.service";
import { ButtonModule } from "primeng/button";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { InputNumberModule } from "primeng/inputnumber";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { TooltipModule } from "primeng/tooltip";

@Component({
  selector: "app-cart",
  templateUrl: "./cart.component.html",
  styleUrls: ["./cart.component.scss"],
  standalone: true,
  imports: [ButtonModule, ToastModule, InputNumberModule, FormsModule, CommonModule, TooltipModule],
  providers: [MessageService]
})
export class CartComponent implements OnInit {
  private readonly cartService = inject(CartService);
  private readonly messageService = inject(MessageService);

  public readonly cartItems = this.cartService.cartItems;
  
  public readonly total = computed(() => 
    this.cartItems().reduce((sum, item) => sum + (item.product.price * item.quantity), 0)
  );

  ngOnInit() {
    this.cartService.getCartItems().subscribe({
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger le panier'
        });
      }
    });
  }

  public onQuantityChange(cartItem: CartItem, quantity: number | string | null) {
    const numQuantity = typeof quantity === 'number' ? quantity : (quantity ? Number(quantity) : 1);
    this.cartService.updateCartItemQuantity(cartItem.id, numQuantity).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Quantité mise à jour'
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de mettre à jour la quantité'
        });
      }
    });
  }

  public onRemoveFromCart(cartItem: CartItem) {
    this.cartService.removeFromCart(cartItem.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Produit retiré du panier'
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de retirer le produit'
        });
      }
    });
  }

  public onClearCart() {
    this.cartService.clearCart().subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Panier vidé'
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de vider le panier'
        });
      }
    });
  }
}
