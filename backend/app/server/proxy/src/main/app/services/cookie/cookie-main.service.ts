/**
 * Created by wohlgemuth on 10/31/14.
 */

import {CookieService} from "ngx-cookie-service";
import {Inject} from "@angular/core";
import {NGXLogger} from "ngx-logger";
import {downgradeInjectable} from "@angular/upgrade/static";
import * as angular from 'angular';

export class CookieMain{
    constructor(@Inject(CookieService) private cookie: CookieService,
                @Inject(NGXLogger) private logger: NGXLogger) {
    }

     stringToBoolean = (string) => {
        switch (string) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
            case null:
                return false;
            default:
                return Boolean(string);
        }
    }

    /**
     * updates the cookie
     * @param name
     * @param value
     */
    update = (name, value) => {
        this.cookie.set(name, value);
    }

    /**
     * gets the cookie
     * @param cookieName
     */
    get = (cookieName) => {
        return this.cookie.get(cookieName);
    }

    /**
     * remove a cookie
     * @param cookieName
     */
    remove = (cookieName) => {
        return this.cookie.delete(cookieName);
    }

    /**
     * provides us with a boolean cookie value of true or false
     * @param cookieName name your cookie
     * @param defaultValueIfNotFound default value if we don't find the cookie
     */
    getBooleanValue = (cookieName, defaultValueIfNotFound) => {
        let result = (defaultValueIfNotFound === null) ? false : defaultValueIfNotFound;

        if (this.cookie.get(cookieName) !== null) {
            result = this.stringToBoolean(this.cookie.get(cookieName));
        }

        return result;
    }
}

angular.module('moaClientApp')
    .factory('CookieService', downgradeInjectable(CookieMain));

