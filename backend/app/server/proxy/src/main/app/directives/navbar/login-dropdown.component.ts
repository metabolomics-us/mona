/*
 * Component to render our Login menu
 */

import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'loginDropDown',
    templateUrl: '../../views/navbar/loginDropdown.html'
})
export class LoginDropDownComponent {
    constructor() {}
}

angular.module('moaClientApp')
    .directive('loginDropDown', downgradeComponent({
        component: LoginDropDownComponent
    }));
