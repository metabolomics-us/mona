import {AuthenticationService} from '../../services/authentication.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {TagService} from '../../services/persistence/tag.resource';
import {faEdit, faMinusSquare, faUser} from '@fortawesome/free-solid-svg-icons';
import {NGXLogger} from 'ngx-logger';
import {interval, Subscription} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {Spectrum} from '../../services/persistence/spectrum.resource';
import {ToasterService} from 'angular2-toaster';
import {AdminService} from '../../services/persistence/admin.resource';

@Component({
  selector: 'removeLibraries',
  templateUrl: '../../views/admin/removeLibraries.html'
})
export class ManageSpectraComponent implements OnInit, OnDestroy {
  faEdit = faEdit;
  faMinusSquare = faMinusSquare;
  faUser = faUser;
  libraryTags;
  formErrors;
  hidePasswords;
  newPassword;
  currentUser;
  librarySubscription: Subscription;
  deleteSubscription: Subscription;
  deletionPollSubscription: Subscription;
  deletionJob: any;
  // Human readable label for what is being deleted (selected library names), shown alongside progress
  deletionLabel: string;
  removeIDs: string;
  constructor(public auth: AuthenticationService, public tagService: TagService,
              public logger: NGXLogger, public spectraQueryBuilderService: SpectraQueryBuilderService,
              public spectrum: Spectrum, public toaster: ToasterService, public adminService: AdminService) {}

  ngOnInit() {
    this.newPassword = {
      emailAddress: '',
      password: '',
      passwordMatch: ''
    };
    this.currentUser = {};
    this.deleteSubscription = null;
    this.deletionPollSubscription = null;
    this.deletionJob = null;
    this.deletionLabel = null;
    this.libraryTags = [];
    this.removeIDs = null;
    this.hidePasswords = true;
    this.librarySubscription = this.tagService.query().subscribe(
      (tags: any) => {
        if (tags.length > 0) {
          this.libraryTags = tags.filter((x) => {
            return x.category === 'library';
          });
        }
      },
      (error) => {
        this.logger.error('Tag pull failed: ' + error);
      }
    );
  }

  ngOnDestroy() {
    this.librarySubscription.unsubscribe();
    if (this.deleteSubscription !== null) {
      this.deleteSubscription.unsubscribe();
    }
    if (this.deletionPollSubscription !== null) {
      this.deletionPollSubscription.unsubscribe();
    }
  }

  // Polls the deletion job until it reaches a terminal state, updating the progress bar each tick
  startDeletionPolling(jobId: string) {
    if (this.deletionPollSubscription !== null) {
      this.deletionPollSubscription.unsubscribe();
    }

    this.deletionPollSubscription = interval(2000).pipe(
      switchMap(() => this.spectrum.deletionStatus(jobId))
    ).subscribe((job: any) => {
      this.deletionJob = job;

      if (job.status === 'COMPLETE' || job.status === 'FAILED') {
        this.deletionPollSubscription.unsubscribe();

        if (job.status === 'COMPLETE') {
          this.toaster.pop({
            type: 'success',
            title: 'Deletion Complete!',
            body: `Deleted ${job.deleted}${job.skipped > 0 ? ', skipped ' + job.skipped : ''} spectra${this.deletionLabel ? ' from ' + this.deletionLabel : ''}. The library list will refresh shortly.`
          });
          this.refreshTags();
        } else {
          this.toaster.pop({
            type: 'error',
            title: 'Deletion Failed',
            body: job.errorMessage || 'See server logs for details.'
          });
        }
      }
    }, (error) => {
      this.logger.error('Deletion status poll failed: ' + error);
    });
  }

  // Percentage complete (deleted + skipped) of the current deletion job, for the progress bar
  deletionProgress(): number {
    if (!this.deletionJob || !this.deletionJob.total) {
      return 0;
    }
    return Math.floor(((this.deletionJob.deleted + this.deletionJob.skipped) / this.deletionJob.total) * 100);
  }

  isDeletionRunning(): boolean {
    return this.deletionJob && (this.deletionJob.status === 'SCHEDULED' || this.deletionJob.status === 'RUNNING');
  }

  // Recomputes tag statistics from live data so deleted libraries drop off immediately, then
  // updates the displayed library list
  refreshTags() {
    this.adminService.refreshLibraries(this.auth.getCurrentUser().accessToken).subscribe((tags: any) => {
      this.libraryTags = (tags || []).filter((x) => {
        return x.category === 'library';
      });
      this.toaster.pop({
        type: 'success',
        title: 'Libraries Refreshed!',
        body: 'The library list has been refreshed.'
      });
    },
      (error) => {
        this.logger.error('Library refresh failed: ' + error);
        this.toaster.pop({
          type: 'error',
          title: 'There was a problem refreshing the libraries.',
          body: `${error.message}`
        });
      });
  }

  submitDeletionQuery() {
    if (this.auth.isAdmin()) {
      this.spectraQueryBuilderService.prepareQuery();

      // Handle library tags
      const libraryTags = this.libraryTags.reduce((result, element) => {
        if (element.selected) {
          result.push(element.text);
        }
        return result;
      }, []);

      if (libraryTags.length > 0) {
        this.spectraQueryBuilderService.addTagToQuery(libraryTags, undefined);
      }

      this.deletionLabel = libraryTags.join(', ');

      this.deleteSubscription = this.spectrum.batchDelete({
        query: this.spectraQueryBuilderService.getFilter()
      }, this.auth.getCurrentUser().accessToken)
        .subscribe((job: any) => {
          this.deletionJob = job;
          this.toaster.pop({
            type: 'success',
            title: 'Deletion Started',
            body: 'The deletion runs in the background and continues even if you leave. Live progress is shown on this page.'
          });
          this.startDeletionPolling(job.id);
        }, (error) => {
          this.toaster.pop({
            type: 'error',
            title: 'There was a problem deleting libraries.',
            body: `${error.message}`
          });
        });
    }
  }

  deleteByIds() {
    if (this.auth.isAdmin()) {
      if (this.removeIDs !== null) {
        const parsed = this.removeIDs.replace(/\s+/g, '').split(',');
        this.deletionLabel = null;
        this.spectrum.batchDeleteByIds(parsed, this.auth.getCurrentUser().accessToken).subscribe((job: any) => {
          this.deletionJob = job;
          this.toaster.pop({
            type: 'success',
            title: 'Deletion Started',
            body: 'The deletion runs in the background and continues even if you leave. Live progress is shown on this page.'
          });
          this.startDeletionPolling(job.id);
          this.removeIDs = null;
        }, (error) => {
          this.toaster.pop({
            type: 'error',
            title: 'There was a problem deleting spectra.',
            body: `${error.message}`
          });
          this.removeIDs = null;
        });
      }
    }
  }

  updateStatistics() {
    if (this.auth.isAdmin()) {
      this.adminService.updateStatistics(this.auth.getCurrentUser().accessToken).subscribe((res) => {
        this.toaster.pop({
          type: 'success',
          title: 'Statistics Update Scheduled!',
          body: 'Statistics will be recalculated. Please allow up to an hour for this operation to complete.'
        });
      }, (error) => {
        if (error.status === 409) {
          this.toaster.pop({
            type: 'info',
            title: 'Update already in progress',
            body: 'A statistics update is already running. Please wait for it to finish.'
          });
          return;
        }
        this.toaster.pop({
          type: 'error',
          title: 'There was a problem requesting statistic update.',
          body: `${error.message}`
        });
      });
    }
  }

  refreshSimilarity() {
    if (this.auth.isAdmin()) {
      this.adminService.refreshSimilarity(this.auth.getCurrentUser().accessToken).subscribe((res) => {
        this.toaster.pop({
          type: 'success',
          title: 'Similarity Index Rebuild Queued!',
          body: 'Similarity Service is being repopulated. Please allow up to an hour for this operation to complete.'
        });
      }, (error) => {
        if (error.status === 409) {
          this.toaster.pop({
            type: 'info',
            title: 'Update already in progress',
            body: 'A similarity refresh is already running. Please wait for it to finish.'
          });
          return;
        }
        this.toaster.pop({
          type: 'error',
          title: 'There was a problem requesting similarity refresh.',
          body: `${error.message}`
        });
      });
    }
  }

  updatePredefinedQueries() {
    if (this.auth.isAdmin()) {
      this.adminService.updatePredefinedDownloads(this.auth.getCurrentUser().accessToken).subscribe(() => {
        this.toaster.pop({
          type: 'success',
          title: 'Re-Generating Downloads!',
          body: 'Predefined queries are now re-generating. Please allow up to an hour for this operation to complete.'
        });
      }, (error) => {
        if (error.status === 409) {
          this.toaster.pop({
            type: 'info',
            title: 'Update already in progress',
            body: 'A predefined query regeneration is already running. Please wait for it to finish.'
          });
          return;
        }
        this.toaster.pop({
          type: 'error',
          title: 'There was a problem requesting an update to predefined queries.',
          body: `${error.message}`
        });
      });
    }
  }

  updateStaticQueries() {
    if (this.auth.isAdmin()) {
      this.adminService.updateStaticDownloads(this.auth.getCurrentUser().accessToken).subscribe(() => {
        this.toaster.pop({
          type: 'success',
          title: 'Static Queries Re-Generating!',
          body: 'Static queries are re-generating. Please allow up to an hour for this operation to complete.'
        });
      }, (error) => {
        this.toaster.pop({
          type: 'error',
          title: 'There was a problem requesting an update to static queries.',
          body: `${error.message}`
        });
      });
    }
  }

  reCurateAllData() {
    if (this.auth.isAdmin()) {
      this.adminService.reCurateAllData(this.auth.getCurrentUser().accessToken).subscribe(() => {
        this.toaster.pop({
          type: 'success',
          title: 'Curation Scheduling for All Spectra Successful',
          body: 'All data is being re-curated, this can take up to a few days.'
        });
      }, (error) => {
        if (error.status === 409) {
          this.toaster.pop({
            type: 'info',
            title: 'Update already in progress',
            body: 'A re-curation is already running. Please wait for it to finish.'
          });
          return;
        }
        this.toaster.pop({
          type: 'error',
          title: 'There was a problem scheduling data for curation.',
          body: `${error.message}`
        });
      });
    }
  }

  validateUser() {
    if (this.auth.isAdmin()) {
      this.adminService.fetchUser(this.auth.getCurrentUser().accessToken, this.newPassword.emailAddress).subscribe((x) => {
        this.hidePasswords = false;
        this.currentUser = x;
        this.toaster.pop({
          type: 'success',
          title: 'Validated Email Address',
          body: 'This email address was found in the database'
        });
      }, (error) => {
        this.hidePasswords = true;
        this.toaster.pop({
          type: 'error',
          title: 'Email Address Not Found',
          body: 'This user does not seem to exist'
        });
      });
    }
  }

  submitPasswordChange() {
    if (this.auth.isAdmin()) {
      this.currentUser.password = this.newPassword.password;
      this.adminService.submitPasswordChange(this.auth.getCurrentUser().accessToken, this.currentUser).subscribe(() => {
        this.toaster.pop({
          type: 'success',
          title: 'Password Successfully Reset',
          body: 'Please try logging in with the new password!'
        });
      }, (error) => {
        this.toaster.pop({
          type: 'error',
          title: 'Unable to reset Password',
          body: 'Please try submitting again or check server logs to see why rejected'
        });
      });
    }
  }

  hasSelectedLibraries() {
    return this.libraryTags?.some(tag => tag.selected) ?? false;
  }
}
