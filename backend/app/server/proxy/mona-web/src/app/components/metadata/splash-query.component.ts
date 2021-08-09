/**
 * Creates or updates a query based on SPLASH
 */

import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Component, Input} from "@angular/core";

@Component({
    selector: 'splash-query',
    templateUrl: '../../views/templates/query/splashQuery.html'
})
export class SplashQueryComponent {
    public SpectraQueryBuilderService;
    @Input() public value;
    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService) {}

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
