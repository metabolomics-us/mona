/**
 * Updated by nolanguzman on 10/31/2021
 * Creates or updates a query with the given submitter information
 */

import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {AuthenticationService} from '../../services/authentication.service';
import {AdminService} from '../../services/persistence/admin.resource';
import {Spectrum} from '../../services/persistence/spectrum.resource';
import {ToasterService} from 'angular2-toaster';
import {Component, Input} from '@angular/core';
import {first} from 'rxjs/operators';
import {faSearch} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'submitter-query',
    templateUrl: '../../views/templates/query/submitterQuery.html'
})
export class SubmitterQueryComponent {

    @Input() submitter;
    faSearch = faSearch;

    // Confirmation state for deleting all of a submitter's spectra
    showSpectraDeletionConfirmation = false;
    pendingSpectraCount;

    constructor( public spectraQueryBuilderService: SpectraQueryBuilderService,
                 public authenticationService: AuthenticationService, public adminService: AdminService,
                 public spectrum: Spectrum, public toaster: ToasterService) {}

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

    // Builds the RSQL query matching every spectrum submitted by this submitter
    buildSubmitterQuery() {
        this.spectraQueryBuilderService.prepareQuery();
        this.spectraQueryBuilderService.addUserToQuery(this.submitter.emailAddress);
        return this.spectraQueryBuilderService.getFilter();
    }

    // Prompts confirmation to delete all of the submitter's spectra, counting them first
    askToDeleteSpectra() {
        if (!this.isAdmin()) { return; }

        this.pendingSpectraCount = null;
        this.showSpectraDeletionConfirmation = true;

        this.spectrum.searchSpectraCount({query: this.buildSubmitterQuery()}).pipe(first()).subscribe((res: any) => {
            this.pendingSpectraCount = res.count;
        }, (error) => {
            this.toaster.pop({
                type: 'error',
                title: 'Could not count spectra',
                body: 'Unable to determine how many spectra this user has submitted.'
            });
        });
    }

    // Cancels the pending spectra deletion confirmation
    cancelDeleteSpectra() {
        this.showSpectraDeletionConfirmation = false;
        this.pendingSpectraCount = null;
    }

    /**
     * Enqueues a background job deleting every spectrum submitted by this user. The backend rejects
     * an empty query so this can never match everything
     */
    confirmDeleteSpectra() {
        if (!this.isAdmin()) { return; }

        const query = this.buildSubmitterQuery();
        const submitterEmail = this.submitter.emailAddress;

        this.spectrum.batchDelete({query}, this.authenticationService.getCurrentUser().accessToken)
            .pipe(first()).subscribe(() => {
                this.toaster.pop({
                    type: 'success',
                    title: 'Deletion Started',
                    body: `Deleting all spectra submitted by ${submitterEmail}. This runs in the background and can take a while depending on how many spectra there are.`
                });
            }, (error) => {
                this.toaster.pop({
                    type: 'error',
                    title: 'There was a problem deleting the user\'s spectra.',
                    body: `${error.message}`
                });
            });

        this.cancelDeleteSpectra();
    }

    isAdmin() {
        return this.authenticationService.isAdmin();
    }
}
