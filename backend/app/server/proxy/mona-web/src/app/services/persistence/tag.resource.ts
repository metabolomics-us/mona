/**
 * Created by wohlgemuth on 6/9/14.
 * simple service to help with available tags
 */

import {HttpClient} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

@Injectable()
export class TagService {
    constructor(public http: HttpClient) {
    }

    query = (): Observable<any> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/tags/library`);
    }
}
