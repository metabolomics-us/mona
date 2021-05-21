/**
 * Created by wohlgemuth on 6/9/14.
 * a service to handle authentications and provides us with the currently logged in user
 */

import * as angular from 'angular';

export class AuthenticationService{
    private static $inject = ['Submitter','$rootScope','$log','$http','CookieService', 'REST_BACKEND_SERVER']
    private Submitter;
    private $log;
    private $http;
    private $rootScope;
    private CookieService;
    private REST_BACKEND_SERVER;
    private loggingIn;
    private currentUser;

    constructor(Submitter, $log, $rootScope, $http, CookieService, REST_BACKEND_SERVER) {
        this.Submitter = Submitter;
        this.$log = $log;
        this.$http = $http;
        this.CookieService = CookieService;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$rootScope = $rootScope;

    }

    $onInit = () => {
        this.loggingIn = false;
    }

    pullSubmitterData() {
        this.$http({
            method: 'GET',
            url: this.REST_BACKEND_SERVER + '/rest/submitters/'+ this.currentUser.username,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer '+ this.currentUser.access_token
            }
        }).then(function (response) {
            this.currentUser.emailAddress = response.data.emailAddress;
            this.currentUser.firstName = response.data.firstName;
            this.currentUser.lastName = response.data.lastName;
            this.currentUser.institution = response.data.institution;
            this.$rootScope.$broadcast('auth:user-update', this.currentUser);
        });
    }

    login(userName, password) {
        this.loggingIn = true;

        this.$http({
            method: 'POST',
            url: this.REST_BACKEND_SERVER + '/rest/auth/login',
            headers: {
                'Content-Type': 'application/json'
            },
            data: {username: userName, password: password}
        }).then(
            function (response) {
                let token = response.data.token;

                this.currentUser = {username: response.config.data.username, access_token: token};
                this.$log.info("Login success.  Current token: "+ this.currentUser.access_token);

                this.CookieService.update('AuthorizationToken', token);
                this.$rootScope.$broadcast('auth:login-success', token, response.status, response.headers, response.config);
                this.loggingIn = false;

                this.pullSubmitterData();
            },
            function (response) {
                this.$log.info(response);
                this.$rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
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
            this.$log.info("Validation: logged in with token: "+ access_token);
        } else {
            access_token = this.CookieService.get('AuthorizationToken');
            this.$log.info("Validation: getting token from cookie: "+ access_token);
        }

        // Only try validating if we found a stored token
        if (angular.isDefined(access_token) && access_token != null && access_token != "") {
            this.$http({
                method: 'POST',
                url: this.REST_BACKEND_SERVER + '/rest/auth/info',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer '+ access_token
                },
                data: {
                    token: access_token
                }
            }).then(
                function (response) {
                    this.$log.info(response);
                    this.currentUser = {username: response.data.username, access_token: response.config.data.token};
                    this.$log.info("Validation successful");
                    this.pullSubmitterData();
                },
                function (response) {
                    this.currentUser = null;
                    this.CookieService.remove('AuthorizationToken');
                    this.$rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
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
        this.$rootScope.$broadcast('auth:logout', null, null, null, null);
        this.currentUser = null;
        this.CookieService.remove('AuthorizationToken');
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
        let deferred = this.$http.defer();

        if (this.isLoggedIn()) {
            deferred.resolve(this.currentUser);
        } else {
            deferred.resolve({});
        }

        return deferred.promise;
    };


}

    angular.module('moaClientApp')
        .service('AuthenticationService', AuthenticationService);

