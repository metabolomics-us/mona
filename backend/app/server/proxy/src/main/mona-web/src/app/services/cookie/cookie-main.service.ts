/**
 * Created by wohlgemuth on 10/31/14.
 */

import {CookieService} from 'ngx-cookie-service';
import {NGXLogger} from 'ngx-logger';
import {Injectable} from '@angular/core';

@Injectable()
export class CookieMain{
    constructor(public cookie: CookieService, public logger: NGXLogger) {}

     stringToBoolean = (value) => {
        switch (value) {
            case 'true':
            case 'yes':
            case '1':
                return true;
            case 'false':
            case 'no':
            case '0':
            case null:
                return false;
            default:
                return Boolean(value);
        }
    }

    /**
     * updates the cookie
     * @param name string
     * @param value string
     */
    update(name, value) {
        this.cookie.set(name, value);
    }

    /**
     * gets the cookie
     * @param cookieName string
     */
    get(cookieName) {
        return this.cookie.get(cookieName);
    }

    /**
     * remove a cookie
     * @param cookieName string
     */
    remove(cookieName) {
        return this.cookie.delete(cookieName);
    }

    /**
     * provides us with a boolean cookie value of true or false
     * @param cookieName name your cookie
     * @param defaultValueIfNotFound default value if we don't find the cookie
     */
    getBooleanValue(cookieName, defaultValueIfNotFound) {
        let result = (defaultValueIfNotFound === null) ? false : defaultValueIfNotFound;

        if (this.cookie.get(cookieName) !== null) {
            result = this.stringToBoolean(this.cookie.get(cookieName));
        }

        return result;
    }
}
