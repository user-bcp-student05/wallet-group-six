import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  LucideArrowLeft,
  LucideArrowRight,
  LucideBadgeCheck,
  LucideCheck,
  LucideCheckCircle2,
  LucideCircleHelp,
  LucideDownload,
  LucideEye,
  LucideEyeOff,
  LucideLockKeyhole,
  LucideLogOut,
  LucideMail,
  LucidePhone,
  LucideShieldCheck,
  LucideSmartphone,
  LucideUserRound,
} from '@lucide/angular';
import {
  ContactTransferApi,
  FinancialEntity,
  TransferExecutionResponse,
  TransferVerificationResponse,
} from './contact-transfer.api';

type JourneyStep = 'login' | 'affiliation' | 'phone' | 'entities' | 'verify' | 'confirm' | 'success';

interface TransferStep {
  id: JourneyStep;
  label: string;
}

const BCP_LOGO_URL = 'https://upload.wikimedia.org/wikipedia/commons/0/0d/Logo-bcp-vector.svg';

@Component({
  selector: 'app-root',
  imports: [
    FormsModule,
    LucideArrowLeft,
    LucideArrowRight,
    LucideBadgeCheck,
    LucideCheck,
    LucideCheckCircle2,
    LucideCircleHelp,
    LucideDownload,
    LucideEye,
    LucideEyeOff,
    LucideLockKeyhole,
    LucideLogOut,
    LucideMail,
    LucidePhone,
    LucideShieldCheck,
    LucideSmartphone,
    LucideUserRound,
  ],
  templateUrl: './wallet.html',
  styleUrl: './app.css',
})
export class App {
  readonly step = signal<JourneyStep>('login');
  readonly busy = signal(false);
  readonly errorMessage = signal('');
  readonly notice = signal('');
  readonly showPassword = signal(false);
  readonly entities = signal<FinancialEntity[]>([]);
  readonly selectedEntity = signal<FinancialEntity | null>(null);
  readonly verification = signal<TransferVerificationResponse | null>(null);
  readonly transferResult = signal<TransferExecutionResponse | null>(null);
  readonly originPhone = signal(
    typeof sessionStorage === 'undefined' ? '' : sessionStorage.getItem('wallet-origin-phone') ?? '',
  );
  readonly transferSteps: TransferStep[] = [
    { id: 'phone', label: 'Destinatario' },
    { id: 'entities', label: 'Entidad' },
    { id: 'verify', label: 'Monto' },
    { id: 'confirm', label: 'Confirmar' },
    { id: 'success', label: 'Constancia' },
  ];

  loginPhone = '';
  loginPassword = '';
  recipientPhone = '';
  amount = '';
  reference = '';
  documentType = 'DNI';
  documentNumber = '';
  affiliationPhone = '';
  email = '';
  password = '';

  constructor(private readonly api: ContactTransferApi) {
    this.loginPhone = this.originPhone().replace(/^\+51/, '');
  }

  get isAuthScreen(): boolean {
    return this.step() === 'login' || this.step() === 'affiliation';
  }

  get activeTransferIndex(): number {
    return this.transferSteps.findIndex(({ id }) => id === this.step());
  }

  get formattedAmount(): string {
    const parsed = Number(this.amount);
    return Number.isFinite(parsed) ? parsed.toFixed(2) : '0.00';
  }

  get destinationEntity(): FinancialEntity | null {
    return this.selectedEntity() ?? this.verification()?.destination.financialEntity ?? null;
  }

  login(): void {
    this.clearMessages();
    if (!/^\d{9}$/.test(this.loginPhone)) {
      this.errorMessage.set('Ingresa un número de celular válido de 9 dígitos.');
      return;
    }
    if (this.loginPassword.length < 4 || this.loginPassword.length > 6) {
      this.errorMessage.set('Ingresa una contraseña de 4 a 6 caracteres.');
      return;
    }
    this.originPhone.set(this.toInternationalPhone(this.loginPhone));
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.setItem('wallet-origin-phone', this.originPhone());
    }
    this.step.set('phone');
  }

  logout(): void {
    this.clearMessages();
    this.originPhone.set('');
    this.loginPhone = '';
    this.loginPassword = '';
    if (typeof sessionStorage !== 'undefined') {
      sessionStorage.removeItem('wallet-origin-phone');
    }
    this.step.set('login');
  }

  openAffiliation(): void {
    this.clearMessages();
    this.step.set('affiliation');
  }

  async affiliate(): Promise<void> {
    this.clearMessages();
    this.busy.set(true);
    try {
      await this.api.affiliateWallet({
        documentType: this.documentType as 'DNI' | 'CE' | 'PASSPORT',
        documentNumber: this.encodeSensitiveValue(this.documentNumber.trim()),
        phone: this.encodeSensitiveValue(this.toInternationalPhone(this.affiliationPhone)),
        email: this.encodeSensitiveValue(this.email.trim()),
        password: this.encodeSensitiveValue(this.password),
      });
      this.password = '';
      this.notice.set('Tu solicitud de afiliación fue completada. Ingresa para continuar.');
      this.step.set('login');
    } catch (error) {
      this.showApiError(error, 'No pudimos completar la afiliación. Revisa tus datos e inténtalo otra vez.');
    } finally {
      this.busy.set(false);
    }
  }

  async searchRecipient(): Promise<void> {
    this.clearMessages();
    if (!/^\d{9}$/.test(this.recipientPhone)) {
      this.errorMessage.set('Ingresa un número de celular válido de 9 dígitos.');
      return;
    }
    this.busy.set(true);
    try {
      const phone = this.toInternationalPhone(this.recipientPhone);
      this.entities.set(await this.api.searchFinancialEntities({ phone: this.encodeSensitiveValue(phone) }));
      this.selectedEntity.set(null);
      this.step.set('entities');
    } catch (error) {
      this.showApiError(error, 'No encontramos entidades para este número. Verifícalo e inténtalo de nuevo.');
    } finally {
      this.busy.set(false);
    }
  }

  selectEntity(entity: FinancialEntity): void {
    this.selectedEntity.set(entity);
  }

  continueToAmount(): void {
    if (!this.selectedEntity()) {
      this.errorMessage.set('Selecciona una entidad financiera para continuar.');
      return;
    }
    this.clearMessages();
    this.step.set('verify');
  }

  async verifyTransfer(): Promise<void> {
    this.clearMessages();
    this.busy.set(true);
    try {
      this.verification.set(await this.api.verifyTransfer({ amount: Number(this.amount) }));
      this.step.set('confirm');
    } catch (error) {
      this.showApiError(error, 'No pudimos verificar la transferencia. Revisa el monto e inténtalo de nuevo.');
    } finally {
      this.busy.set(false);
    }
  }

  async confirmTransfer(): Promise<void> {
    this.clearMessages();
    this.busy.set(true);
    try {
      this.transferResult.set(await this.api.executeTransfer({ description: this.reference.trim() }));
      this.step.set('success');
    } catch (error) {
      this.showApiError(error, 'No se pudo completar la transferencia. Inténtalo nuevamente.');
    } finally {
      this.busy.set(false);
    }
  }

  goBack(): void {
    this.clearMessages();
    const previous: Partial<Record<JourneyStep, JourneyStep>> = {
      entities: 'phone',
      verify: 'entities',
      confirm: 'verify',
    };
    const target = previous[this.step()];
    if (target) this.step.set(target);
  }

  restart(): void {
    this.clearMessages();
    this.recipientPhone = '';
    this.amount = '';
    this.reference = '';
    this.selectedEntity.set(null);
    this.verification.set(null);
    this.transferResult.set(null);
    this.step.set('phone');
  }

  formatPhone(value: string): string {
    const digits = value.replace(/\D/g, '').replace(/^51/, '');
    return `+51 ${digits.slice(0, 3)} ${digits.slice(3, 6)} ${digits.slice(6, 9)}`.trim();
  }

  formatDate(value: string | undefined): string {
    if (!value) return 'Hoy';
    return new Intl.DateTimeFormat('es-PE', {
      dateStyle: 'long',
      timeStyle: 'short',
      timeZone: 'America/Lima',
    }).format(new Date(value));
  }

  async downloadReceipt(): Promise<void> {
    const result = this.transferResult();
    const details = this.verification();
    if (!result || !details) return;
    const logoBytes = await this.createBcpLogoJpeg();
    const clean = (value: string): string =>
      value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^\x20-\x7E]/g, '');
    const escape = (value: string): string => clean(value).replace(/[\\()]/g, '\\$&');
    const entity = this.destinationEntity ?? details.destination.financialEntity;
    const entityColor = entity.entity.toLowerCase() === 'yape' ? '0.40 0.16 0.56' : '0.10 0.59 0.56';
    const text = (
      x: number,
      y: number,
      size: number,
      value: string,
      color = '0.12 0.18 0.27',
      font = '/F1',
    ): string => `BT ${font} ${size} Tf ${color} rg ${x} ${y} Td (${escape(value)}) Tj ET`;
    const rect = (x: number, y: number, width: number, height: number, color: string): string =>
      `${color} rg ${x} ${y} ${width} ${height} re f`;
    const rule = (y: number): string => `0.88 0.91 0.95 RG 0.8 w 48 ${y} m 547 ${y} l S`;
    const description = this.reference.trim() || 'Sin referencia';
    const content = [
      rect(0, 660, 595, 182, '0.03 0.18 0.45'),
      rect(48, 782, 88, 34, '1 1 1'),
      'q 78 0 0 20 53 789 cm /Logo Do Q',
      text(151, 801, 9, 'BILLETERA DIGITAL', '0.72 0.91 0.88', '/F2'),
      rect(430, 782, 112, 32, entityColor),
      text(448, 792, 13, entity.entity.toLowerCase(), '1 1 1', '/F2'),
      text(48, 738, 20, 'CONSTANCIA DE TRANSFERENCIA', '1 1 1', '/F2'),
      text(48, 716, 10, 'TRANSFERENCIA REALIZADA CON EXITO', '0.75 0.91 0.88'),
      text(48, 684, 8, 'CODIGO DE OPERACION', '0.73 0.82 0.93', '/F2'),
      text(48, 668, 11, result.operationCode, '1 1 1', '/F2'),
      text(48, 623, 9, 'MONTO ENVIADO', '0.44 0.51 0.61', '/F2'),
      text(48, 580, 31, `S/ ${this.formattedAmount}`, '0.03 0.24 0.57', '/F2'),
      rect(431, 584, 111, 25, '0.88 0.96 0.92'),
      text(447, 593, 9, 'OPERACION EXITOSA', '0.08 0.43 0.33', '/F2'),
      rule(550),
      text(48, 526, 8, 'ORIGEN', '0.44 0.51 0.61', '/F2'),
      rect(48, 474, 36, 36, '0.89 0.93 0.99'),
      text(60, 487, 14, details.origin.name.charAt(0) || 'O', '0.03 0.24 0.57', '/F2'),
      text(98, 497, 8, 'TITULAR DE LA BILLETERA', '0.52 0.58 0.66'),
      text(98, 481, 12, details.origin.name, '0.10 0.16 0.25', '/F2'),
      text(98, 465, 9, this.formatPhone(this.originPhone()), '0.44 0.51 0.61'),
      rule(447),
      text(48, 423, 8, 'DESTINO', '0.44 0.51 0.61', '/F2'),
      rect(48, 371, 36, 36, '0.99 0.92 0.88'),
      text(60, 384, 14, details.destination.name.charAt(0) || 'D', '0.68 0.34 0.23', '/F2'),
      text(98, 394, 8, 'DESTINATARIO', '0.52 0.58 0.66'),
      text(98, 378, 12, details.destination.name, '0.10 0.16 0.25', '/F2'),
      text(98, 362, 9, this.formatPhone(this.recipientPhone), '0.44 0.51 0.61'),
      text(98, 346, 9, `Billetera ${entity.entity}`, entityColor, '/F2'),
      rule(327),
      text(48, 303, 8, 'DETALLE DEL PAGO', '0.44 0.51 0.61', '/F2'),
      text(48, 280, 9, 'Referencia', '0.52 0.58 0.66'),
      text(150, 280, 9, description.slice(0, 66), '0.10 0.16 0.25'),
      text(48, 257, 9, 'Fecha y hora', '0.52 0.58 0.66'),
      text(150, 257, 9, this.formatDate(result.transactionDateTime), '0.10 0.16 0.25'),
      rule(88),
      text(48, 65, 8, 'BCP  |  BILLETERA DIGITAL', '0.44 0.51 0.61', '/F2'),
      text(390, 65, 8, 'CONSTANCIA DIGITAL', '0.44 0.51 0.61', '/F2'),
    ].join('\n');
    const objects = [
      '<< /Type /Catalog /Pages 2 0 R >>',
      '<< /Type /Pages /Kids [3 0 R] /Count 1 >>',
      '<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> /XObject << /Logo 7 0 R >> >> /Contents 6 0 R >>',
      '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>',
      '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>',
      `<< /Length ${content.length} >>\nstream\n${content}\nendstream`,
    ];
    const imageHeader = new TextEncoder().encode(
      `<< /Type /XObject /Subtype /Image /Width 512 /Height 129 /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${logoBytes.length} >>\nstream\n`,
    );
    const imageEnd = new TextEncoder().encode('\nendstream');
    const imageObject = new Uint8Array(imageHeader.length + logoBytes.length + imageEnd.length);
    imageObject.set(imageHeader);
    imageObject.set(logoBytes, imageHeader.length);
    imageObject.set(imageEnd, imageHeader.length + logoBytes.length);
    const pdfObjects: Array<string | Uint8Array> = [...objects, imageObject];
    const encoder = new TextEncoder();
    const chunks: Uint8Array[] = [];
    let byteLength = 0;
    const append = (value: string | Uint8Array): void => {
      const chunk = typeof value === 'string' ? encoder.encode(value) : value;
      chunks.push(chunk);
      byteLength += chunk.length;
    };
    append('%PDF-1.4\n');
    const offsets: number[] = [];
    for (const [index, object] of pdfObjects.entries()) {
      offsets.push(byteLength);
      append(`${index + 1} 0 obj\n`);
      append(object);
      append('\nendobj\n');
    }
    const crossReferenceOffset = byteLength;
    append(`xref\n0 ${pdfObjects.length + 1}\n0000000000 65535 f \n`);
    for (const offset of offsets) {
      append(`${String(offset).padStart(10, '0')} 00000 n \n`);
    }
    append(`trailer\n<< /Size ${pdfObjects.length + 1} /Root 1 0 R >>\nstartxref\n${crossReferenceOffset}\n%%EOF`);
    const pdfBytes = new Uint8Array(byteLength);
    let byteOffset = 0;
    for (const chunk of chunks) {
      pdfBytes.set(chunk, byteOffset);
      byteOffset += chunk.length;
    }
    const url = URL.createObjectURL(new Blob([pdfBytes.buffer], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = `constancia-${result.operationCode}.pdf`;
    document.body.append(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  private async createBcpLogoJpeg(): Promise<Uint8Array> {
    const response = await fetch(BCP_LOGO_URL);
    if (!response.ok) throw new Error('No se pudo cargar el logo BCP para la constancia.');

    const imageUrl = URL.createObjectURL(await response.blob());
    try {
      const image = new Image();
      image.src = imageUrl;
      await image.decode();
      const canvas = document.createElement('canvas');
      canvas.width = 512;
      canvas.height = 129;
      const context = canvas.getContext('2d');
      if (!context) throw new Error('No se pudo preparar el logo BCP para la constancia.');
      context.fillStyle = '#ffffff';
      context.fillRect(0, 0, canvas.width, canvas.height);
      context.drawImage(image, 0, 0, canvas.width, canvas.height);
      const encoded = canvas.toDataURL('image/jpeg', 0.96).split(',')[1];
      const binary = atob(encoded);
      return Uint8Array.from(binary, (character) => character.charCodeAt(0));
    } finally {
      URL.revokeObjectURL(imageUrl);
    }
  }

  private toInternationalPhone(value: string): string {
    const digits = value.replace(/\D/g, '');
    return digits.startsWith('51') ? `+${digits}` : `+51${digits}`;
  }

  private encodeSensitiveValue(value: string): string {
    const binary = Array.from(new TextEncoder().encode(value), (byte) => String.fromCharCode(byte)).join('');
    return btoa(binary);
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.notice.set('');
  }

  private showApiError(error: unknown, fallback: string): void {
    if (error instanceof HttpErrorResponse && error.status === 0) {
      this.errorMessage.set('No hay conexión con el servicio local. Inicia el backend Quarkus en el puerto 8080.');
      return;
    }
    const description = error instanceof HttpErrorResponse && typeof error.error?.description === 'string'
      ? error.error.description
      : '';
    this.errorMessage.set(description || fallback);
  }
}
