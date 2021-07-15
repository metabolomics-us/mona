/**
 * Creates or updates a query based on SPLASH
 */

import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Component, Inject, Input} from "@angular/core";
import * as angular from 'angular';
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'splash-query',
    templateUrl: '../../views/templates/query/splashQuery.html'
})
export class SplashQueryComponent {
    private SpectraQueryBuilderService;
    @Input() private value;
    constructor(@Inject(SpectraQueryBuilderService) private spectraQueryBuilderService) {}

    /**
     * Create a new query based on the selected SPLASH
     */
    newQuery = () => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    };

    /**
     * Add selected SPLASH to the current query
     */
    addToQuery = () => {
        this.SpectraQueryBuilderService.addSplashToQuery(this.value.splash);
        this.SpectraQueryBuilderService.executeQuery();
    };
}

angular.module('moaClientApp')
    .directive('splashQuery', downgradeComponent({
        component: SplashQueryComponent,
        inputs: ['value']
    }));

