/**
 * Created by wohlgemuth on 6/9/14.
 * a service to handle authentications and provides us with the currently logged in user
 */

import {Submitter} from "./persistence/submitter.resource";
import {CookieMain} from "./cookie/cookie-main.service";
import {NGXLogger} from "ngx-logger";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpClient} from "@angular/common/http";
import {AuthenticationModalController} from "../components/authentication/authentication-modal.component";
import {Inject} from "@angular/core";
import{environment} from "../environments/environment";
import {downgradeInjectable} from "@angular/upgrade/static";
import * as angular from 'angular';

export class AuthenticationService{
    private loggingIn;
    private currentUser;
    private readonly ADMIN_ROLE_NAME;
    constructor(@Inject(Submitter) private submitter:Submitter, @Inject(CookieMain) private cookie:CookieMain,
                @Inject(NGXLogger) private logger: NGXLogger, @Inject(NgbModal) private modalService: NgbModal,
                @Inject(HttpClient) private http:HttpClient) {
        this.loggingIn = false;
        this.currentUser = {};
        this.ADMIN_ROLE_NAME = 'ROLE_ADMIN';
    }

    pullSubmitterData() {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer '+ this.currentUser.access_token
            }
        }
        this.http.get(`${environment.REST_BACKEND_SERVER}/rest/submitters/${this.currentUser.user}`, config).toPromise()
            .then((res) => {
                this.currentUser.emailAddress = res["emailAddress"];
                this.currentUser.firstName = res["firstName"];
                this.currentUser.lastName = res["lastName"];
                this.currentUser.institution = res["institution"];
                //this.$rootScope.$broadcast('auth:user-update', this.currentUser);
            });
    }

    login(userName, password) {
        this.loggingIn = true;

        this.http.post(`${environment.REST_BACKEND_SERVER}/rest/auth/login`, {username: userName, password: password}).toPromise()
            .then(
                (response) => {
                    let token = response["token"];

                    this.currentUser = {username: response["config"]["data"]["username"], access_token: token};
                    this.logger.info("Login success.  Current token: "+ this.currentUser.access_token);

                    this.cookie.update('AuthorizationToken', token);
                    //this.$rootScope.$broadcast('auth:login-success', token, response.status, response.headers, response.config);
                    this.loggingIn = false;

                    this.pullSubmitterData();
                },
                (response) => {
                    this.logger.info(response);
                    //this.$rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
                    this.loggingIn = false;
                }
            );
    };

    /**
     * validate user
     */
    validate() {
        let access_token = undefined;
        this.loggingIn = true;

        if (this.isLoggedIn()) {
            access_token = this.currentUser.access_token;
            this.logger.info("Validation: logged in with token: "+ access_token);
        } else {
            access_token = this.cookie.get('AuthorizationToken');
            this.logger.info("Validation: getting token from cookie: "+ access_token);
        }

        // Only try validating if we found a stored token
        if (angular.isDefined(access_token) && access_token != null && access_token != "") {
            const config = {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer '+ access_token
                }
            };
            this.http.post(`${environment.REST_BACKEND_SERVER}/rest/auth/info`, {token: access_token}, config).toPromise()
                .then(
                     (response) => {
                        this.logger.info(response);
                        this.currentUser = {username: response["username"], access_token: response["config"]["data"]["token"]};
                        this.logger.info("Validation successful");
                        this.pullSubmitterData();
                    },
                     (response) => {
                        this.currentUser = null;
                        this.cookie.remove('AuthorizationToken');
                        //this.$rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
                        this.loggingIn = false;
                    }
                );

            } else {
                this.loggingIn = false;
            }
    };

    /**
     * log us out
     */
    logout() {
        //this.$rootScope.$broadcast('auth:logout', null, null, null, null);
        this.currentUser = null;
        this.cookie.remove('AuthorizationToken');
    };


    isLoggedIn() {
        return angular.isDefined(this.currentUser) &&
            this.currentUser !== null &&
            angular.isDefined(this.currentUser.access_token);
    };

    isLoggingIn() {
        return this.loggingIn;
    };

    /**
     * returns a promise of the currently logged in user
     * @returns {*}
     */
    getCurrentUser() {
        const newPromise = new Promise((resolve, reject) => {
            if(this.isLoggedIn()) {
                resolve(this.currentUser);
                //resolve(this.currentUser);
            } else{
                resolve(null);
            }
        });

        return newPromise;
    };

    /**
     * Handle login
     */
    handleLogin() {
        if (this.isLoggedIn()) {
            this.logout();
        } else {
            this.openAuthenticationDialog();
        }
    };

    /**
     * Opens the authentication modal dialog
     */
    openAuthenticationDialog() {
        this.modalService.open(AuthenticationModalController, {
            size: 'sm',
            backdrop: true
        });
    };

    isAdmin() {
        if (this.isLoggedIn() && typeof this.currentUser.roles !== 'undefined') {
            for (let i = 0; i < this.currentUser.roles.length; i++) {
                if (this.currentUser.roles[i].authority === this.ADMIN_ROLE_NAME)
                    return true;
            }
        }

        return false;
    };
}

angular.module('moaClientApp')
    .factory('AuthenticationService', downgradeInjectable(AuthenticationService));

