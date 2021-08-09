import {HttpClient} from "@angular/common/http";
import {NewSubmitter} from "../mocks/newSubmitter.model";
import {environment} from "../../environments/environment";
import {map} from "rxjs/operators";
import {Injectable} from "@angular/core";

@Injectable()
export class RegistrationService {
    public newSubmitter: NewSubmitter;

    constructor(public http: HttpClient) {
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
