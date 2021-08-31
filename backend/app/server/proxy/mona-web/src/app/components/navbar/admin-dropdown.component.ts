/*
 * Component to render our Admin drop down menu
 */
import {Component} from '@angular/core';
import {AuthenticationService} from '../../services/authentication.service';
import {faCaretDown} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'admin-drop-down',
    templateUrl: '../../views/navbar/adminDropdown.html'
})
export class AdminDropDownComponent {
    faCaretDown = faCaretDown;
    constructor(public authenticationService: AuthenticationService) {}

    isAdmin = () => {
        return this.authenticationService.isAdmin();
    }
}
