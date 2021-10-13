/**
 * Created by wohlgemuth on 7/11/14.
 * Updated by nolanguzman on 10/31/2021
 */

// TODO: waiting for implementation of return user data for admin from authentication Service
import {Component, OnInit} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {NGXLogger} from 'ngx-logger';
import {AuthenticationService} from '../../services/authentication.service';
import {AuthenticationModalComponent} from './authentication-modal.component';
import {RegistrationModalComponent} from './registration-modal.component';
import {faUser, faCaretDown, faSignOutAlt, faUsers} from '@fortawesome/free-solid-svg-icons';


@Component({
    selector: 'authentication',
    templateUrl: '../../views/navbar/loginDropdown.html'
})
export class AuthenticationComponent implements OnInit{
    ADMIN_ROLE_NAME;
    currentUser;
    welcomeMessage;

    faUser = faUser;
    faCaretDown = faCaretDown;
    faSignOutAlt = faSignOutAlt;
    faUsers = faUsers;

    constructor(public modalService: NgbModal, public authenticationService: AuthenticationService,
                public logger: NGXLogger) {}

    ngOnInit() {
        this.authenticationService.validate();
        this.authenticationService.currentUser.subscribe((x) => {
            this.currentUser = x;
        });

        this.ADMIN_ROLE_NAME = 'ROLE_ADMIN';
        this.welcomeMessage = 'Login/Register';

        /**
         * Create a welcome message on login
         */
        this.authenticationService.isAuthenticated.subscribe((authorized) => {
            if (authorized) {
                this.welcomeMessage = `Welcome, ${this.authenticationService.getCurrentUser()}!`;
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
            if (request) {
                this.handleLogin();
            }
        });
    }

    isLoggedIn(): boolean {
        return this.authenticationService.isLoggedIn();
    }

    isAdmin(): boolean {
        return this.authenticationService.isAdmin();
    }

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
    }

    /**
     * Opens the authentication modal dialog
     */
    openAuthenticationDialog() {
        this.modalService.open(AuthenticationModalComponent, {
            size: 'sm',
            backdrop: true
        });
    }

    /**
     * Opens the registration modal dialog
     */
    handleRegistration() {
        if (!this.isLoggedIn()) {
            this.modalService.open(RegistrationModalComponent, {
              size: 'md',
              backdrop: 'static'
            });
        }
    }
}
