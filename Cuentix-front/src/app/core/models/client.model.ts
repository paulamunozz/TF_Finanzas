export type RateType = 'TN' | 'TE';

export interface Client {
  id: string;
  dni: string;
  name: string;
  currency: string;
  compensatoryRate: number;
  rateType: RateType;
  ratePeriod: 'Mensual' | 'Diaria';
  capitalizationPeriod?: 'Diaria' | 'Mensual';
  lateRate: number;
  lateRateType: RateType;
  maxMonths: number;
  creditLine: number;
  cutOffDay: number;
  paymentDay: number;
}

export type CreateClientDto = Omit<Client, 'id'>;
export type UpdateClientDto = CreateClientDto;
