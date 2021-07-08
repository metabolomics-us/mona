/**
 * Created by wohlgemuth on 6/9/14.
 * simple service to help with available tags
 */

import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable, Inject} from "@angular/core";
import { map } from 'rxjs/operators';
import {downgradeInjectable} from "@angular/upgrade/static";
import { environment } from "../../environments/environment";
import * as  angular from 'angular';

export class TagService {
    constructor(@Inject(HttpClient) private http: HttpClient) {
    }

    query = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/tags/library`).toPromise();
    }
}

angular.module('moaClientApp')
    .factory('TagService', downgradeInjectable(TagService));
