/*
 * Component to render our Admin drop down menu
 */
import {Component, Inject} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {AuthenticationService} from "../../services/authentication.service";
import * as angular from 'angular';

@Component({
    selector: 'adminDropDown',
    templateUrl: '../../views/navbar/adminDropdown.html'
})
export class AdminDropDownComponent {
    constructor(@Inject([AuthenticationService]) private authenticationService: AuthenticationService) {}

    isAdmin = () => {
        return this.authenticationService.isAdmin();
    }
}

angular.module('moaClientApp')
    .directive('adminDropDown', downgradeComponent({
        component: AdminDropDownComponent
    }));
