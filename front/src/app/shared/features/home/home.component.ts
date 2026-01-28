import { Component, OnInit, inject, signal, computed } from "@angular/core";
import { Product } from "app/products/data-access/product.model";
import { ProductsService, PageResponse } from "app/products/data-access/products.service";
import { ProductItemComponent } from "app/products/ui/product-item/product-item.component";
import { CartService } from "app/shared/data-access/cart.service";
import { WishlistService } from "app/shared/data-access/wishlist.service";
import { PaginatorModule } from 'primeng/paginator';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { CommonModule } from '@angular/common';
import { TooltipModule } from 'primeng/tooltip';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';

@Component({
  selector: "app-home",
  templateUrl: "./home.component.html",
  styleUrls: ["./home.component.scss"],
  standalone: true,
  imports: [
    CommonModule,
    ProductItemComponent,
    PaginatorModule,
    InputTextModule,
    DropdownModule,
    FormsModule,
    ToastModule,
    TooltipModule
  ],
  providers: [MessageService]
})
export class HomeComponent implements OnInit {
  private readonly productsService = inject(ProductsService);
  private readonly cartService = inject(CartService);
  private readonly wishlistService = inject(WishlistService);
  private readonly messageService = inject(MessageService);

  public readonly appTitle = "ALTEN SHOP";
  public readonly products = this.productsService.products;
  
  // Filtrage et recherche
  public searchText = signal<string>("");
  private searchSubject = new Subject<string>();
  public selectedCategory = signal<string | null>(null);
  public readonly categories = [
    { label: "Toutes", value: null },
    { label: "Accessories", value: "Accessories" },
    { label: "Fitness", value: "Fitness" },
    { label: "Clothing", value: "Clothing" },
    { label: "Electronics", value: "Electronics" }
  ];
  
  // Pagination côté serveur
  public first = signal<number>(0);
  public rows = signal<number>(9);
  public totalRecords = signal<number>(0);
  public loading = signal<boolean>(false);
  
  // Produits affichés (déjà paginés côté serveur)
  public readonly paginatedProducts = computed(() => this.products());

  ngOnInit() {
    // Charger les produits initiaux
    this.loadProducts();
    this.wishlistService.getWishlistItems().subscribe();
    
    // Débounce pour la recherche
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
      this.loadProducts();
    });
  }

  private loadProducts() {
    this.loading.set(true);
    const page = Math.floor(this.first() / this.rows());
    
    this.productsService.get({
      page: page,
      size: this.rows(),
      sortBy: 'name',
      sortDir: 'ASC',
      category: this.selectedCategory() || undefined,
      search: this.searchText() || undefined
    }).subscribe({
      next: (response) => {
        if (Array.isArray(response)) {
          // Mode sans pagination (fallback)
          this.totalRecords.set(response.length);
        } else {
          // Mode avec pagination
          const pageResponse = response as PageResponse<Product>;
          this.totalRecords.set(pageResponse.totalElements);
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  public onAddToCart(event: { product: Product; quantity: number }) {
    this.cartService.addToCart(event.product.id, event.quantity).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Produit ajouté au panier'
        });
      },
      error: (error) => {
        let errorMessage = 'Impossible d\'ajouter le produit au panier';
        if (error.status === 401 || error.status === 403) {
          errorMessage = 'Veuillez vous connecter pour synchroniser votre panier avec le serveur';
        }
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: errorMessage,
          life: 5000
        });
      }
    });
  }

  public onAddToWishlist(product: Product) {
    this.wishlistService.addToWishlist(product.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Produit ajouté aux favoris'
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible d\'ajouter le produit aux favoris'
        });
      }
    });
  }

  public onRemoveFromWishlist(product: Product) {
    const wishlistItem = this.wishlistService.wishlistItems().find(item => item.product.id === product.id);
    if (wishlistItem) {
      this.wishlistService.removeFromWishlist(wishlistItem.id, product.id).subscribe({
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
            detail: 'Impossible de retirer le produit des favoris'
          });
        }
      });
    }
  }

  public isInWishlist(productId: number): boolean {
    return this.wishlistService.isInWishlist(productId);
  }
  
  public onPageChange(event: any) {
    this.first.set(event.first);
    this.rows.set(event.rows);
    this.loadProducts();
  }
  
  public onCategoryChange() {
    this.first.set(0);
    this.loadProducts();
  }
  
  public onSearch() {
    this.first.set(0);
    this.searchSubject.next(this.searchText());
  }
  
  public onSearchInput() {
    this.searchSubject.next(this.searchText());
  }
}
