import { Component, OnInit, inject } from "@angular/core";
import { WishlistService, WishlistItem } from "../../data-access/wishlist.service";
import { ButtonModule } from "primeng/button";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { CommonModule } from "@angular/common";
import { TooltipModule } from "primeng/tooltip";
import { CartService } from "../../data-access/cart.service";

@Component({
  selector: "app-wishlist",
  templateUrl: "./wishlist.component.html",
  styleUrls: ["./wishlist.component.scss"],
  standalone: true,
  imports: [ButtonModule, ToastModule, CommonModule, TooltipModule],
  providers: [MessageService]
})
export class WishlistComponent implements OnInit {
  private readonly wishlistService = inject(WishlistService);
  private readonly cartService = inject(CartService);
  private readonly messageService = inject(MessageService);

  public readonly wishlistItems = this.wishlistService.wishlistItems;

  ngOnInit() {
    this.wishlistService.getWishlistItems().subscribe({
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger la liste d\'envie'
        });
      }
    });
  }

  public onAddToCart(productId: number, quantity: number = 1) {
    this.cartService.addToCart(productId, quantity).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Produit ajouté au panier'
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible d\'ajouter le produit au panier'
        });
      }
    });
  }

  public onRemoveFromWishlist(item: WishlistItem) {
    this.wishlistService.removeFromWishlist(item.id, item.product.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Produit retiré des favoris'
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
}
