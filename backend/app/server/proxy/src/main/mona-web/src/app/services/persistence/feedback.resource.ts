/**
 * Created by noguzman on 12/10/2021.
 */
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable()
export class Feedback {

  constructor(public http: HttpClient) {
  }

  get(id: string): Observable<any> {
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/feedback/${id}`);
  }

  save(payload: any): Observable<any> {
    return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/feedback`, payload);
  }
}
