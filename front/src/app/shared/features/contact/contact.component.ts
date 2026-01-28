import { Component, inject } from "@angular/core";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { InputTextModule } from "primeng/inputtext";
import { InputTextareaModule } from "primeng/inputtextarea";
import { ToastModule } from "primeng/toast";
import { MessageModule } from "primeng/message";
import { CommonModule } from "@angular/common";
import { ContactService } from "../../data-access/contact.service";

@Component({
  selector: "app-contact",
  templateUrl: "./contact.component.html",
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    ToastModule,
    MessageModule,
    CommonModule
  ],
  providers: [MessageService]
})
export class ContactComponent {
  private readonly contactService = inject(ContactService);
  private readonly messageService = inject(MessageService);
  private readonly fb = inject(FormBuilder);

  public contactForm: FormGroup;
  public isSubmitted = false;
  public maxMessageLength = 300;

  constructor() {
    this.contactForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      message: ['', [Validators.required, Validators.maxLength(300)]]
    });
  }

  public get email() {
    return this.contactForm.get('email');
  }

  public get message() {
    return this.contactForm.get('message');
  }

  public get remainingChars(): number {
    return this.maxMessageLength - (this.message?.value?.length || 0);
  }

  public onSubmit() {
    this.isSubmitted = true;

    if (this.contactForm.valid) {
      this.contactService.sendContact(this.contactForm.value).subscribe({
        next: () => {
          this.isSubmitted = true;
          this.contactForm.reset();
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Demande de contact envoyée avec succès'
          });
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible d\'envoyer le message'
          });
        }
      });
    } else {
      this.contactForm.markAllAsTouched();
    }
  }

  public onReset() {
    this.isSubmitted = false;
    this.contactForm.reset();
  }
}
