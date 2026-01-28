import {
  Component,
  computed,
  effect,
  EventEmitter,
  input,
  Output,
  ViewEncapsulation,
} from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { Product } from "app/products/data-access/product.model";
import { SelectItem } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { InputTextareaModule } from 'primeng/inputtextarea';

@Component({
  selector: "app-product-form",
  template: `
    <form [formGroup]="productForm" (ngSubmit)="onSave()">
      <div class="form-field">
        <label for="code">Code produit <span class="required">*</span></label>
        <input pInputText
          type="text"
          id="code"
          formControlName="code"
          [class.ng-invalid]="code?.invalid && (code?.touched || code?.dirty)" />
        @if (code?.hasError('required') && (code?.touched || code?.dirty)) {
          <small class="p-error block mt-1">Le code produit est obligatoire</small>
        }
        @if (code?.hasError('maxlength') && (code?.touched || code?.dirty)) {
          <small class="p-error block mt-1">Le code ne doit pas dépasser 100 caractères</small>
        }
      </div>
      <div class="form-field">
        <label for="name">Nom <span class="required">*</span></label>
        <input pInputText
          type="text"
          id="name"
          formControlName="name"
          [class.ng-invalid]="name?.invalid && (name?.touched || name?.dirty)" />
        @if (name?.hasError('required') && (name?.touched || name?.dirty)) {
          <small class="p-error block mt-1">Le nom est obligatoire</small>
        }
        @if (name?.hasError('maxlength') && (name?.touched || name?.dirty)) {
          <small class="p-error block mt-1">Le nom ne doit pas dépasser 255 caractères</small>
        }
      </div>
      <div class="form-field">
        <label for="price">Prix <span class="required">*</span></label>
        <p-inputNumber 
          formControlName="price"
          mode="decimal"
          [min]="0.01"
          [step]="0.01"
          [class.ng-invalid]="price?.invalid && (price?.touched || price?.dirty)" /> 
        @if (price?.hasError('required') && (price?.touched || price?.dirty)) {
          <small class="p-error block mt-1">Le prix est obligatoire</small>
        }
        @if (price?.hasError('min') && (price?.touched || price?.dirty)) {
          <small class="p-error block mt-1">Le prix doit être supérieur à 0.01</small>
        }
      </div>
      <div class="form-field">
        <label for="quantity">Quantité <span class="required">*</span></label>
        <p-inputNumber 
          formControlName="quantity"
          [min]="0"
          [class.ng-invalid]="quantity?.invalid && (quantity?.touched || quantity?.dirty)" /> 
        @if (quantity?.hasError('required') && (quantity?.touched || quantity?.dirty)) {
          <small class="p-error block mt-1">La quantité est obligatoire</small>
        }
        @if (quantity?.hasError('min') && (quantity?.touched || quantity?.dirty)) {
          <small class="p-error block mt-1">La quantité doit être supérieure ou égale à 0</small>
        }
      </div>
      <div class="form-field">
        <label for="category">Catégorie <span class="required">*</span></label>
        <p-dropdown 
          [options]="categories" 
          formControlName="category"
          appendTo="body"
          [class.ng-invalid]="category?.invalid && (category?.touched || category?.dirty)" />
        @if (category?.hasError('required') && (category?.touched || category?.dirty)) {
          <small class="p-error block mt-1">La catégorie est obligatoire</small>
        }
      </div>
      <div class="form-field">
        <label for="inventoryStatus">Statut d'inventaire <span class="required">*</span></label>
        <p-dropdown 
          [options]="inventoryStatuses" 
          formControlName="inventoryStatus"
          appendTo="body"
          [class.ng-invalid]="inventoryStatus?.invalid && (inventoryStatus?.touched || inventoryStatus?.dirty)" />
        @if (inventoryStatus?.hasError('required') && (inventoryStatus?.touched || inventoryStatus?.dirty)) {
          <small class="p-error block mt-1">Le statut d'inventaire est obligatoire</small>
        }
      </div>
      <div class="form-field">
        <label for="description">Description</label>
        <textarea pInputTextarea 
          id="description"
          formControlName="description"
          rows="5" 
          cols="30"></textarea>
      </div>
      <div class="form-field">
        <label for="image">Image (URL)</label>
        <input pInputText
          type="text"
          id="image"
          formControlName="image" />
      </div>
      <div class="flex justify-content-between">
        <p-button type="button" (click)="onCancel()" label="Annuler" severity="help"/>
        <p-button type="submit" [disabled]="productForm.invalid" label="Enregistrer" severity="success"/>
      </div>
    </form>
  `,
  styleUrls: ["./product-form.component.scss"],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    InputTextareaModule,
    DropdownModule,
  ],
  encapsulation: ViewEncapsulation.None
})
export class ProductFormComponent {
  public readonly product = input.required<Product>();

  @Output() cancel = new EventEmitter<void>();
  @Output() save = new EventEmitter<Product>();

  private readonly fb = new FormBuilder();

  public productForm: FormGroup;

  public readonly categories: SelectItem[] = [
    { value: "Accessories", label: "Accessories" },
    { value: "Fitness", label: "Fitness" },
    { value: "Clothing", label: "Clothing" },
    { value: "Electronics", label: "Electronics" },
  ];

  public readonly inventoryStatuses: SelectItem[] = [
    { value: "INSTOCK", label: "En stock" },
    { value: "LOWSTOCK", label: "Stock faible" },
    { value: "OUTOFSTOCK", label: "Rupture de stock" },
  ];

  constructor() {
    this.productForm = this.fb.group({
      code: ['', [Validators.required, Validators.maxLength(100)]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      price: [0, [Validators.required, Validators.min(0.01)]],
      quantity: [0, [Validators.required, Validators.min(0)]],
      category: ['Accessories', [Validators.required]],
      inventoryStatus: ['INSTOCK', [Validators.required]],
      description: [''],
      image: ['']
    });

    // Mettre à jour le formulaire quand le produit change
    effect(() => {
      const product = this.product();
      if (product) {
        this.productForm.patchValue({
          code: product.code || '',
          name: product.name || '',
          price: product.price || 0,
          quantity: product.quantity || 0,
          category: product.category || 'Accessories',
          inventoryStatus: product.inventoryStatus || 'INSTOCK',
          description: product.description || '',
          image: product.image || ''
        });
      }
    });
  }

  public get code() {
    return this.productForm.get('code');
  }

  public get name() {
    return this.productForm.get('name');
  }

  public get price() {
    return this.productForm.get('price');
  }

  public get quantity() {
    return this.productForm.get('quantity');
  }

  public get category() {
    return this.productForm.get('category');
  }

  public get inventoryStatus() {
    return this.productForm.get('inventoryStatus');
  }

  onCancel() {
    this.cancel.emit();
  }

  onSave() {
    if (this.productForm.valid) {
      const formValue = this.productForm.value;
      const product: Product = {
        ...this.product(),
        ...formValue,
        // S'assurer que les valeurs numériques sont correctes
        price: Number(formValue.price) || 0,
        quantity: Number(formValue.quantity) || 0,
        rating: this.product().rating || 0,
        createdAt: this.product().createdAt || Date.now(),
        updatedAt: Date.now()
      };
      this.save.emit(product);
    } else {
      this.productForm.markAllAsTouched();
    }
  }
}
