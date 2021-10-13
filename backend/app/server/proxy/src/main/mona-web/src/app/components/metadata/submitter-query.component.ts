/**
 * Updated by nolanguzman on 10/31/2021
 * Creates or updates a query with the given submitter information
 */

import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {AuthenticationService} from '../../services/authentication.service';
import {Component, Input} from '@angular/core';
import {faSearch} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'submitter-query',
    templateUrl: '../../views/templates/query/submitterQuery.html'
})
export class SubmitterQueryComponent {

    @Input() submitter;
    faSearch = faSearch;
    constructor( public spectraQueryBuilderService: SpectraQueryBuilderService,
                 public authenticationService: AuthenticationService) {}

    /**
     * Create a new query based on the selected submitter
     */
    newQuery() {
        this.spectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    }

    /**
     * Add selected submitter to the current query
     */
    addToQuery() {
        this.spectraQueryBuilderService.addUserToQuery(this.submitter.id);
        this.spectraQueryBuilderService.executeQuery(undefined);
    }

    /**
     * Curate spectra based on selected submitter
     */
    curateSpectra() {
        this.spectraQueryBuilderService.prepareQuery();
        this.spectraQueryBuilderService.addUserToQuery(this.submitter.id);

        const query = this.spectraQueryBuilderService.getRSQLQuery();
        // TODO Add curation functionality
        // Spectrum.curateSpectraByQuery(query, function(data) {});
    }

    isAdmin() {
        return this.authenticationService.isAdmin();
    }
}
