/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * simple service to help with available tags
 */
import {HttpClient} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

@Injectable()
export class Metadata {
    constructor(public http: HttpClient) {
    }

    metadata(): Observable<any> {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/data?max=100`);
    }

    categories(): Observable<any> {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/category?max=100`);
    }

    categoryData(): Observable<any> {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/category/data?max=100`);
    }

    dataValues(): Observable<any> {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/data/value?max=100`);
    }

    queryValues(data: any): Observable<any> {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            },
            params: {query: '@query'}
        };
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/meta/data/search?max=10`, data);
    }

    metaDataNames(): Observable<any> {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            },
            cache: true
        };
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/metaData/names`, config);
    }
}
