/**
 * Created by sajjan on 10/17/2017.
 */
import {HttpClient} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable()
export class Download{
    constructor(public http: HttpClient) {
    }

    getPredefinedQueries = (): Observable<any> => {
        const api = `${environment.REST_BACKEND_SERVER}/rest/downloads/predefined`;
        return this.http.get(api);
    }

    getStaticDownloads = (): Observable<any> => {
        const api = `${environment.REST_BACKEND_SERVER}/rest/downloads/static`;
        return this.http.get(api);
    }
}
