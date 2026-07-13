import {of, throwError} from 'rxjs';
import {SpectraUploadComponent} from './spectra-upload.component';

/**
 * Cancel flow tests for the My Uploads page. The component is instantiated directly with spy
 * object dependencies (no TestBed), matching basic-uploader.component.spec.ts
 */
describe('SpectraUploadComponent', () => {
  let component: SpectraUploadComponent;
  let uploadJobResource: any;
  let chunkedUploadService: any;
  let toaster: any;
  let logger: any;

  const makeJob = (status: string): any => ({id: 'job-1', fileName: 'test.msp', status});

  beforeEach(() => {
    uploadJobResource = jasmine.createSpyObj('UploadJobResource', ['cancelJob', 'listMyJobs', 'deletionStatus']);
    chunkedUploadService = jasmine.createSpyObj('ChunkedUploadService', ['cancelUpload']);
    toaster = jasmine.createSpyObj('ToasterService', ['pop']);
    logger = jasmine.createSpyObj('NGXLogger', ['debug', 'info', 'error']);

    const authenticationService: any = {
      getCurrentUser: () => ({accessToken: 'token'}),
      isLoggedIn: () => true
    };

    component = new SpectraUploadComponent(
      authenticationService,
      logger,
      {} as any,
      uploadJobResource,
      {} as any,
      {} as any,
      toaster,
      {} as any,
      {} as any,
      chunkedUploadService
    );
  });

  describe('canCancel', () => {
    it('offers cancel for in flight statuses', () => {
      ['UPLOADING', 'INTERRUPTED', 'SCHEDULED', 'RUNNING'].forEach((status) => {
        expect(component.canCancel(makeJob(status))).toBe(true, status);
      });
    });

    it('does not offer cancel for terminal or draining statuses', () => {
      ['CANCELLING', 'CANCELLED', 'COMPLETE', 'FAILED', 'DELETING', 'DELETED'].forEach((status) => {
        expect(component.canCancel(makeJob(status))).toBe(false, status);
      });
    });
  });

  it('treats CANCELLING as running so delete stays disabled and the bar stays striped', () => {
    expect(component.isRunning(makeJob('CANCELLING'))).toBe(true);
    expect(component.isRunning(makeJob('CANCELLED'))).toBe(false);
  });

  it('keeps polling while a job is CANCELLING', () => {
    component.jobs = [makeJob('CANCELLING')];
    expect(component.hasActiveJobs()).toBe(true);

    component.jobs = [makeJob('CANCELLED')];
    expect(component.hasActiveJobs()).toBe(false);
  });

  describe('deletion progress', () => {
    it('drives the DELETING bar from the deletion job behind it', () => {
      const job = makeJob('DELETING');
      uploadJobResource.listMyJobs.and.returnValue(of([job]));
      uploadJobResource.deletionStatus.and.returnValue(of({status: 'RUNNING', total: 10, deleted: 4, skipped: 1}));

      component.loadJobs();

      expect(uploadJobResource.deletionStatus).toHaveBeenCalledWith('job-1', 'token');
      expect(component.jobProgress(job)).toBe(50);
    });

    it('shows zero progress until the deletion status arrives', () => {
      expect(component.jobProgress(makeJob('DELETING'))).toBe(0);
    });

    it('shows zero progress for a deletion job with no total', () => {
      const job = makeJob('DELETING');
      uploadJobResource.listMyJobs.and.returnValue(of([job]));
      uploadJobResource.deletionStatus.and.returnValue(of({status: 'RUNNING', total: 0, deleted: 0, skipped: 0}));

      component.loadJobs();

      expect(component.jobProgress(job)).toBe(0);
    });

    it('does not fetch deletion status for rows that are not DELETING', () => {
      uploadJobResource.listMyJobs.and.returnValue(of([makeJob('COMPLETE'), makeJob('DELETED')]));

      component.loadJobs();

      expect(uploadJobResource.deletionStatus).not.toHaveBeenCalled();
    });

    it('keeps polling while a job is DELETING', () => {
      component.jobs = [makeJob('DELETING')];
      expect(component.hasActiveJobs()).toBe(true);

      component.jobs = [makeJob('DELETED')];
      expect(component.hasActiveJobs()).toBe(false);
    });

    it('logs and keeps the last known progress when the deletion status fetch fails', () => {
      const job = makeJob('DELETING');
      component.deletionProgress['job-1'] = 30;
      uploadJobResource.listMyJobs.and.returnValue(of([job]));
      uploadJobResource.deletionStatus.and.returnValue(throwError({status: 404}));

      component.loadJobs();

      expect(component.jobProgress(job)).toBe(30);
      expect(logger.error).toHaveBeenCalled();
    });
  });

  describe('cancelJob', () => {
    it('stops a local chunk loop, replaces the row, and reports a pending cancel', () => {
      const job = makeJob('RUNNING');
      component.jobs = [job];
      uploadJobResource.cancelJob.and.returnValue(of({...job, status: 'CANCELLING'}));

      component.cancelJob(job);

      expect(chunkedUploadService.cancelUpload).toHaveBeenCalledWith('job-1');
      expect(uploadJobResource.cancelJob).toHaveBeenCalledWith('job-1', 'token');
      expect(component.jobs[0].status).toBe('CANCELLING');
      expect(toaster.pop).toHaveBeenCalledWith(jasmine.objectContaining({type: 'success'}));
    });

    it('reports an immediate cancel for a job that was never enqueued', () => {
      const job = makeJob('UPLOADING');
      component.jobs = [job];
      uploadJobResource.cancelJob.and.returnValue(of({...job, status: 'CANCELLED'}));

      component.cancelJob(job);

      expect(component.jobs[0].status).toBe('CANCELLED');
      expect(toaster.pop).toHaveBeenCalledWith(jasmine.objectContaining({type: 'success'}));
    });

    it('pops an error toast and leaves the row alone when the cancel fails', () => {
      const job = makeJob('RUNNING');
      component.jobs = [job];
      uploadJobResource.cancelJob.and.returnValue(throwError({status: 409}));

      component.cancelJob(job);

      expect(component.jobs[0].status).toBe('RUNNING');
      expect(toaster.pop).toHaveBeenCalledWith(jasmine.objectContaining({type: 'error'}));
      expect(logger.error).toHaveBeenCalled();
    });
  });
});
