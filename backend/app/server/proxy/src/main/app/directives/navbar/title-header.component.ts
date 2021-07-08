/*
 * Component to render Header for our navbar
 */

import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'titleHeader',
    templateUrl: '../../views/navbar/titleHeader.html'
})
export class TitleHeaderComponent {
    constructor() {}
}

angular.module('moaClientApp')
    .directive('titleHeader', downgradeComponent({
        component: TitleHeaderComponent
    }));
