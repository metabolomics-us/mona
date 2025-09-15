import {HttpClient} from '@angular/common/http';
import {NewSubmitter} from '../mocks/newSubmitter.model';
import {environment} from '../../environments/environment';
import {map} from 'rxjs/operators';
import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable()
export class RegistrationService {
    newSubmitter: NewSubmitter;

    modalRequestSubject = new BehaviorSubject<boolean>(false);
    modalRequest = this.modalRequestSubject.asObservable();

    constructor(public http: HttpClient) {
        this.newSubmitter = new NewSubmitter();
    }

    submit() {
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/users`,
            {emailAddress: this.newSubmitter.emailAddress,
             password: this.newSubmitter.password},
            {headers: {
                'Content-Type': 'application/json'
            }
            });

    }

    authorize() {
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/auth/login`,
            {emailAddress: this.newSubmitter.emailAddress,
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
                    Authorization: `Bearer ${token}`
                }
            }).pipe(map((x) => {
                this.newSubmitter = new NewSubmitter();
        }));
    }

    resetSubmitter() {
      this.newSubmitter = new NewSubmitter();
    }

    requestModal() {
      this.modalRequestSubject.next(true);
    }
}
