/**
 * Created by sajjan on 10/17/2017.
 */
import {HttpClient} from "@angular/common/http";
import {Injectable, Inject} from "@angular/core";
import {downgradeInjectable} from "@angular/upgrade/static";
import { environment } from "../../environments/environment";
import * as  angular from 'angular';

export class Download{
    constructor(@Inject(HttpClient) private http: HttpClient) {
    }

    getPredefinedQueries = (): Promise<Object> => {
        const api = `${environment.REST_BACKEND_SERVER}/rest/downloads/predefined`;
        return this.http.get(api).toPromise();
    }

    getStaticDownloads = (): Promise<Object> => {
        const api = `${environment.REST_BACKEND_SERVER}/rest/downloads/static`;
        return this.http.get(api).toPromise();
    }
}

angular.module('moaClientApp')
    .factory('DownloadService', downgradeInjectable(Download));


