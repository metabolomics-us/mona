/**
 * Created by Gert on 5/28/2014.
 *
 * The My Uploads page. Shows the user's current and past uploads
 */
import {AuthenticationService} from '../../services/authentication.service';
import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {UploadJobResource} from '../../services/upload/upload-job.resource';
import {UploadJobService} from '../../services/upload/upload-job.service';
import {UploadJobModel} from '../../mocks/upload-job.model';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {ChunkedUploadService} from '../../services/upload/chunked-upload.service';
import {faUser, faCloudUploadAlt, faTrash, faExclamationTriangle, faSearch, faPlay, faBan} from '@fortawesome/free-solid-svg-icons';
import {NGXLogger} from 'ngx-logger';
import {Component, OnDestroy, OnInit, TemplateRef} from '@angular/core';
import {Router} from '@angular/router';
import {interval, Subscription} from 'rxjs';
import {ToasterService} from 'angular2-toaster';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'spectra-upload',
    templateUrl: '../../views/spectra/upload/uploadStatus.html'
})
export class SpectraUploadComponent implements OnInit, OnDestroy {
  faUser = faUser;
  faCloudUploadAlt = faCloudUploadAlt;
  faTrash = faTrash;
  faExclamationTriangle = faExclamationTriangle;
  faSearch = faSearch;
  faPlay = faPlay;
  faBan = faBan;

  jobs: UploadJobModel[] = [];
  authSubscription: Subscription;
  jobsChangedSubscription: Subscription;
  refreshSubscription: Subscription;
  pendingDelete: UploadJobModel = null;
  pendingDeleteSpectraCount: number = null;
  pendingCancel: UploadJobModel = null;
  deletionProgress: {[jobId: string]: number} = {};
  // Whether the previous poll tick saw an upload in progress, used for one trailing fetch
  private wasActive = false;

  constructor(public authenticationService: AuthenticationService, public logger: NGXLogger,
              public uploadLibraryService: UploadLibraryService, public uploadJobResource: UploadJobResource,
              public uploadJobService: UploadJobService, public spectraQueryBuilderService: SpectraQueryBuilderService,
              public toaster: ToasterService, public modalService: NgbModal, public router: Router,
              public chunkedUploadService: ChunkedUploadService) {}

  ngOnInit() {
    // Login state is restored asynchronously after a page refresh, so a one time isLoggedIn
    // check here runs too early and the page never fetches. Waiting on the auth observable
    // fetches immediately when already logged in and as soon as a restored session completes
    this.authSubscription = this.authenticationService.isAuthenticated.subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        this.loadJobs();
      }
    });

    // An interactive upload only writes its history row once its batch finishes, which can be
    // after this page has already loaded and gone idle. Refetch the moment the row is written
    this.jobsChangedSubscription = this.uploadJobService.jobsChanged.subscribe(() => {
      if (this.authenticationService.isLoggedIn()) {
        this.loadJobs();
      }
    });

    // Poll only while an upload is still moving. One trailing fetch runs after everything goes
    // quiet so the terminal status and the history row of an interactive upload still land
    this.refreshSubscription = interval(3000).subscribe(() => {
      if (!this.authenticationService.isLoggedIn()) {
        return;
      }
      const active = this.hasActiveJobs() || this.uploadLibraryService.isUploading();
      if (active || this.wasActive) {
        this.loadJobs();
      }
      this.wasActive = active;
    });
  }

  ngOnDestroy() {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    if (this.jobsChangedSubscription) {
      this.jobsChangedSubscription.unsubscribe();
    }
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  loadJobs() {
    const token = this.authenticationService.getCurrentUser().accessToken;
    this.uploadJobResource.listMyJobs(token).subscribe(
      (jobs: UploadJobModel[]) => {
        this.jobs = jobs;
        this.refreshDeletionProgress(token);
      },
      (error) => this.logger.error('failed to load upload jobs: ' + error)
    );
  }

  // Fetches the DeletionJob behind each DELETING row so its progress bar advances
  private refreshDeletionProgress(token: string) {
    this.jobs.filter((job) => job.status === 'DELETING').forEach((job) => {
      this.uploadJobResource.deletionStatus(job.id, token).subscribe(
        (deletion) => {
          this.deletionProgress[job.id] = deletion && deletion.total
            ? Math.floor((((deletion.deleted || 0) + (deletion.skipped || 0)) / deletion.total) * 100)
            : 0;
        },
        (error) => this.logger.error('failed to load deletion status: ' + error)
      );
    });
  }

  hasActiveJobs(): boolean {
    return this.jobs.some((job) => job.status === 'UPLOADING' || job.status === 'SCHEDULED' || job.status === 'RUNNING'
      || job.status === 'DELETING' || job.status === 'CANCELLING');
  }

  // Percentage for the row's progress bar: bytes transferred while uploading, spectra parsed
  // while running, spectra deleted while deleting
  jobProgress(job: UploadJobModel): number {
    if (job.status === 'COMPLETE') {
      return 100;
    }
    if (job.status === 'UPLOADING' || job.status === 'INTERRUPTED') {
      return job.fileSize ? Math.floor(((job.uploadedBytes || 0) / job.fileSize) * 100) : 0;
    }
    if (job.status === 'DELETING') {
      return this.deletionProgress[job.id] || 0;
    }
    return this.uploadJobService.jobProgress(job);
  }

  isRunning(job: UploadJobModel): boolean {
    return job.status === 'UPLOADING' || job.status === 'SCHEDULED' || job.status === 'RUNNING' || job.status === 'CANCELLING';
  }

  // Whether the row can still be cancelled. CANCELLING rows show the button disabled instead
  canCancel(job: UploadJobModel): boolean {
    return job.status === 'UPLOADING' || job.status === 'INTERRUPTED' || job.status === 'SCHEDULED' || job.status === 'RUNNING';
  }

  confirmCancel(job: UploadJobModel, modalTemplate: TemplateRef<any>) {
    this.pendingCancel = job;
    this.modalService.open(modalTemplate).result.then(
      () => this.cancelJob(job),
      () => {}
    );
  }

  // Cancels the rest of an upload while keeping the spectra persisted so far
  cancelJob(job: UploadJobModel) {
    this.chunkedUploadService.cancelUpload(job.id);
    const token = this.authenticationService.getCurrentUser().accessToken;
    this.uploadJobResource.cancelJob(job.id, token).subscribe(
      (updated: UploadJobModel) => {
        const index = this.jobs.findIndex((j) => j.id === job.id);
        if (index !== -1) {
          this.jobs[index] = updated;
        }
        const body = updated.status === 'CANCELLING'
          ? `Cancelling upload of ${job.fileName}. Spectra imported so far are kept.`
          : `Cancelled upload of ${job.fileName}.`;
        this.toaster.pop({type: 'success', title: 'Upload cancelled', body});
      },
      (error) => {
        this.logger.error('failed to cancel upload job: ' + error);
        this.toaster.pop({type: 'error', title: 'Cancel failed', body: 'Could not cancel the upload, the upload likely already completed'});
      }
    );
  }

  // Fetches an up to date spectra count before showing the confirmation, so the warning always
  // reflects exactly what deleteJob is about to delete
  confirmDelete(job: UploadJobModel, modalTemplate: TemplateRef<any>) {
    const token = this.authenticationService.getCurrentUser().accessToken;
    this.uploadJobResource.spectraCount(job.id, token).subscribe(
      (count) => this.openDeleteConfirm(job, count, modalTemplate),
      (error) => {
        this.logger.error('failed to fetch spectra count: ' + error);
        this.openDeleteConfirm(job, null, modalTemplate);
      }
    );
  }

  private openDeleteConfirm(job: UploadJobModel, spectraCount: number, modalTemplate: TemplateRef<any>) {
    this.pendingDelete = job;
    this.pendingDeleteSpectraCount = spectraCount;
    this.modalService.open(modalTemplate).result.then(
      () => this.deleteJob(job),
      () => {}
    );
  }

  // Deletes the upload's spectra. Deletion is async (see UploadSchedulerController), so
  // the job row is kept and updated in place (status DELETING, then DELETED)
  deleteJob(job: UploadJobModel) {
    const token = this.authenticationService.getCurrentUser().accessToken;
    this.uploadJobResource.deleteJob(job.id, token, true).subscribe(
      (updated: UploadJobModel) => {
        const index = this.jobs.findIndex((j) => j.id === job.id);
        if (index !== -1) {
          this.jobs[index] = updated;
        }
        const body = updated.status === 'DELETING'
          ? `Removed upload of ${job.fileName}. Deleting its spectra now.`
          : `Removed upload of ${job.fileName}.`;
        this.toaster.pop({type: 'success', title: 'Upload removed', body});
      },
      (error) => {
        this.logger.error('failed to delete upload job: ' + error);
        this.toaster.pop({type: 'error', title: 'Delete failed', body: 'Could not remove the upload, see server logs'});
      }
    );
  }

  // Resumes an interrupted upload once the user re-selects the file. Driven by the server's job
  // row, so this works even from a different browser or device than the original upload
  onResumeFileSelected(event: Event, job: UploadJobModel) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }
    const file = input.files[0];
    input.value = '';

    const token = this.authenticationService.getCurrentUser().accessToken;
    this.chunkedUploadService.resumeJobFromServer(job, file, token).subscribe(
      (progress) => {
        if (progress.phase === 'error') {
          this.toaster.pop({type: 'error', title: 'Resume failed', body: progress.error});
          return;
        }
        job.status = 'UPLOADING';
        job.uploadedBytes = progress.bytesSent;
        if (progress.phase === 'queued') {
          this.loadJobs();
        }
      },
      (error) => {
        this.logger.error('failed to resume upload job: ' + error);
        this.toaster.pop({type: 'error', title: 'Resume failed', body: 'Could not resume the upload, see server logs'});
      }
    );
  }

  // Single-spectrum interactive uploads store their history row's fileName as this literal
  // "Spectrum <id>" string (set in basic-uploader.component.ts on upload completion)
  private static readonly SPECTRUM_FILENAME_PATTERN = /^Spectrum (\S+)$/;

  // Extracts the server assigned spectrum id from a single-spectrum upload's stored fileName,
  // or null for anything else (chunked file uploads, multi file batches)
  spectrumId(job: UploadJobModel): string | null {
    if (!job.fileName) {
      return null;
    }
    const match = job.fileName.match(SpectraUploadComponent.SPECTRUM_FILENAME_PATTERN);
    return match ? match[1] : null;
  }

  // Queries the spectra of an upload
  queryJob(job: UploadJobModel) {
    if (!job.fileName) {
      return;
    }

    const spectrumId = this.spectrumId(job);
    if (spectrumId) {
      this.router.navigate(['/spectra/display/' + spectrumId]);
      return;
    }

    const query = job.fileName.split(',')
      .map((name) => name.trim())
      .filter((name) => name.length > 0)
      .map((name) => `exists(metaData.name:'origin' and metaData.value:'${name}')`)
      .join(' or ');
    this.router.navigate(['/spectra/browse'], {queryParams: {query}});
  }

  // Browses the current user's spectra
  manageMySpectra() {
    this.spectraQueryBuilderService.prepareQuery();
    this.spectraQueryBuilderService.addUserToQuery(this.authenticationService.getCurrentUser().emailAddress);
    this.spectraQueryBuilderService.executeQuery();
  }
}
