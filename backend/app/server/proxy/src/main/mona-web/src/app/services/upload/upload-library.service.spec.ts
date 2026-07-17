import {UploadLibraryService} from './upload-library.service';
import {MspParserLibService} from 'angular-msp-parser/dist/msp-parser-lib';
import {MgfParserLibService} from 'angular-mgf-parser/dist/mgf-parser-lib';
import {MassbankParserLibService} from 'angular-massbank-parser/dist/massbank-parser-lib';
import {of} from 'rxjs';

describe('UploadLibraryService', () => {
  let service: UploadLibraryService;
  let logger: any;

  const MSP_CONTENT =
    'Name: Test Compound\n' +
    'InChIKey: QNAYBMKLOCPYGJ-REOHCLBHSA-N\n' +
    'Num Peaks: 3\n' +
    '10 100\n' +
    '20 200\n' +
    '30 999\n';

  const MGF_CONTENT =
    'BEGIN IONS\n' +
    'PEPMASS=278.190\n' +
    'CHARGE=1+\n' +
    'TITLE=Test\n' +
    '100.101 3.5\n' +
    '101.202 10.0\n' +
    'END IONS\n';

  const MASSBANK_CONTENT =
    'ACCESSION: TEST00001\n' +
    'RECORD_TITLE: Test; LC-ESI-QTOF; MS2\n' +
    'CH$NAME: Test Compound\n' +
    'CH$IUPAC: InChI=1S/CH4/h1H4\n' +
    'AC$MASS_SPECTROMETRY: MS_TYPE MS2\n' +
    'PK$NUM_PEAK: 2\n' +
    'PK$PEAK: m/z int. rel.int.\n' +
    '  100.101 200 999\n' +
    '  110.202 100 500\n' +
    '//\n';

  // SDF-like structure data that matches none of the supported spectra formats
  const SDF_CONTENT =
    'Structure1\n' +
    '  ChemDraw\n' +
    '\n' +
    '  1  0  0  0  0  0  0  0  0  0999 V2000\n' +
    '    0.0000    0.0000    0.0000 C   0  0\n' +
    'M  END\n' +
    '$$$$\n';

  // Loads a file through the full loadSpectraFile then processData flow and
  // collects every spectrum delivered to the callback
  const loadAndParse = async (file: File): Promise<any[]> => {
    const spectra = [];
    await service.loadSpectraFile(file, (data, origin) => {
      service.processData(data, (spectrum) => spectra.push(spectrum), origin);
    });
    return spectra;
  };

  beforeEach(() => {
    logger = jasmine.createSpyObj('NGXLogger', ['trace', 'debug', 'info', 'log', 'warn', 'error']);
    service = new UploadLibraryService(
      logger,
      new MspParserLibService(null as any, logger),
      new MgfParserLibService(logger),
      {isAuthenticated: of(false)} as any,
      new MassbankParserLibService(logger),
      null as any,
      {} as any,
      {} as any,
      {} as any,
      {} as any
    );
  });

  describe('unsupported file extensions', () => {
    it('rejects a file with an unsupported extension without invoking the callback', async () => {
      const callback = jasmine.createSpy('callback');
      const file = new File(['print(1)'], 'test.py');

      await expectAsync(service.loadSpectraFile(file, callback))
        .toBeRejectedWithError(/unsupported file type/i);
      expect(callback).not.toHaveBeenCalled();
    });

    it('rejects a file with no extension', async () => {
      const file = new File(['some content'], 'README');

      await expectAsync(service.loadSpectraFile(file, () => {}))
        .toBeRejectedWithError(/unsupported file type/i);
    });

    it('rejects an sdf file since sdf is not a supported spectra format', async () => {
      const file = new File([SDF_CONTENT], 'structures.sdf');

      await expectAsync(service.loadSpectraFile(file, () => {}))
        .toBeRejectedWithError(/unsupported file type/i);
    });

    it('uses only the trailing extension so an inner .msp does not count', async () => {
      const file = new File([MSP_CONTENT], 'data.msp.old.py');

      await expectAsync(service.loadSpectraFile(file, () => {}))
        .toBeRejectedWithError(/unsupported file type/i);
    });
  });

  describe('mislabeled file extensions', () => {
    it('rejects mgf content in a .msp file and names the detected format', async () => {
      const file = new File([MGF_CONTENT], 'foo.msp');

      await expectAsync(service.loadSpectraFile(file, () => {}))
        .toBeRejectedWithError('File uploaded was .msp, but detected as .mgf');
    });

    it('rejects msp content in a .mgf file and names the detected format', async () => {
      const file = new File([MSP_CONTENT], 'foo.mgf');

      await expectAsync(service.loadSpectraFile(file, () => {}))
        .toBeRejectedWithError('File uploaded was .mgf, but detected as .msp');
    });

    it('rejects massbank content in a .mgf file and names the detected format', async () => {
      const file = new File([MASSBANK_CONTENT], 'foo.mgf');

      await expectAsync(service.loadSpectraFile(file, () => {}))
        .toBeRejectedWithError('File uploaded was .mgf, but detected as .txt');
    });
  });

  describe('content that matches no supported format', () => {
    it('resolves with zero spectra for undetectable content in a supported extension', async () => {
      const spectra = await loadAndParse(new File([SDF_CONTENT], 'foo.mgf'));

      expect(spectra.length).toBe(0);
    });

    it('resolves with zero spectra for an empty file', async () => {
      const spectra = await loadAndParse(new File([''], 'empty.msp'));

      expect(spectra.length).toBe(0);
    });
  });

  describe('valid files parse successfully', () => {
    it('parses a valid msp file and tags the origin', async () => {
      const spectra = await loadAndParse(new File([MSP_CONTENT], 'test.msp'));

      expect(spectra.length).toBe(1);
      expect(spectra[0].spectrum).toContain('10:100');
      expect(spectra[0].meta.some((m) => m.name === 'origin' && m.value === 'test.msp')).toBeTrue();
    });

    it('parses a valid mgf file', async () => {
      const spectra = await loadAndParse(new File([MGF_CONTENT], 'test.mgf'));

      expect(spectra.length).toBe(1);
      expect(spectra[0].spectrum).toContain('100.101:3.5');
    });

    it('parses a valid massbank txt file', async () => {
      const spectra = await loadAndParse(new File([MASSBANK_CONTENT], 'test.txt'));

      expect(spectra.length).toBe(1);
      expect(spectra[0].names).toContain('Test Compound');
    });

    it('handles uppercase extensions', async () => {
      const spectra = await loadAndParse(new File([MSP_CONTENT], 'TEST.MSP'));

      expect(spectra.length).toBe(1);
    });
  });

  describe('total spectra pre-count for the progress bar', () => {
    it('records the full msp spectra count when the file is read', async () => {
      const multiMsp = MSP_CONTENT + '\n' + MSP_CONTENT + '\n' + MSP_CONTENT;

      await loadAndParse(new File([multiMsp], 'multi.msp'));

      expect(service.totalSpectraCount).toBe(3);
    });

    it('records the full mgf spectra count when the file is read', async () => {
      const multiMgf = MGF_CONTENT + '\n' + MGF_CONTENT;

      await loadAndParse(new File([multiMgf], 'multi.mgf'));

      expect(service.totalSpectraCount).toBe(2);
    });

    it('records the massbank record count when the file is read', async () => {
      await loadAndParse(new File([MASSBANK_CONTENT], 'test.txt'));

      expect(service.totalSpectraCount).toBe(1);
    });

    it('accumulates the total across multiple files', async () => {
      await loadAndParse(new File([MSP_CONTENT], 'one.msp'));
      await loadAndParse(new File([MSP_CONTENT], 'two.msp'));

      expect(service.totalSpectraCount).toBe(2);
    });

    it('counts a marker even when it is split across chunk boundaries', () => {
      const marker = 'Num Peaks: 3\n';
      const chunkSize = 3 * 1024 * 1024;
      // Position one marker so it straddles the first chunk boundary
      const content = 'x'.repeat(chunkSize - 5) + marker + 'y'.repeat(100);
      const buffer = new TextEncoder().encode(content).buffer;

      expect(service.countSpectraInBuffer(buffer, 'msp')).toBe(1);
    });
  });

  describe('processData null spectrum guard', () => {
    it('forwards null to the callback without throwing when the parser yields null', () => {
      spyOn(service.mspParserLibService, 'convertFromData').and.callFake((data, cb: any) => cb(null));
      const callback = jasmine.createSpy('callback');

      expect(() => service.processData([['block']], callback, 'file.msp')).not.toThrow();
      expect(callback).toHaveBeenCalledWith(null);
    });
  });
});
