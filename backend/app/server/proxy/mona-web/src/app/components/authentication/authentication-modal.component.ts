/**
 * Updated by nolanguzman on 10/31/2021
 */
import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NGXLogger} from 'ngx-logger';
import {AuthenticationService} from '../../services/authentication.service';
import {first} from 'rxjs/operators';
import {faSpinner, faCheck} from '@fortawesome/free-solid-svg-icons';


@Component({
    selector: 'authentication-modal',
    templateUrl: '../../views/authentication/authenticationModal.html'
})
export class AuthenticationModalComponent implements OnInit {
    public errors;
    public state;
    public credentials;
    faSpinner = faSpinner;
    faCheck = faCheck;

    constructor(public authenticationService: AuthenticationService, public activeModal: NgbActiveModal,
                public logger: NGXLogger) {}

    ngOnInit() {
        this.errors = [];
        this.state = 'login';
        this.credentials = {
            email: '',
            password: ''
        };
    }


    submitLogin() {
        this.errors = [];

        if (this.credentials.email === '') {
            this.errors.push('Please enter your email address');
        }

        if (this.credentials.password === '') {
            this.errors.push('Please enter your password');
        }

        if (this.errors.length === 0) {
            this.state = 'logging in';
            this.authenticationService.login(this.credentials.email, this.credentials.password).pipe(first()).subscribe(
                (user) => {
                    this.state = 'success';
                    setTimeout(() => {
                        this.activeModal.close();
                    }, 1000);
                }, (err => {
                    this.state = 'login';
                    this.logger.info(err);
                    this.errors.push(err);
                })
            );
        }
    }

    cancelDialog() {
        this.activeModal.dismiss('cancel');
    }
}
