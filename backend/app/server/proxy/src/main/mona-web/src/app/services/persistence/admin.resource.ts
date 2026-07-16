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

  // Recomputes tag statistics from live data and returns the refreshed library tags
  refreshLibraries(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      }
    };
    return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/tags/library/refresh`, null, config);
  }

  refreshSimilarity(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token,
      },
      responseType: 'text' as 'json'
    };
    return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/similarity/refresh`, null, config);
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

  // Removes predefined library downloads whose library no longer exists
  reconcilePredefinedDownloads(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      }
    };
    return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/downloads/predefined/reconcile`, null, config);
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

  reCurateUserData(token: any, query: string): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      responseType: 'text' as 'json'
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/curation/`, { ...config, params: {query} } );
  }

  fetchUser(token: any, emailAddress: string): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      }
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/users/${emailAddress}`, config);
  }

  submitPasswordChange(token: any, user: any) {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      }
    };
    return this.http.put(`${environment.REST_BACKEND_SERVER}/rest/users/${user.emailAddress}`, user, config);
  }

  // Lists the known service keys for the diagnostics page's service selector
  getDiagnosticServices(token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      }
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/diagnostics/services`, config);
  }

  // Fetches recent ERROR-level log lines for the given service
  getDiagnosticLogs(token: any, service: string, hours: number): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      params: {service, hours: hours.toString()}
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/diagnostics/logs`, config);
  }

  // Fetches the stack trace/continuation lines following a single error entry
  getDiagnosticLogTrace(token: any, service: string, timestamp: number): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      params: {service, timestamp: timestamp.toString()},
      responseType: 'text' as 'json'
    };
    return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/diagnostics/logs/trace`, config);
  }
}
