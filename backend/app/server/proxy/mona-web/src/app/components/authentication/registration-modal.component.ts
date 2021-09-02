import {HttpClient} from '@angular/common/http';
import {AuthenticationService} from '../../services/authentication.service';
import {RegistrationService} from '../../services/registration.service';
import {Component, OnInit, ViewChild} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {SubmitterFormComponent} from '../submitter/submitter-form.component';
import {NGXLogger} from 'ngx-logger';
import { first } from 'rxjs/operators';

@Component({
    selector: 'registration-modal',
    templateUrl: '../../views/authentication/registrationModal.html'
})
export class RegistrationModalComponent implements OnInit{
    public errors;
    public state;
    public submitterFormStatus: boolean;
    @ViewChild(SubmitterFormComponent) submitterForm: SubmitterFormComponent;

    constructor(public http: HttpClient, public authenticationService: AuthenticationService,
                public registrationService: RegistrationService, public activeModal: NgbActiveModal,
                public logger: NGXLogger){}

    ngOnInit(): void {
        this.errors = [];
        this.state = 'register';
    }

    cancelDialog(): void {
        this.activeModal.dismiss('cancel');
    }


    /**
     * closes the dialog and finishes and builds the query
     */
    submitRegistration(): void {
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
                            this.errors.push('An unknown error has occurred: ' + JSON.stringify(error));
                        }));

                }, (error => {
                    this.errors.push('An unknown error has occurred: ' + JSON.stringify(error));
                }));

        }, (error => {
                this.errors.push('An unknown error has occurred: ' + JSON.stringify(error));
            }));
    }

    /**
     * Close dialog and open login modal
     */
    logIn(): void {
        this.activeModal.dismiss({$value: 'cancel'});
        this.authenticationService.requestModal();
    }

}
