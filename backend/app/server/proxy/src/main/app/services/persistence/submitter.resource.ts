/**
 * Created by Gert on 5/28/2014.
 */
import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable, Inject} from "@angular/core";
import { map } from 'rxjs/operators';
import {downgradeInjectable} from "@angular/upgrade/static";
import { environment } from "../../environments/environment";
import * as  angular from 'angular';

export class Submitter {
    constructor(@Inject(HttpClient) private http: HttpClient) {
    }

    update = (data: Object): Promise<Object> => {
        return this.http.put(`${environment.REST_BACKEND_SERVER}/rest/submitters/${data["id"]}`, data).toPromise();
    }

}

angular.module('moaClientApp')
    .factory('Submitter', downgradeInjectable(Submitter));

