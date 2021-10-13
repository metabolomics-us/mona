/**
 * Created by Gert on 5/28/2014.
 */
import {HttpClient} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable()
export class Submitter {
    constructor(public http: HttpClient) {
    }

    get(token): Observable<any> {
        const config = {
          headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + token
          }
        };
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/submitters`, config);
    }

    update(data: any, token): Observable<any> {
      const config = {
        headers: {
          'Content-Type': 'application/json',
          Authorization: 'Bearer ' + token
        }
      };
      return this.http.put(`${environment.REST_BACKEND_SERVER}/rest/submitters/${data.id}`, data, config);
    }

    delete(data: any, token): Observable<any> {
      const config = {
        headers: {
          'Content-Type': 'application/json',
          Authorization: 'Bearer ' + token
        }
      };
      return this.http.delete(`${environment.REST_BACKEND_SERVER}/rest/submitters/${data.id}`, config);
    }
}
