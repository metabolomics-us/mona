/*
 * Component to render our Admin drop down menu
 */
import {Component} from "@angular/core";
import {AuthenticationService} from "../../services/authentication.service";

@Component({
    selector: 'admin-drop-down',
    templateUrl: '../../views/navbar/adminDropdown.html'
})
export class AdminDropDownComponent {
    constructor(public authenticationService: AuthenticationService) {}

    isAdmin = () => {
        return this.authenticationService.isAdmin();
    }
}
