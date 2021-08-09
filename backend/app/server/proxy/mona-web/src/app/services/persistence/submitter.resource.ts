/**
 * Created by Gert on 5/28/2014.
 */
import {HttpClient} from "@angular/common/http";
import { environment } from "../../../environments/environment";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable()
export class Submitter {
    constructor(public http: HttpClient) {
    }

    get = (): Observable<any> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/submitters`);
    }

    update = (data: any): Observable<any> => {
        return this.http.put(`${environment.REST_BACKEND_SERVER}/rest/submitters/${data.id}`, data);
    }

    delete = (data: any): Observable<any> => {
        return this.http.delete(`${environment.REST_BACKEND_SERVER}/rest/submitters/${data.id}`, data);
    }
}
