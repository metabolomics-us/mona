/**
 * Created by wohlgemuth
 *
 * simple service to help with available tags
 */

import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable, Inject} from "@angular/core";
import {downgradeInjectable} from "@angular/upgrade/static";
import { environment } from "../../environments/environment";
import * as  angular from 'angular';

export class Statistics {
    constructor(@Inject(HttpClient) private http: HttpClient) {
    }

    private cleanParameters = (data: Object) : HttpParams => {
        let params = new HttpParams();
        Object.keys(data).forEach(k => {data[k] ? params = params.set(k, data[k]): {}});

        return params;
    }

    executionTime = (data: Object): Promise<Object> => {
        let params = this.cleanParameters(data);

        let api = `${environment.REST_BACKEND_SERVER}/rest/statistics/category/${params["method"]}/${params["time"]}`;
        return this.http.get(api, {params: params}).toPromise();
    }

    pendingJobs = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/jobs/pending`).toPromise();
    }

    spectraCount = (data: Object): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/submitters/count/${data["id"]}`).toPromise();
    }

    spectraTopScores = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/submitters`).toPromise();
    }
}

angular.module('moaClientApp')
    .factory('StatisticsService', downgradeInjectable(Statistics));
