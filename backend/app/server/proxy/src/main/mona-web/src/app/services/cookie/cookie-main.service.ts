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
        this.cookie.set(name, value, undefined, '/');
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
        // Delete at root path (for properly-set cookies with path='/')
        this.cookie.delete(cookieName, '/');

        // Also delete at all parent path segments of the current URL.
        // Cookies set before the path fix were stored at the page's URL path
        // (e.g. /spectra/ or /documentation/) rather than '/'.  Those legacy
        // cookies shadow the root-path cookie on matching routes and survive
        // a root-only delete, causing re-authentication on refresh.
        const segments = window.location.pathname.split('/').filter(s => s.length > 0);
        let path = '';
        for (const segment of segments) {
            path += '/' + segment;
            this.cookie.delete(cookieName, path);
            this.cookie.delete(cookieName, path + '/');
        }
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
