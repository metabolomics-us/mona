import {AuthenticationService} from '../../services/authentication.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {TagService} from '../../services/persistence/tag.resource';
import {faEdit, faMinusSquare, faUser} from '@fortawesome/free-solid-svg-icons';
import {NGXLogger} from 'ngx-logger';
import {Subscription} from 'rxjs';
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
  }

  refreshTags() {
    this.tagService.query().subscribe((tags: any) => {
      if (tags.length > 0) {
        this.libraryTags = tags.filter((x) => {
          return x.category === 'library';
        });
      }
    },
      (error) => {
        this.logger.error('Tag pull failed: ' + error);
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

      this.deleteSubscription = this.spectrum.batchDelete({
        query: this.spectraQueryBuilderService.getFilter()
      }, this.auth.getCurrentUser().accessToken)
        .subscribe(() => {
          this.toaster.pop({
            type: 'success',
            title: 'Deletion Successful!',
            body: 'Deletion was successful, libraries will persist until they reload overnight. Please wait a few minutes then validate that the libraries were deleted.'
          });
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
        this.spectrum.batchDeleteByIds(parsed, this.auth.getCurrentUser().accessToken).subscribe(() => {
          this.toaster.pop({
            type: 'success',
            title: 'Deletion Successful!',
            body: 'Deletion was successful, libraries associated with the deleted spectra will persist until they reload overnight. Please wait a few minutes then validate that the spectra were deleted.'
          });
          this.removeIDs = null;
        }, (error) => {
          this.toaster.pop({
            type: 'error',
            title: 'There was a problem deleting libraries.',
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
          title: 'Statistics Being Updated!',
          body: 'Statistics are currently being recalculated. Please allow up to an hour for this operation to complete.'
        });
      }, (error) => {
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
          title: 'Similarity Service Being Updated!',
          body: 'Similarity Service is being repopulated. Please allow up to an hour for this operation to complete.'
        });
      }, (error) => {
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
          title: 'Predefined Queries Re-Generating!',
          body: 'Predefined queries are re-generating. Please allow up to an hour for this operation to complete.'
        });
      }, (error) => {
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
          title: 'Curation Scheduling for All Data Successful',
          body: 'All data is being re-curated, this can be a lengthy process and involve a few days depending on size of current database.'
        });
      }, (error) => {
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
