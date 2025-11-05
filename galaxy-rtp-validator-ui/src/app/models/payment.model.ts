export interface PaymentInfoRequest {
  senderRoutingNumber: string;
  receiverRoutingNumber: string;
  senderAccountNumber: string;
  receiverAccountNumber: string;
  amount: string;
  currency: string;
  templateName?: string;
}

export interface PaymentTemplate {
  id: number;
  templateName: string;
  senderRoutingNumber: string;
  receiverRoutingNumber: string;
  senderAccountNumber: string;
  receiverAccountNumber: string;
  amount: string;
  currency: string;
  createdAt: string;
  updatedAt: string;
}

