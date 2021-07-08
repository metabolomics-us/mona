/*
 * Component to render our Admin drop down menu
 */
import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'adminDropDown',
    templateUrl: '../../views/navbar/adminDropdown.html'
})
export class AdminDropDownComponent {
    constructor() {}
}

angular.module('moaClientApp')
    .directive('adminDropDown', downgradeComponent({
        component: AdminDropDownComponent
    }));
