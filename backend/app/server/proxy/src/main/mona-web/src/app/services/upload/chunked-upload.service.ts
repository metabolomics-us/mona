import {Injectable} from '@angular/core';
import {HttpEventType} from '@angular/common/http';
import {Observable, Subscription} from 'rxjs';
import {NGXLogger} from 'ngx-logger';
import {ToasterService} from 'angular2-toaster';
import {UploadJobResource} from './upload-job.resource';
import {UploadJobModel} from '../../mocks/upload-job.model';

export interface RawUploadProgress {
  jobId: string;
  fileName: string;
  bytesSent: number;
  totalBytes: number;
  phase: 'creating' | 'uploading' | 'finalizing' | 'queued' | 'error';
  error?: string;
  job?: UploadJobModel;
}

/**
 * Uploads a raw spectra file to the server in fixed size chunks.
 * Each chunk is sent at an explicit byte offset. The server job row tracks
 * how many bytes it has committed, so a dropped connection or a closed tab
 * can resume by re-selecting the same file
 */
@Injectable()
export class ChunkedUploadService {
  // 8MB slices, comfortably under the download-scheduler 32MB per request multipart limit
  readonly CHUNK_SIZE = 8 * 1024 * 1024;

  // Holds background upload subscriptions so the transfer keeps running after the uploader
  // component is destroyed by navigating to the status page. Progress after that is read from the
  // server job's uploadedBytes, so a refresh still shows the in flight upload
  private active = new Set<Subscription>();

  // Job ids whose transfer should stop. Checked between chunks by runUpload, so a cancel from the
  // status page stops a loop this browser is driving. Cancels for jobs driven elsewhere are a
  // no-op here; the server rejects their further chunks with 409 instead
  private cancelledJobs = new Set<string>();

  constructor(public uploadJob: UploadJobResource, public logger: NGXLogger, public toaster: ToasterService) {}

  // Flags a job so its chunk loop stops at the next iteration without calling completeUpload
  cancelUpload(jobId: string): void {
    this.cancelledJobs.add(jobId);
  }

  // Starts an upload from the singleton service so the transfer outlives the component. The
  // returned promise resolves as soon as the job row exists on the server (or creation failed),
  // so a caller can navigate to the status page knowing its first fetch will show the job
  startUpload(file: File, meta: any, token: string): Promise<void> {
    return new Promise((resolve) => {
      const sub = this.uploadFile(file, meta, token).subscribe(
        (progress) => {
          if (progress.jobId || progress.phase === 'error') {
            resolve();
          }
        },
        () => {
          this.active.delete(sub);
          resolve();
        },
        () => {
          this.active.delete(sub);
          resolve();
        });
      this.active.add(sub);
    });
  }

  /**
   * Resumes an interrupted upload driven entirely by the server's job row.
   * Validates the re-selected file against the job's recorded name and size
   * before reading the server's committed offset and continuing the transfer
   */
  resumeJobFromServer(job: UploadJobModel, file: File, token: string): Observable<RawUploadProgress> {
    return new Observable<RawUploadProgress>((subscriber) => {
      if (file.name !== job.fileName || file.size !== job.fileSize) {
        this.fail(subscriber, file, job.id, 'Selected file does not match the interrupted upload (name or size differs)');
        return;
      }

      this.uploadJob.getJobStatus(job.id, token).toPromise()
        .then((current) => this.runUpload(file, job.id, current.uploadedBytes || 0, token, subscriber))
        .catch((err) => this.fail(subscriber, file, job.id, err));
    });
  }

  /**
   * Starts a fresh upload: creates the job, streams every chunk, then marks it complete so the
   * server enqueues it for parsing
   */
  uploadFile(file: File, meta: any, token: string): Observable<RawUploadProgress> {
    return new Observable<RawUploadProgress>((subscriber) => {
      const payload = {
        fileName: file.name,
        fileSize: file.size,
        format: meta && meta.format ? meta.format : this.detectFormat(file.name),
        libraryName: meta ? meta.libraryName : null,
        libraryDescription: meta ? meta.libraryDescription : null,
        libraryLink: meta ? meta.libraryLink : null,
        libraryPrefix: meta ? meta.libraryPrefix : null,
        librarySubmitterEmail: meta ? meta.librarySubmitterEmail : null,
        librarySubmitterFirstName: meta ? meta.librarySubmitterFirstName : null,
        librarySubmitterLastName: meta ? meta.librarySubmitterLastName : null,
        librarySubmitterInstitution: meta ? meta.librarySubmitterInstitution : null,
        additionalTags: meta ? meta.additionalTags : null
      };

      subscriber.next({jobId: null, fileName: file.name, bytesSent: 0, totalBytes: file.size, phase: 'creating'});

      this.uploadJob.createJob(payload, token).toPromise()
        .then((job) => this.runUpload(file, job.id, 0, token, subscriber))
        .catch((err) => this.fail(subscriber, file, null, err));
    });
  }

  // Streams chunks from startOffset to EOF, then completes the upload
  private async runUpload(file: File, jobId: string, startOffset: number, token: string, subscriber): Promise<void> {
    try {
      let offset = startOffset;

      while (offset < file.size) {
        if (this.cancelledJobs.has(jobId)) {
          this.cancelledJobs.delete(jobId);
          this.logger.debug('chunked upload cancelled for job ' + jobId);
          subscriber.complete();
          return;
        }

        const end = Math.min(offset + this.CHUNK_SIZE, file.size);
        const blob = file.slice(offset, end);
        const committedBase = offset;

        const newEnd: number = await new Promise<number>((resolve, reject) => {
          this.uploadJob.uploadChunk(jobId, committedBase, blob, token).subscribe((event: any) => {
            if (event.type === HttpEventType.UploadProgress) {
              subscriber.next({jobId, fileName: file.name, bytesSent: committedBase + event.loaded, totalBytes: file.size, phase: 'uploading'});
            } else if (event.type === HttpEventType.Response) {
              const uploaded = event.body && event.body.uploadedBytes ? event.body.uploadedBytes : end;
              resolve(uploaded);
            }
          }, (err) => reject(err));
        });

        offset = Math.max(newEnd, end);
        subscriber.next({jobId, fileName: file.name, bytesSent: offset, totalBytes: file.size, phase: 'uploading'});
      }

      subscriber.next({jobId, fileName: file.name, bytesSent: file.size, totalBytes: file.size, phase: 'finalizing'});

      const job = await this.uploadJob.completeUpload(jobId, token).toPromise();
      subscriber.next({jobId, fileName: file.name, bytesSent: file.size, totalBytes: file.size, phase: 'queued', job});
      subscriber.complete();
    } catch (err) {
      this.fail(subscriber, file, jobId, err);
    }
  }

  private fail(subscriber, file: File, jobId: string, err): void {
    const message = err && err.message ? err.message : String(err);
    this.logger.error('chunked upload failed: ' + message);
    // 507 INSUFFICIENT_STORAGE means the shared uploads volume is full, which is a server side
    // problem the user cannot fix by retrying, so surface it plainly and point them to the issue tracker
    if (err && err.status === 507) {
      this.toaster.pop({
        type: 'error',
        title: 'Upload storage full',
        body: 'Server\'s storage has reached capacity. Please report this issue to the issue tracker'
      });
    }
    subscriber.next({jobId, fileName: file.name, bytesSent: 0, totalBytes: file.size, phase: 'error', error: message});
    subscriber.complete();
  }

  private detectFormat(fileName: string): string {
    const ext = fileName.toLowerCase().split('.').pop();
    if (ext === 'mgf') { return 'mgf'; }
    if (ext === 'txt') { return 'massbank'; }
    return 'msp';
  }
}
