export interface NavItem {
  label: string;
  route: string;
  icon: string;
}

export interface Account {
  name: string;
  type: string;
  routingNumber: string;
}

export interface AccountBalance {
  date: string;
  account: string;
  openingBalance: number;
  availableBalance: number;
  runningBalance: number;
  closingBalance: number;
}

interface ThresholdSettings {
  amount: number;
  email: string[];
  sms: string[];
}

export interface ForecastSettingsPayload {
  account: string;
  forecastRiskWarning: boolean;
  lowRiskThreshold: ThresholdSettings;
  highRiskThreshold: ThresholdSettings;
  stopPaymentThreshold: ThresholdSettings;
}

export interface ThresholdLevel {
  level: string;
  thresholdAmount: number;
  accountBalance: number;
  isSMSEnabled: boolean;
  isEmailEnabled: boolean;
}

export interface RoutingNumber {
  bankName: string;
  number: string;
}
