/**
 * Created by noguzman on 2/24/2022.
 * Allows signed in admin user to access admin api's
 */
import {HttpClient} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

@Injectable()
export class AdminService {
  constructor(public http: HttpClient) {
  }

  // Triggers job to update all statistics
  updateStatistics(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token,
      },
      responseType: 'text' as 'json'
    };
    return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/statistics/update`, null, config);
  }

  // Re-generates static downloads
  updateStaticDownloads(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      }
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/downloads/generateStatic`, config);
  }

  // Re-generates predefined downloads
  updatePredefinedDownloads(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      }
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/downloads/generatePredefined`, config);
  }

  reCurateAllData(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      responseType: 'text' as 'json'
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/curation`, config);
  }
}
