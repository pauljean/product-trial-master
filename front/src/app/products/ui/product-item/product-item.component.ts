import { Component, input, output, computed, OnInit } from "@angular/core";
import { Product } from "../../data-access/product.model";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { InputNumberModule } from "primeng/inputnumber";
import { FormsModule } from "@angular/forms";
import { TagModule } from "primeng/tag";
import { CommonModule } from "@angular/common";
import { TooltipModule } from "primeng/tooltip";

@Component({
  selector: "app-product-item",
  templateUrl: "./product-item.component.html",
  styleUrls: ["./product-item.component.scss"],
  standalone: true,
  imports: [CardModule, ButtonModule, InputNumberModule, FormsModule, TagModule, CommonModule, TooltipModule],
})
export class ProductItemComponent implements OnInit {
  public readonly product = input.required<Product>();
  public readonly showQuantityControl = input<boolean>(false);
  public readonly quantity = input<number>(1);
  public readonly isInWishlist = input<boolean>(false);
  
  public readonly addToCart = output<{ product: Product; quantity: number }>();
  public readonly quantityChange = output<number>();
  public readonly removeFromCart = output<Product>();
  public readonly addToWishlist = output<Product>();
  public readonly removeFromWishlist = output<Product>();

  public readonly currentQuantity = computed(() => this.quantity());
  public _localQuantity = 1;

  ngOnInit() {
    this._localQuantity = this.quantity();
  }

  public onAddToCart() {
    this.addToCart.emit({ product: this.product(), quantity: this._localQuantity });
  }

  public onQuantityChange(value: number | string | null) {
    const numValue = typeof value === 'number' ? value : (value ? Number(value) : 1);
    this._localQuantity = numValue;
    this.quantityChange.emit(numValue);
  }

  public onRemoveFromCart() {
    this.removeFromCart.emit(this.product());
  }

  public onToggleWishlist() {
    if (this.isInWishlist()) {
      this.removeFromWishlist.emit(this.product());
    } else {
      this.addToWishlist.emit(this.product());
    }
  }

  public getInventoryStatusSeverity(status: string): "success" | "warning" | "danger" | undefined {
    switch (status) {
      case "INSTOCK":
        return "success";
      case "LOWSTOCK":
        return "warning";
      case "OUTOFSTOCK":
        return "danger";
      default:
        return undefined;
    }
  }

  public getInventoryStatusLabel(status: string): string {
    switch (status) {
      case "INSTOCK":
        return "En stock";
      case "LOWSTOCK":
        return "Stock faible";
      case "OUTOFSTOCK":
        return "Rupture de stock";
      default:
        return status;
    }
  }
}
