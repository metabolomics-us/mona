import {HttpClient, HttpEvent, HttpHeaders, HttpRequest} from '@angular/common/http';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {UploadJobModel} from '../../mocks/upload-job.model';

/**
 * REST client for the server side upload pipeline. Mirrors the per call bearer header convention
 * used by spectrum.resource.ts (there is no global interceptor)
 */
@Injectable()
export class UploadJobResource {
  constructor(public http: HttpClient) {}

  private authHeaders(token: string) {
    return {Authorization: 'Bearer ' + token};
  }

  // Creates the job and returns it (status UPLOADING) so the client can start sending chunks
  createJob(meta: any, token: string): Observable<UploadJobModel> {
    return this.http.post<UploadJobModel>(`${environment.REST_BACKEND_SERVER}/rest/uploads`, meta, {
      headers: {'Content-Type': 'application/json', ...this.authHeaders(token)}
    });
  }

  // Sends one chunk at the given byte offset. Observes events so the caller can report byte progress
  uploadChunk(jobId: string, offset: number, chunk: Blob, token: string): Observable<HttpEvent<any>> {
    const form = new FormData();
    form.append('chunk', chunk);
    const request = new HttpRequest('PUT', `${environment.REST_BACKEND_SERVER}/rest/uploads/${jobId}/chunk?offset=${offset}`, form, {
      headers: new HttpHeaders(this.authHeaders(token)),
      reportProgress: true
    });
    return this.http.request(request);
  }

  // Verifies the assembled file and enqueues it for parsing, returns the job (status SCHEDULED)
  completeUpload(jobId: string, token: string): Observable<UploadJobModel> {
    return this.http.post<UploadJobModel>(`${environment.REST_BACKEND_SERVER}/rest/uploads/${jobId}/complete`, {}, {
      headers: this.authHeaders(token)
    });
  }

  // Current job, used both to poll parsing progress and to read the committed offset when resuming
  getJobStatus(jobId: string, token: string): Observable<UploadJobModel> {
    return this.http.get<UploadJobModel>(`${environment.REST_BACKEND_SERVER}/rest/uploads/${jobId}`, {
      headers: this.authHeaders(token)
    });
  }

  // Records a client side interactive upload as a completed history entry (no stored file)
  recordInteractive(summary: any, token: string): Observable<UploadJobModel> {
    return this.http.post<UploadJobModel>(`${environment.REST_BACKEND_SERVER}/rest/uploads/record`, summary, {
      headers: {'Content-Type': 'application/json', ...this.authHeaders(token)}
    });
  }

  // Upload history for the caller, newest first
  listMyJobs(token: string): Observable<UploadJobModel[]> {
    return this.http.get<UploadJobModel[]>(`${environment.REST_BACKEND_SERVER}/rest/uploads`, {
      headers: this.authHeaders(token)
    });
  }

  // Deletes the job and its stored file (if it exists), as well as TODO: the spectra of the job
  deleteJob(jobId: string, token: string, deleteSpectra: boolean = false): Observable<any> {
    return this.http.delete(`${environment.REST_BACKEND_SERVER}/rest/uploads/${jobId}?deleteSpectra=${deleteSpectra}`, {
      headers: this.authHeaders(token)
    });
  }
}
