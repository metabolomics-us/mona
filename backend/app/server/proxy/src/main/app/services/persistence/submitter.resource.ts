/**
 * Created by Gert on 5/28/2014.
 */
import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable, Inject} from "@angular/core";
import { map } from 'rxjs/operators';
import {downgradeInjectable} from "@angular/upgrade/static";
import { environment } from "../../environments/environment";
import * as  angular from 'angular';
import {Observable} from "rxjs";

export class Submitter {
    constructor(@Inject(HttpClient) private http: HttpClient) {
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

angular.module('moaClientApp')
    .factory('Submitter', downgradeInjectable(Submitter));

