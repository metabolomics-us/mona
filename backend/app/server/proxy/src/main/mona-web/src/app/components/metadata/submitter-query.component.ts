/**
 * Updated by nolanguzman on 10/31/2021
 * Creates or updates a query with the given submitter information
 */

import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {AuthenticationService} from '../../services/authentication.service';
import {AdminService} from '../../services/persistence/admin.resource';
import {ToasterService} from 'angular2-toaster';
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
                 public authenticationService: AuthenticationService, public adminService: AdminService, public toaster: ToasterService) {}

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
        this.spectraQueryBuilderService.addUserToQuery(this.submitter.emailAddress);
        this.spectraQueryBuilderService.executeQuery(undefined);
    }

    /**
     * Curate spectra based on selected submitter
     */
    curateSpectra() {
      if (this.isAdmin()) {
        this.spectraQueryBuilderService.prepareQuery();
        this.spectraQueryBuilderService.addUserToQuery(this.submitter.emailAddress);
        const query = this.spectraQueryBuilderService.getFilter().toString();
        this.adminService.reCurateUserData(this.authenticationService.getCurrentUser().accessToken, query).subscribe(() => {
            this.toaster.pop({
              type: 'success',
              title: 'Curation Scheduling for User Data Successful',
              body: 'User data is being re-curated, this can be a lengthy process depending on the number of spectra the user has submitted.'
            });
          }, (error) => {
            this.toaster.pop({
              type: 'error',
              title: 'There was a problem scheduling the user\'s data for curation.',
              body: `${error.message}`
            });
          });
      }
    }

    isAdmin() {
        return this.authenticationService.isAdmin();
    }
}
