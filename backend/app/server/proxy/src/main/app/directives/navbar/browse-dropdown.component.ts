/*
 * Component to render our Browse drop down menu
 */

import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Component, Inject} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'browseDropDown',
    templateUrl: '../../views/navbar/browseDropdown.html'
})
export class BrowseDropDownComponent {

    constructor(@Inject(SpectraQueryBuilderService) private spectraQueryBuilderService: SpectraQueryBuilderService) {}

    // Reset query when user click browse
    resetQuery = () => {
        this.spectraQueryBuilderService.prepareQuery();
    }
}

angular.module('moaClientApp')
    .directive('browseDropDown', downgradeComponent({
        component: BrowseDropDownComponent
    }));
