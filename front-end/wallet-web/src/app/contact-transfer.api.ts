import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface FinancialEntity {
  code: string;
  entity: string;
}

export interface WalletAffiliationRequest {
  documentType: 'DNI' | 'CE' | 'PASSPORT';
  documentNumber: string;
  phone: string;
  email: string;
  password: string;
}

export interface TransferVerificationResponse {
  origin: { name: string };
  destination: { name: string; financialEntity: FinancialEntity };
}

export interface TransferExecutionResponse {
  operationCode: string;
  transactionDateTime: string;
}

@Injectable({ providedIn: 'root' })
export class ContactTransferApi {
  private readonly baseUrl = '/api';
  private readonly sessionId = `wallet-demo-${crypto.randomUUID()}`;

  constructor(private readonly http: HttpClient) {}

  affiliateWallet(payload: WalletAffiliationRequest): Promise<void> {
    return firstValueFrom(this.http.post<void>(`${this.baseUrl}/affiliation-wallet`, payload, { headers: this.headers() }));
  }

  searchFinancialEntities(payload: { phone: string }): Promise<FinancialEntity[]> {
    return firstValueFrom(this.http.post<FinancialEntity[]>(`${this.baseUrl}/contact-transfers/financial-entities/search`, payload, { headers: this.headers() }));
  }

  verifyTransfer(payload: { amount: number }): Promise<TransferVerificationResponse> {
    return firstValueFrom(this.http.post<TransferVerificationResponse>(`${this.baseUrl}/contact-transfers/authorization/verify`, payload, { headers: this.headers() }));
  }

  executeTransfer(payload: { description: string }): Promise<TransferExecutionResponse> {
    return firstValueFrom(this.http.post<TransferExecutionResponse>(`${this.baseUrl}/contact-transfers/execute`, payload, { headers: this.headers() }));
  }

  private headers(): HttpHeaders {
    return new HttpHeaders({
      Authorization: 'Bearer local-demo-token',
      autentication: 'local-demo',
      sessionId: this.sessionId,
    });
  }
}
