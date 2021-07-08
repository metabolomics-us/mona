/*
 * Component to render our Resources drop down menu
 */

import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'resourceDropDown',
    templateUrl: '../../views/navbar/resDropdown.html'
})
export class ResourceDropDownComponent {
    constructor() {}
}

angular.module('moaClientApp')
    .directive('resourceDropDown', downgradeComponent({
        component: ResourceDropDownComponent
    }));

