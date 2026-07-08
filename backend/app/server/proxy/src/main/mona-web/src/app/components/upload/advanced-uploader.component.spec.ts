import {AdvancedUploaderComponent} from './advanced-uploader.component';
import {of} from 'rxjs';

describe('AdvancedUploaderComponent', () => {
  let component: AdvancedUploaderComponent;
  let uploadLibraryService: any;
  let toaster: any;
  let asyncService: any;
  let logger: any;

  const validSpectrum = () => ({names: ['Test'], meta: [], spectrum: '10:100 20:999'});

  const makeFiles = (...names: string[]) => names.map((n) => new File(['data'], n));

  beforeEach(() => {
    logger = jasmine.createSpyObj('NGXLogger', ['trace', 'debug', 'info', 'log', 'warn', 'error']);
    toaster = jasmine.createSpyObj('ToasterService', ['pop']);
    asyncService = jasmine.createSpyObj('AsyncService', ['addToPool', 'resetPool', 'hasPooledTasks']);
    asyncService.addToPool.and.callFake((fn) => fn());
    uploadLibraryService = {
      isSTP: false,
      completedSpectraCount: 0,
      failedSpectraCount: 0,
      uploadedSpectraCount: 0,
      loadSpectraFile: jasmine.createSpy('loadSpectraFile'),
      processData: jasmine.createSpy('processData')
    };
    const tagService = {allTags: () => of([])};

    component = new AdvancedUploaderComponent(
      {} as any,
      {} as any,
      uploadLibraryService,
      tagService as any,
      asyncService,
      logger,
      {nativeElement: {}} as any,
      {} as any,
      {} as any,
      {} as any,
      {} as any,
      toaster,
      {} as any,
      {} as any
    );
    component.ngOnInit();
  });

  it('shows a readable error and resets the form when every file fails to parse', async () => {
    component.files = makeFiles('test.py');
    component.fileUpload = 'test.py';
    uploadLibraryService.loadSpectraFile.and.returnValue(
      Promise.reject(new Error('Unsupported file type .py. Supported file types: .msp, .mgf, .txt')));

    await component.parseFiles();

    expect(toaster.pop).toHaveBeenCalled();
    const popArgs = toaster.pop.calls.mostRecent().args[0];
    expect(popArgs.type).toBe('error');
    expect(popArgs.title).toBe(`Error parsing file: 'test.py'`);
    expect(String(popArgs.body)).toMatch(/unsupported file type/i);
    expect(String(popArgs.body)).not.toMatch(/TypeError|exec/);
    expect(component.spectraLoaded).toBe(0);
    expect(component.fileUpload).toBeNull();
  });

  it('shows the mislabel message when a file is detected as another format', async () => {
    component.files = makeFiles('foo.msp');
    component.fileUpload = 'foo.msp';
    uploadLibraryService.loadSpectraFile.and.returnValue(
      Promise.reject(new Error('File uploaded was .msp, but detected as .mgf')));

    await component.parseFiles();

    const popArgs = toaster.pop.calls.mostRecent().args[0];
    expect(String(popArgs.body)).toBe('File uploaded was .msp, but detected as .mgf');
  });

  it('reaches the no valid spectra state when parsing yields zero spectra', async () => {
    component.files = makeFiles('empty.msp');
    uploadLibraryService.loadSpectraFile.and.returnValue(Promise.resolve());

    await component.parseFiles();

    expect(component.spectraLoaded).toBe(2);
    expect(component.spectra.length).toBe(0);
  });

  it('still loads good files when one file in the batch fails', async () => {
    component.files = makeFiles('bad.py', 'good.msp');
    uploadLibraryService.loadSpectraFile.and.callFake((file, cb) => {
      if (file.name === 'bad.py') {
        return Promise.reject(new Error('Unsupported file type .py. Supported file types: .msp, .mgf, .txt'));
      }
      return Promise.resolve(cb([['block']], file.name));
    });
    uploadLibraryService.processData.and.callFake((item, cb, origin) => cb(validSpectrum()));

    await component.parseFiles();
    await Promise.resolve();

    expect(component.spectra.length).toBe(1);
    expect(component.spectraLoaded).toBe(2);
    expect(toaster.pop).toHaveBeenCalledTimes(1);
    expect(toaster.pop.calls.mostRecent().args[0].title).toBe(`Error parsing file: 'bad.py'`);
    expect(String(toaster.pop.calls.mostRecent().args[0].body)).toMatch(/unsupported file type/i);
  });
});
