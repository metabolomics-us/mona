/**
 * Created by wohlgemuth on 6/9/14.
 * a service to handle authentications and provides us with the currently logged in user
 */

import {Submitter} from './persistence/submitter.resource';
import {CookieMain} from './cookie/cookie-main.service';
import {NGXLogger} from 'ngx-logger';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {User} from '../mocks/user.model';
import {distinctUntilChanged, first, map} from 'rxjs/operators';
import {BehaviorSubject, Observable} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable()
export class AuthenticationService{
    currentUserSubject = new BehaviorSubject<User>(null);
    currentUser = this.currentUserSubject.asObservable().pipe(distinctUntilChanged());

    isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
    isAuthenticated = this.isAuthenticatedSubject.asObservable();

    modalRequestSubject = new BehaviorSubject<boolean>(false);
    modalRequest = this.modalRequestSubject.asObservable();

    private readonly ADMIN_ROLE_NAME;
    constructor(public submitter: Submitter, public cookie: CookieMain,
                public logger: NGXLogger, public modalService: NgbModal,
                public http: HttpClient) {
        this.ADMIN_ROLE_NAME = 'ADMIN';
    }

    pullSubmitterData(credentials: any) {
        if (credentials.username === 'admin') {
          this.currentUserSubject.next({emailAddress: credentials.username, id: credentials.id, accessToken: credentials.accessToken,
            firstName: credentials.username, lastName: '', institution: '', roles: [{authority: 'ADMIN'}]});
          this.isAuthenticatedSubject.next(true);
        } else {
          const config = {
            headers: {
              'Content-Type': 'application/json',
              Authorization: 'Bearer ' + credentials.accessToken
            }
          };
          this.http.get(`${environment.REST_BACKEND_SERVER}/rest/submitters/${credentials.username}`, config).subscribe((res: User) => {
            this.currentUserSubject.next({
              emailAddress: res.emailAddress, id: res.id, accessToken: credentials.accessToken,
              firstName: res.firstName, lastName: res.lastName, institution: res.institution, roles: res.roles || []
            });
            this.isAuthenticatedSubject.next(true);
          }, error => {
            this.logger.debug(error);
          });
        }
    }

    login(userName, password): Observable<any> {
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/auth/login`,
          JSON.stringify({username: userName, password}),
            {headers: {'Content-Type': 'application/json'}}
        ).pipe(map((x: any) => {
            this.cookie.update('AuthorizationToken', x.token);
            this.pullSubmitterData({username: userName, accessToken: x.token});
        }));
    }

    validate() {
      let accessToken;

      if (this.isLoggedIn()) {
        accessToken = this.getCurrentUser().accessToken;
        this.logger.debug('Validation: logged in with token: ' + accessToken);
      } else {
        accessToken = this.cookie.get('AuthorizationToken');
        this.logger.debug('Validation: getting token from cookie: ' + accessToken);
      }

      // Only try validating if we found a stored token
      if (typeof accessToken !== 'undefined' && accessToken !== null && accessToken !== '') {
        const config = {
          headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + accessToken
          }
        };
        this.http.post(`${environment.REST_BACKEND_SERVER}/rest/auth/info`, {token: accessToken}, config).pipe(first()).subscribe(
          (res: any) => {
            this.logger.debug(res);
            const credentials = {
              username: res.username, accessToken
            };
            this.pullSubmitterData(credentials);
          }, (() => {
            this.currentUserSubject.next(null);
            this.cookie.remove('AuthorizationToken');
            this.isAuthenticatedSubject.next(false);
          })
        );
      }
    }

    /**
     * log us out
     */
    logout() {
        this.currentUserSubject.next(null);
        this.isAuthenticatedSubject.next(false);
        this.cookie.remove('AuthorizationToken');
    }



    isLoggedIn(): boolean {
        return this.isAuthenticatedSubject.value;
    }

    /**
     * returns a object of the currently logged in user
     * @returns object
     */
    getCurrentUser() {
        return this.currentUserSubject.value;
    }

    isAdmin(): boolean {
        if (typeof this.currentUserSubject.value !== null && typeof this.currentUserSubject.value !== 'undefined') {
            if (this.isAuthenticatedSubject.value && (typeof this.currentUserSubject.value.roles !== null)
              && (typeof this.currentUserSubject.value.roles !== 'undefined')) {
                for (let i = 0; i < this.currentUserSubject.value.roles.length; i++) {
                    if (this.currentUserSubject.value.roles[i].authority === this.ADMIN_ROLE_NAME) {
                      return true;
                    }
                }
            }
        }
        return false;
    }

    requestModal() {
        this.modalRequestSubject.next(true);
    }
}
