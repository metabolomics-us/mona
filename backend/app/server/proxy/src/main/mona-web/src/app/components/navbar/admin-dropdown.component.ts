/**
 * Updated by nolanguzman on 10/31/2021
 * Component to render our Admin drop down menu
 */
import {Component} from '@angular/core';
import {AuthenticationService} from '../../services/authentication.service';
import {faCaretDown, faUserShield} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'admin-drop-down',
    templateUrl: '../../views/navbar/adminDropdown.html'
})
export class AdminDropDownComponent {
    faCaretDown = faCaretDown;
    faUserShield = faUserShield;
    constructor(public authenticationService: AuthenticationService) {}

    isAdmin() {
        return this.authenticationService.isAdmin();
    }
}
