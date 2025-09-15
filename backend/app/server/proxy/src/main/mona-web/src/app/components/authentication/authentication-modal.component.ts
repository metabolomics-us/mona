/**
 * Updated by nolanguzman on 10/31/2021
 */
import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NGXLogger} from 'ngx-logger';
import {AuthenticationService} from '../../services/authentication.service';
import {RegistrationService} from '../../services/registration.service';
import {first} from 'rxjs/operators';
import {faSpinner, faCheck} from '@fortawesome/free-solid-svg-icons';


@Component({
    selector: 'authentication-modal',
    templateUrl: '../../views/authentication/authenticationModal.html'
})
export class AuthenticationModalComponent implements OnInit {
    errors;
    state;
    credentials;
    faSpinner = faSpinner;
    faCheck = faCheck;

    constructor(public authenticationService: AuthenticationService, public activeModal: NgbActiveModal,
                public logger: NGXLogger, public registrationService: RegistrationService) {}

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
                }, (error => {
                    this.state = 'login';
                    if(typeof error === 'undefined' || error === null) {
                      this.errors.push({status: 400, name: 'Unknown', error: 'Unknown', message: 'Unknown Error, Please Try Again'})
                    } else {
                      this.logger.debug(error);
                      if (error.status === 401) {
                        this.errors.push({status: error.status, name: error.name, error: error.statusText, message: 'Incorrect Email or Password. Try Again.'});
                      } else {
                        this.errors.push({status: error.status, name: error.name, error: error.statusText, message: error.message});
                      }

                    }

                })
            );
        }
    }

    cancelDialog() {
        this.activeModal.dismiss('cancel');
    }

    register() {
      this.activeModal.dismiss({$value: 'cancel'});
      this.registrationService.requestModal();
    }
}
