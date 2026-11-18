export interface Product {
  name: string;
  quantity: number;
  unit: string;
}

export interface Installment {
  purchaseId: string;
  businessId: string;
  lateAnnualRate: number;
  id: string;
  clientId: string;
  purchaseDate: string;
  number: number;
  totalInstallments: number;
  dueDate: string;
  interest: number;
  amortization: number;
  installmentAmount: number;
  lateDays: number;
  lateAmount: number;
  cancelled: boolean;
  paidAt?: string;
  products: Product[];
}

/** Read model joining Compra, CuentaCredito and Item. No database schema changes. */
export interface PurchaseSchedule {
  id: string;
  accountId: string;
  clientId: string;
  businessId: string;
  currency: string;
  purchaseDate: string;
  principal: number;
  graceInterest: number;
  mode: 'single' | 'installments';
  installments: number;
  products: Product[];
}
