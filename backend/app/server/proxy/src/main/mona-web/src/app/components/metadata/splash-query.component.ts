/**
 * Updated by nolanguzman on 10/31/2021
 * Creates or updates a query based on SPLASH
 */

import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {Component, Input} from '@angular/core';
import {faSearch} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'splash-query',
    templateUrl: '../../views/templates/query/splashQuery.html'
})
export class SplashQueryComponent {
    public SpectraQueryBuilderService;
    @Input() public value;
    faSearch = faSearch;

    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService) {}

    /**
     * Create a new query based on the selected SPLASH
     */
    newQuery() {
        this.spectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    }

    /**
     * Add selected SPLASH to the current query
     */
    addToQuery() {
        this.spectraQueryBuilderService.addSplashToQuery(this.value.splash);
        this.spectraQueryBuilderService.executeQuery();
    }
}
