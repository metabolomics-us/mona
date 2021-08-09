/**
 * Created by wohlgemuth on 7/11/14.
 */

// TODO: waiting for implementation of return user data for admin from authentication Service
import {Component, OnInit} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NGXLogger} from "ngx-logger";
import {AuthenticationService} from "../../services/authentication.service";
import {AuthenticationModalComponent} from "./authentication-modal.component";
import {RegistrationModalComponent} from "./registration-modal.component";


@Component({
    selector: 'authentication',
    templateUrl: '../../views/navbar/loginDropdown.html'
})
export class AuthenticationComponent implements OnInit{
    public ADMIN_ROLE_NAME;
    public currentUser;
    public welcomeMessage;

    constructor(public modalService: NgbModal, public authenticationService: AuthenticationService,
                public logger: NGXLogger) {}

    ngOnInit() {
        this.authenticationService.currentUser.subscribe((x) => {
            this.currentUser = x;
        });

        this.ADMIN_ROLE_NAME = 'ROLE_ADMIN';
        this.welcomeMessage = 'Login/Register';

        /**
         * Create a welcome message on login
         */
        this.authenticationService.isAuthenticated.subscribe((authorized) => {
            if(authorized) {
                this.welcomeMessage = `Welcome, ${this.authenticationService.getCurrentUser()}!`
            } else if (!authorized && this.currentUser === null) {
                /**
                 * Remove the welcome message on logout
                 */
                this.welcomeMessage = 'Login/Register';
            }
        });


        /**
         * Listen for external calls to bring up the authentication modal
         */
        this.authenticationService.modalRequest.subscribe((request) => {
            if(request) {
                this.handleLogin();
            }
        });
    }

    isLoggedIn() {
        return this.authenticationService.isLoggedIn();
    }

    isAdmin() {
        return this.authenticationService.isAdmin();
    };

    /**
     * Handle login
     */

    /**
     * Handle login
     */
    handleLogin() {
        if (this.authenticationService.isLoggedIn()) {
            this.authenticationService.logout();
        } else {
            this.openAuthenticationDialog();
        }
    };

    /**
     * Opens the authentication modal dialog
     */
    openAuthenticationDialog() {
        let modalRef;
        modalRef = this.modalService.open(AuthenticationModalComponent, {
            size: 'sm',
            backdrop: true
        });
    };

    /**
     * Opens the registration modal dialog
     */
    handleRegistration() {
        let modalRef;
        if (!this.isLoggedIn()) {
            modalRef = this.modalService.open(RegistrationModalComponent,{
                size: 'md',
                backdrop: 'static'
            });
        }
    };
}