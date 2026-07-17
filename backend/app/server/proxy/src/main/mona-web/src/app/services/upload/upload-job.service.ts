import {Injectable} from '@angular/core';
import {interval, Observable, Subject} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {NGXLogger} from 'ngx-logger';
import {UploadJobResource} from './upload-job.resource';
import {UploadJobModel} from '../../mocks/upload-job.model';

/**
 * Polls an upload job's parsing progress until it reaches a terminal state, modeled on the
 * deletion job polling in manage-spectra.component.ts. Emits the job on every tick so a component
 * can drive a progress bar and stop polling on COMPLETE or FAILED
 */
@Injectable()
export class UploadJobService {
  // Fired when an upload writes a job row after the My Uploads page has already loaded, which
  // happens for interactive uploads whose history record is only created once the batch finishes.
  // The page refetches on this instead of relying on a poll tick that may never come
  jobsChangedSubject = new Subject<void>();
  jobsChanged = this.jobsChangedSubject.asObservable();

  constructor(public uploadJob: UploadJobResource, public logger: NGXLogger) {}

  notifyJobsChanged() {
    this.jobsChangedSubject.next();
  }

  // Emits the job every 2 seconds. Callers should unsubscribe when status is COMPLETE or FAILED
  pollJob(jobId: string, token: string): Observable<UploadJobModel> {
    return interval(2000).pipe(
      switchMap(() => this.uploadJob.getJobStatus(jobId, token))
    );
  }

  // Percentage of spectra parsed (parsed + failed over total), for the parsing progress bar
  jobProgress(job: UploadJobModel): number {
    if (!job || !job.total) {
      return 0;
    }
    return Math.floor((((job.parsed || 0) + (job.failed || 0)) / job.total) * 100);
  }
}
