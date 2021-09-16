/**
 * Created by wohlgemuth
 *
 * simple service to help with available tags
 */

import {HttpClient, HttpParams} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

@Injectable()
export class Statistics {
    constructor(public http: HttpClient) {
    }

    private cleanParameters(data: any): any {
        let params = new HttpParams();
        Object.keys(data).forEach(k => {data[k] ? params = params.set(k, data[k]) : {}; });

        return params;
    }

    executionTime(data: any): Observable<any> {
        const params = this.cleanParameters(data);

        const api = `${environment.REST_BACKEND_SERVER}/rest/statistics/category/${params.method}/${params.time}`;
        return this.http.get(api, {params});
    }

    pendingJobs(): Observable<any> {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/jobs/pending`);
    }

    spectraCount(data: any): Observable<any> {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/submitters/count/${data.id}`);
    }

    spectraTopScores(): Observable<any> {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/submitters`);
    }
}
