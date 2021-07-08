/*
 * Component to render our Search Box
 */
import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'searchBox',
    templateUrl: '../../views/navbar/searchBox.html'
})
export class SearchBoxComponent {
    constructor() {}
}

angular.module('moaClientApp')
    .directive('searchBox', downgradeComponent({
        component: SearchBoxComponent
    }));
