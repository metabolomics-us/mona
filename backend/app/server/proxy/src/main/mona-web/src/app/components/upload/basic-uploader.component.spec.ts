import {BasicUploaderComponent} from './basic-uploader.component';

describe('BasicUploaderComponent', () => {
  let component: BasicUploaderComponent;
  let uploadLibraryService: any;
  let logger: any;

  const validSpectrum = () => ({names: [''], meta: [], spectrum: '10:100 20:999'});

  const fileEvent = (name: string) => ({target: {files: [new File(['data'], name)]}});

  beforeEach(() => {
    logger = jasmine.createSpyObj('NGXLogger', ['trace', 'debug', 'info', 'log', 'warn', 'error']);
    uploadLibraryService = {
      isSTP: false,
      loadSpectraFile: jasmine.createSpy('loadSpectraFile'),
      processData: jasmine.createSpy('processData'),
      isUploading: jasmine.createSpy('isUploading').and.returnValue(false),
      uploadSpectra: jasmine.createSpy('uploadSpectra')
    };

    component = new BasicUploaderComponent(
      {} as any,
      uploadLibraryService,
      {} as any,
      {} as any,
      logger,
      {} as any,
      {} as any,
      {} as any,
      {} as any,
      {} as any
    );
    component.ngOnInit();
  });

  it('clears the spinner and shows the error when parsing rejects with an unsupported type', async () => {
    uploadLibraryService.loadSpectraFile.and.returnValue(
      Promise.reject(new Error('Unsupported file type .py. Supported file types: .msp, .mgf, .txt')));

    await component.parseFiles(fileEvent('test.py'));

    expect(component.page).toBe(0);
    expect(component.uploadError).toMatch(/unsupported file type/i);
  });

  it('clears the spinner and shows the error when parsing rejects with a mislabeled extension', async () => {
    uploadLibraryService.loadSpectraFile.and.returnValue(
      Promise.reject(new Error('File uploaded was .msp, but detected as .mgf')));

    await component.parseFiles(fileEvent('foo.msp'));

    expect(component.page).toBe(0);
    expect(component.uploadError).toBe('File uploaded was .msp, but detected as .mgf');
  });

  it('reaches the no valid spectra state when parsing yields zero spectra', async () => {
    uploadLibraryService.loadSpectraFile.and.returnValue(Promise.resolve());

    await component.parseFiles(fileEvent('empty.msp'));

    expect(component.page).toBe(2);
    expect(component.currentSpectrum).toBeNull();
  });

  it('treats a null spectrum from the parser as no valid spectra without crashing', async () => {
    uploadLibraryService.loadSpectraFile.and.callFake((file, cb) => Promise.resolve(cb([['block']], file.name)));
    uploadLibraryService.processData.and.callFake((data, cb, origin) => cb(null));

    await component.parseFiles(fileEvent('partial.msp'));

    expect(component.page).toBe(2);
    expect(component.currentSpectrum).toBeNull();
  });

  it('displays the spectrum when parsing succeeds', async () => {
    uploadLibraryService.loadSpectraFile.and.callFake((file, cb) => Promise.resolve(cb([['block']], file.name)));
    uploadLibraryService.processData.and.callFake((data, cb, origin) => cb(validSpectrum()));

    await component.parseFiles(fileEvent('test.msp'));

    expect(component.page).toBe(2);
    expect(component.currentSpectrum).not.toBeNull();
    expect(component.currentSpectrum.ions.length).toBe(2);
    expect(component.uploadError).toBeNull();
  });
});
