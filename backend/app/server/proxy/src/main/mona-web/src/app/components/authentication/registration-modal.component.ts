/**
 * Updated by nolanguzman on 10/31/2021
 */
import {HttpClient} from '@angular/common/http';
import {AuthenticationService} from '../../services/authentication.service';
import {RegistrationService} from '../../services/registration.service';
import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NGXLogger} from 'ngx-logger';
import { first } from 'rxjs/operators';
import {faSpinner, faCheck, faExclamationTriangle} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'registration-modal',
    templateUrl: '../../views/authentication/registrationModal.html'
})
export class RegistrationModalComponent implements OnInit{
    errors;
    state;
    submitterFormStatus: boolean;
    faSpinner = faSpinner;
    faCheck = faCheck;
    faExclamationTriangle = faExclamationTriangle;

    constructor(public http: HttpClient, public authenticationService: AuthenticationService,
                public registrationService: RegistrationService, public activeModal: NgbActiveModal,
                public logger: NGXLogger){}

    ngOnInit() {
        this.errors = [];
        this.state = 'register';
    }

    cancelDialog() {
        this.activeModal.dismiss('cancel');
    }


    /**
     * closes the dialog and finishes and builds the query
     */
    submitRegistration() {
        this.errors = [];
        this.state = 'registering';

        this.registrationService.submit().pipe(first()).subscribe(
            () => {
                this.registrationService.authorize().pipe(first()).subscribe((res: any) => {
                    this.registrationService.registerAsSubmitter(res.token).pipe(first()).subscribe(
                        () => {
                            this.state = 'success';
                        },
                        (error => {
                            this.errors.push({status: error.status, name: error.name, error: error.error.error,
                                              exception: error.error.exception, message: error.error.message});
                            this.state = 'fail';
                        }));

                }, (error => {
                    this.errors.push({status: error.status, name: error.name, error: error.error.error,
                      exception: error.error.exception, message: error.error.message});
                    this.state = 'fail';
                }));

        }, (error => {
                if (error.status === 409) {
                  this.errors.push({status: error.status, name: error.name, error: 'REST API Error',
                    exception: 'Problem when calling Registration REST API',
                    message: 'This problem typically arises when the account being registered already exists. Please contact the admin if you need your password reset.'
                  });
                  this.state = 'fail';
                }
                else if (error.error !== null) {
                  this.errors.push({status: error.status, name: error.name, error: error.error.error,
                    exception: error.error.exception, message: error.error.message});
                  this.state = 'fail';
                } else{
                  this.errors.push({status: error.status, name: error.name, error: 'REST Error',
                    exception: 'REST Error', message: 'Unknown Problem with REST Call'});
                  this.state = 'fail';
                }
            }));
    }

    /**
     * Close dialog and open login modal
     */
    logIn() {
        this.activeModal.dismiss({$value: 'cancel'});
        this.authenticationService.requestModal();
    }

    resetState() {
      this.state = 'register';
      this.errors = [];
    }

}
