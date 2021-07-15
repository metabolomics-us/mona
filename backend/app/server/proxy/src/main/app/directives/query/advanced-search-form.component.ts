import * as angular from 'angular';

import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'advanced-search-form',
    templateUrl: '../../views/spectra/query/advancedSearchForm.html'
})
export class AdvancedSearchFormComponent {
    constructor() {}
}

angular.module('moaClientApp')
    .directive('advancedSearchForm', downgradeComponent({
        component: AdvancedSearchFormComponent
    }));
