export interface GraphQLResponse<T> {
  data: T
}

export interface Currencies {
  currencies: Currency[]
}

export interface Currency {
  code?: string,
  currencyName?: string
}

export interface User {
  firstName: string;
  lastName: string;
  email: string;
  dashboardCurrencies: string[];
}
