import {SimilaritySearchFormComponent} from './similarity-search-form.component';

describe('SimilaritySearchFormComponent', () => {
  let component: SimilaritySearchFormComponent;
  let uploadLibraryService: any;
  let logger: any;

  const validSpectrum = () => ({names: [''], meta: [], spectrum: '10:100 20:999'});

  const fileEvent = (name: string) => ({target: {files: [new File(['data'], name)]}});

  beforeEach(() => {
    logger = jasmine.createSpyObj('NGXLogger', ['trace', 'debug', 'info', 'log', 'warn', 'error']);
    uploadLibraryService = {
      loadSpectraFile: jasmine.createSpy('loadSpectraFile'),
      processData: jasmine.createSpy('processData')
    };

    component = new SimilaritySearchFormComponent(
      logger,
      uploadLibraryService,
      {} as any,
      {} as any,
      {} as any
    );
    component.ngOnInit();
  });

  it('returns to the form and shows the error when parsing rejects with an unsupported type', async () => {
    uploadLibraryService.loadSpectraFile.and.returnValue(
      Promise.reject(new Error('Unsupported file type .py. Supported file types: .msp, .mgf, .txt')));

    await component.parseFiles(fileEvent('test.py'));

    expect(component.page).toBe(0);
    expect(component.uploadError).toMatch(/unsupported file type/i);
  });

  it('returns to the form and shows the error for a mislabeled extension', async () => {
    uploadLibraryService.loadSpectraFile.and.returnValue(
      Promise.reject(new Error('File uploaded was .msp, but detected as .mgf')));

    await component.parseFiles(fileEvent('foo.msp'));

    expect(component.page).toBe(0);
    expect(component.uploadError).toBe('File uploaded was .msp, but detected as .mgf');
  });

  it('reports when parsing yields zero spectra instead of hanging', async () => {
    uploadLibraryService.loadSpectraFile.and.returnValue(Promise.resolve());

    await component.parseFiles(fileEvent('empty.msp'));

    expect(component.page).toBe(0);
    expect(component.uploadError).toMatch(/no valid mass spectra/i);
  });

  it('treats a null spectrum from the parser as no valid spectra without crashing', async () => {
    uploadLibraryService.loadSpectraFile.and.callFake((file, cb) => Promise.resolve(cb([['block']], file.name)));
    uploadLibraryService.processData.and.callFake((data, cb, origin) => cb(null));

    await component.parseFiles(fileEvent('partial.msp'));

    expect(component.page).toBe(0);
    expect(component.uploadError).toMatch(/no valid mass spectra/i);
  });

  it('shows the spectrum viewer when parsing succeeds', async () => {
    uploadLibraryService.loadSpectraFile.and.callFake((file, cb) => Promise.resolve(cb([['block']], file.name)));
    uploadLibraryService.processData.and.callFake((data, cb, origin) => cb(validSpectrum()));

    await component.parseFiles(fileEvent('test.msp'));

    expect(component.page).toBe(2);
    expect(component.spectrum).toBe('10:100 20:999');
    expect(component.uploadError).toBeNull();
  });
});
