import { Component, OnInit, inject, signal, computed } from "@angular/core";
import { Product } from "app/products/data-access/product.model";
import { ProductsService } from "app/products/data-access/products.service";
import { ProductFormComponent } from "app/products/ui/product-form/product-form.component";
import { ProductItemComponent } from "app/products/ui/product-item/product-item.component";
import { CartService } from "app/shared/data-access/cart.service";
import { AuthService } from "app/shared/data-access/auth.service";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { DataViewModule } from 'primeng/dataview';
import { DialogModule } from 'primeng/dialog';
import { PaginatorModule } from 'primeng/paginator';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

const emptyProduct: Product = {
  id: 0,
  code: "",
  name: "",
  description: "",
  image: "",
  category: "Accessories",
  price: 0,
  quantity: 0,
  internalReference: "",
  shellId: 0,
  inventoryStatus: "INSTOCK",
  rating: 0,
  createdAt: 0,
  updatedAt: 0,
};

@Component({
  selector: "app-product-list",
  templateUrl: "./product-list.component.html",
  styleUrls: ["./product-list.component.scss"],
  standalone: true,
  imports: [
    DataViewModule, 
    CardModule, 
    ButtonModule, 
    DialogModule, 
    ProductFormComponent,
    ProductItemComponent,
    PaginatorModule,
    InputTextModule,
    DropdownModule,
    FormsModule,
    ToastModule
  ],
  providers: [MessageService]
})
export class ProductListComponent implements OnInit {
  private readonly productsService = inject(ProductsService);
  private readonly cartService = inject(CartService);
  private readonly authService = inject(AuthService);
  private readonly messageService = inject(MessageService);

  public readonly products = this.productsService.products;
  public readonly isAdmin = computed(() => {
    const email = this.authService.getCurrentUserEmail();
    return email === 'admin@admin.com';
  });

  public isDialogVisible = false;
  public isCreation = false;
  public readonly editedProduct = signal<Product>(emptyProduct);

  ngOnInit() {
    if (!this.isAdmin()) {
      this.messageService.add({
        severity: 'error',
        summary: 'Accès refusé',
        detail: 'Cette page est réservée aux administrateurs'
      });
      return;
    }
    this.productsService.get().subscribe();
  }

  public onCreate() {
    this.isCreation = true;
    this.isDialogVisible = true;
    this.editedProduct.set(emptyProduct);
  }

  public onUpdate(product: Product) {
    this.isCreation = false;
    this.isDialogVisible = true;
    this.editedProduct.set(product);
  }

  public onDelete(product: Product) {
    this.productsService.delete(product.id).subscribe();
  }

  public onSave(product: Product) {
    if (this.isCreation) {
      this.productsService.create(product).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Produit créé avec succès'
          });
          this.closeDialog();
        },
        error: (error) => {
          let errorMessage = 'Erreur lors de la création du produit';
          if (error.error?.errors) {
            // Erreurs de validation
            const validationErrors = Object.values(error.error.errors).join(', ');
            errorMessage = `Erreurs de validation: ${validationErrors}`;
          } else if (error.error?.message) {
            errorMessage = error.error.message;
          }
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: errorMessage,
            life: 5000
          });
        }
      });
    } else {
      this.productsService.update(product).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Produit mis à jour avec succès'
          });
          this.closeDialog();
        },
        error: (error) => {
          let errorMessage = 'Erreur lors de la mise à jour du produit';
          if (error.error?.errors) {
            const validationErrors = Object.values(error.error.errors).join(', ');
            errorMessage = `Erreurs de validation: ${validationErrors}`;
          } else if (error.error?.message) {
            errorMessage = error.error.message;
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
  }

  public onCancel() {
    this.closeDialog();
  }
  

  private closeDialog() {
    this.isDialogVisible = false;
  }
}
