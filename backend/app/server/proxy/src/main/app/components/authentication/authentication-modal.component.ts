import {Component, Inject, Input, OnInit} from "@angular/core";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NGXLogger} from "ngx-logger";
import {downgradeComponent} from "@angular/upgrade/static";
import {AuthenticationService} from "../../services/authentication.service";
import {first} from "rxjs/operators";
import * as angular from 'angular';


@Component({
    selector: 'authentication-modal',
    templateUrl: '../../views/authentication/authenticationModal.html'
})
export class AuthenticationModalComponent implements OnInit {
    private errors;
    private state;
    public credentials;

    constructor(@Inject(AuthenticationService) private authenticationService: AuthenticationService, @Inject(NgbActiveModal) private activeModal: NgbActiveModal,
                @Inject(NGXLogger) private logger: NGXLogger) {}

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
                    this.state = 'success'
                    setTimeout(() => {
                        this.activeModal.close();
                    }, 1000);
                }, (err => {
                    this.logger.info(err);
                    this.errors.push(err);
                })
            );
        }
    };

    cancelDialog() {
        this.activeModal.dismiss('cancel');
    };
}

angular.module('moaClientApp')
    .directive('authenticationModal', downgradeComponent({
        component: AuthenticationModalComponent
    }))
