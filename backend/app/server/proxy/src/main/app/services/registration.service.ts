import * as angular from 'angular';
import {downgradeInjectable} from "@angular/upgrade/static";
import {HttpClient} from "@angular/common/http";
import {NewSubmitter} from "../mocks/newSubmitter.model";
import {Inject} from "@angular/core";
import {environment} from "../environments/environment";
import {map} from "rxjs/operators";

export class RegistrationService {
    public newSubmitter: NewSubmitter;

    constructor(@Inject(HttpClient) private http: HttpClient) {
        this.newSubmitter = new NewSubmitter();
    };

    submit() {
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/users`,
            {username: this.newSubmitter.emailAddress,
             password: this.newSubmitter.password},
            {headers: {
                'Content-Type': 'application/json'
            }
            });

    }

    authorize() {
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/auth/login`,
            {username: this.newSubmitter.emailAddress,
             password: this.newSubmitter.password},
            {headers: {
                'Content-Type': 'application/json'
                }});
    }

    registerAsSubmitter(token) {
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/submitters`,
            {
                emailAddress: this.newSubmitter.emailAddress,
                firstName: this.newSubmitter.firstName,
                lastName: this.newSubmitter.lastName,
                institution: this.newSubmitter.institution
            },
            {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }).pipe(map((x) => {
                this.newSubmitter = new NewSubmitter();
        }))
    }
}


angular.module('moaClientApp')
    .factory('RegistrationService', downgradeInjectable(RegistrationService));
