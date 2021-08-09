/**
 * Created by wohlgemuth on 6/9/14.
 * simple service to help with available tags
 */

import {HttpClient} from "@angular/common/http";
import { environment } from "../../../environments/environment";
import {Injectable} from "@angular/core";

@Injectable()
export class TagService {
    constructor(public http: HttpClient) {
    }

    query = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/tags/library`).toPromise();
    }
}
