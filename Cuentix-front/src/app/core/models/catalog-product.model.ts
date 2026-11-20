export interface CatalogProduct {
  id: number;
  name: string;
  unit: string;
  active?: boolean;
  supplier?: string;
  brand?: string;
  description?: string;
  imageUrl?: string;
  installmentsAllowed?: boolean;
  cashPrice: number;
  creditPrice: number;
}
