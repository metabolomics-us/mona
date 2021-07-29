/**
 * Created by wohlgemuth on 6/9/14.
 * a service to handle authentications and provides us with the currently logged in user
 */

import {Submitter} from "./persistence/submitter.resource";
import {CookieMain} from "./cookie/cookie-main.service";
import {NGXLogger} from "ngx-logger";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {HttpClient} from "@angular/common/http";
import {Inject} from "@angular/core";
import{environment} from "../environments/environment";
import {downgradeInjectable} from "@angular/upgrade/static";
import {User} from "../mocks/user.model";
import {distinctUntilChanged, map} from "rxjs/operators";
import * as angular from 'angular';
import {BehaviorSubject} from "rxjs";

export class AuthenticationService{
    private currentUserSubject = new BehaviorSubject<User>(null);
    public currentUser = this.currentUserSubject.asObservable().pipe(distinctUntilChanged());

    private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
    public isAuthenticated = this.isAuthenticatedSubject.asObservable();

    private modalRequestSubject = new BehaviorSubject<boolean>(false);
    public modalRequest = this.modalRequestSubject.asObservable();

    private readonly ADMIN_ROLE_NAME;
    constructor(@Inject(Submitter) private submitter:Submitter, @Inject(CookieMain) private cookie:CookieMain,
                @Inject(NGXLogger) private logger: NGXLogger, @Inject(NgbModal) private modalService: NgbModal,
                @Inject(HttpClient) private http:HttpClient) {
        this.ADMIN_ROLE_NAME = 'ADMIN';
    }

    pullSubmitterData(credentials: any) {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer '+ credentials.access_token
            }
        }
        this.http.get(`${environment.REST_BACKEND_SERVER}/rest/submitters/${credentials.username}`, config).subscribe((res: User) => {
            this.logger.info(res);
            this.currentUserSubject.next({emailAddress: res.emailAddress, access_token: credentials.access_token,
                firstName: res.firstName, lastName: res.lastName, institution: res.institution, roles: res.roles || []});
            this.isAuthenticatedSubject.next(true);
        });
    }

    login(userName, password) {
        this.logger.info(userName);
        this.logger.info(password);
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/auth/login`, JSON.stringify({username: userName, password: password}),
            {headers: {'Content-Type': 'application/json'}}
        ).pipe(map((x: any) => {
            this.cookie.update('AuthorizationToken', x.token);
            this.pullSubmitterData({username: userName, access_token: x.token});
        }));
    };

    /**
     * log us out
     */
    logout() {
        //this.$rootScope.$broadcast('auth:logout', null, null, null, null);
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
        this.cookie.remove('AuthorizationToken');
    };



    isLoggedIn() {
        return this.isAuthenticatedSubject.value;
    };

    /**
     * returns a promise of the currently logged in user
     * @returns {*}
     */
    getCurrentUser() {
        return this.currentUserSubject.value;
    };

    isAdmin() {
        if(typeof this.currentUserSubject.value !== null && typeof this.currentUserSubject.value !== 'undefined') {
            if (this.isAuthenticatedSubject.value && typeof this.currentUserSubject.value.roles !== null && typeof this.currentUserSubject.value.roles !== 'undefined') {
                for (let i = 0; i < this.currentUserSubject.value.roles.length; i++) {
                    console.log(this.currentUserSubject.value);
                    if (this.currentUserSubject.value.roles[i].authority === this.ADMIN_ROLE_NAME)
                        return true;
                }
            }
        }

        return false;
    };

    requestModal() {
        this.modalRequestSubject.next(true);
    }
}

angular.module('moaClientApp')
    .factory('AuthenticationService', downgradeInjectable(AuthenticationService));

