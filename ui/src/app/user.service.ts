import {Injectable} from '@angular/core';
import {Observable} from 'rxjs'
import {Currencies, Currency, GraphQLResponse, User} from './app.model';
import {HttpClient} from '@angular/common/http';
import {environment} from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private httpEndpoint = environment.http_endpoint;

  constructor(private httpClient: HttpClient) {}

  findUser(email: string, what: string[]): Observable<GraphQLResponse<{ findClient: User }>> {
    const body = {
      query: 'query FetchName($email: String!){ findClient (email: $email){' + what.join(', ') + '}}',
      variables: {
        email: email
      }
    };
    return this.httpClient.post<GraphQLResponse<{ findClient: User }>>(this.httpEndpoint, body);
  }

  saveUser(user: User): Observable<GraphQLResponse<{saveUser: string}>> {
    const body = {
      query: 'mutation SAVE($firstName: String!, $lastName: String!, $email: String!, $dashboardCurrencies: [String!]!)' +
        '{ saveUser (firstName: $firstName, lastName: $lastName, email: $email, dashboardCurrencies: $dashboardCurrencies)}',
      variables: {
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        dashboardCurrencies: user.dashboardCurrencies
      }
    };
    return this.httpClient.post<GraphQLResponse<{saveUser: string}>>(this.httpEndpoint, body);
  }

}
