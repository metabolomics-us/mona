import {Subject, of, throwError} from 'rxjs';
import {DiagnosticsComponent} from './diagnostics.component';

describe('DiagnosticsComponent', () => {
  let component: DiagnosticsComponent;
  let auth: any;
  let adminService: any;
  let logger: any;
  let toaster: any;
  let authSubject: Subject<boolean>;

  const SERVICES = {webhooks: 'webhooks-service', proxy: 'proxy-service', auth: 'auth-service'};

  beforeEach(() => {
    authSubject = new Subject<boolean>();
    logger = jasmine.createSpyObj('NGXLogger', ['trace', 'debug', 'info', 'log', 'warn', 'error']);
    toaster = jasmine.createSpyObj('ToasterService', ['pop']);
    adminService = jasmine.createSpyObj('AdminService', ['getDiagnosticServices', 'getDiagnosticLogs', 'getDiagnosticLogTrace']);
    auth = {
      isAuthenticated: authSubject.asObservable(),
      isAdmin: jasmine.createSpy('isAdmin').and.returnValue(true),
      getCurrentUser: jasmine.createSpy('getCurrentUser').and.returnValue({accessToken: 'admin-token'})
    };

    component = new DiagnosticsComponent(auth, adminService, logger, toaster);
  });

  describe('ngOnInit / loadServices', () => {
    it('loads and sorts services, selects the first, and refreshes once authenticated as admin', () => {
      adminService.getDiagnosticServices.and.returnValue(of(SERVICES));
      adminService.getDiagnosticLogs.and.returnValue(of({entries: [], exists: true, truncated: false}));

      component.ngOnInit();
      authSubject.next(true);

      expect(component.serviceKeys).toEqual(['auth', 'proxy', 'webhooks']);
      expect(component.selectedService).toBe('auth');
      expect(adminService.getDiagnosticLogs).toHaveBeenCalledWith('admin-token', 'auth', 1);
    });

    it('does not load services for a non-admin user', () => {
      auth.isAdmin.and.returnValue(false);

      component.ngOnInit();
      authSubject.next(true);

      expect(adminService.getDiagnosticServices).not.toHaveBeenCalled();
    });

    it('only loads services once even if isAuthenticated fires again', () => {
      adminService.getDiagnosticServices.and.returnValue(of(SERVICES));
      adminService.getDiagnosticLogs.and.returnValue(of({entries: []}));

      component.ngOnInit();
      authSubject.next(true);
      authSubject.next(true);

      expect(adminService.getDiagnosticServices).toHaveBeenCalledTimes(1);
    });

    it('surfaces an error toast and logs when loading services fails', () => {
      adminService.getDiagnosticServices.and.returnValue(throwError({message: 'boom'}));

      component.ngOnInit();
      authSubject.next(true);

      expect(logger.error).toHaveBeenCalled();
      expect(toaster.pop).toHaveBeenCalledWith(jasmine.objectContaining({type: 'error', title: 'Failed to load services'}));
    });
  });

  describe('refresh', () => {
    beforeEach(() => {
      component.ngOnInit();
    });

    it('does nothing when there is no selected service', () => {
      component.selectedService = null;

      component.refresh();

      expect(adminService.getDiagnosticLogs).not.toHaveBeenCalled();
    });

    it('does nothing for a non-admin user', () => {
      auth.isAdmin.and.returnValue(false);
      component.selectedService = 'webhooks';

      component.refresh();

      expect(adminService.getDiagnosticLogs).not.toHaveBeenCalled();
    });

    it('maps entries and reflects the exists/truncated flags from the response', () => {
      component.selectedService = 'webhooks';
      adminService.getDiagnosticLogs.and.returnValue(of({
        exists: false,
        truncated: true,
        entries: [{timestamp: Date.now(), message: 'boom'}]
      }));

      component.refresh();

      expect(component.logs.length).toBe(1);
      expect(component.logs[0]).toEqual(jasmine.objectContaining({expanded: false, trace: null, traceLoading: false}));
      expect(component.serviceExists).toBe(false);
      expect(component.truncated).toBe(true);
      expect(component.loading).toBe(false);
    });

    it('defaults serviceExists to true and truncated to false when the response omits them', () => {
      component.selectedService = 'webhooks';
      adminService.getDiagnosticLogs.and.returnValue(of({entries: []}));

      component.refresh();

      expect(component.serviceExists).toBe(true);
      expect(component.truncated).toBe(false);
    });

    it('clears the loading flag and logs an error when fetching logs fails', () => {
      component.selectedService = 'webhooks';
      adminService.getDiagnosticLogs.and.returnValue(throwError({message: 'boom'}));

      component.refresh();

      expect(component.loading).toBe(false);
      expect(logger.error).toHaveBeenCalled();
      expect(toaster.pop).toHaveBeenCalledWith(jasmine.objectContaining({type: 'error', title: 'Failed to load logs'}));
    });

    it('buckets entries into 24 slots that together account for every entry in the window', () => {
      component.selectedService = 'webhooks';
      component.hours = 1;
      const now = Date.now();
      const entries = [
        {timestamp: now, message: 'a'},
        {timestamp: now - 60000, message: 'b'},
        {timestamp: now - 3600000 * 2, message: 'clamped-to-the-oldest-bucket'}
      ];
      adminService.getDiagnosticLogs.and.returnValue(of({entries}));

      component.refresh();

      expect(component.chartData[0].values.length).toBe(24);
      const total = component.chartData[0].values.reduce((sum: number, v: any) => sum + v.y, 0);
      expect(total).toBe(entries.length);
    });
  });

  describe('toggleExpand', () => {
    it('fetches the trace on first expand and does not refetch on later expands', () => {
      const entry: any = {timestamp: 12345, expanded: false, trace: null, traceLoading: false};
      component.selectedService = 'webhooks';
      adminService.getDiagnosticLogTrace.and.returnValue(of('  at Foo.bar'));

      component.toggleExpand(entry);
      expect(entry.expanded).toBe(true);
      expect(entry.trace).toBe('  at Foo.bar');
      expect(adminService.getDiagnosticLogTrace).toHaveBeenCalledTimes(1);

      component.toggleExpand(entry);
      expect(entry.expanded).toBe(false);

      component.toggleExpand(entry);
      expect(entry.expanded).toBe(true);
      expect(adminService.getDiagnosticLogTrace).toHaveBeenCalledTimes(1);
    });

    it('clears traceLoading and logs an error when fetching the trace fails', () => {
      const entry: any = {timestamp: 12345, expanded: false, trace: null, traceLoading: false};
      component.selectedService = 'webhooks';
      adminService.getDiagnosticLogTrace.and.returnValue(throwError({message: 'boom'}));

      component.toggleExpand(entry);

      expect(entry.traceLoading).toBe(false);
      expect(entry.trace).toBeNull();
      expect(logger.error).toHaveBeenCalled();
    });
  });
});
