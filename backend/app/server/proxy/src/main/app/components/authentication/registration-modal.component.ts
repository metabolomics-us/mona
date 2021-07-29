import {HttpClient} from "@angular/common/http";
import {AuthenticationService} from "../../services/authentication.service";
import {RegistrationService} from "../../services/registration.service";
import {AfterViewInit, Component, Inject, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SubmitterFormComponent} from "../../directives/submitter/submitter-form.component";
import {downgradeComponent} from "@angular/upgrade/static";
import {NGXLogger} from "ngx-logger";
import { first } from 'rxjs/operators';
import * as angular from 'angular';
import {Form} from "@angular/forms";

@Component({
    selector: 'registration-modal',
    templateUrl: '../../views/authentication/registrationModal.html'
})
export class RegistrationModalComponent implements OnInit{
    private errors;
    private state;
    public submitterFormStatus: boolean;
    @ViewChild(SubmitterFormComponent) submitterForm: SubmitterFormComponent;

    constructor(@Inject(HttpClient) private http: HttpClient, @Inject(AuthenticationService) private authenticationService: AuthenticationService,
                @Inject(RegistrationService) private registrationService: RegistrationService, @Inject(NgbActiveModal) private activeModal: NgbActiveModal,
                @Inject(NGXLogger) private logger: NGXLogger){}

    ngOnInit() {
        this.errors = [];
        this.state = 'register';
    }

    cancelDialog() {
        this.activeModal.dismiss('cancel');
    };


    /**
     * closes the dialog and finishes and builds the query
     */
    submitRegistration(){
        this.errors = [];
        this.state = 'registering';

        this.registrationService.submit().pipe(first()).subscribe(
            (response) => {
                this.registrationService.authorize().pipe(first()).subscribe((response: any) => {
                    this.registrationService.registerAsSubmitter(response.token).pipe(first()).subscribe(
                        (response) => {
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
    logIn() {
        this.activeModal.dismiss({$value: 'cancel'});
        this.authenticationService.requestModal();
    };

}

angular.module('moaClientApp')
    .directive('registrationModal', downgradeComponent({
        component: RegistrationModalComponent
    }));
