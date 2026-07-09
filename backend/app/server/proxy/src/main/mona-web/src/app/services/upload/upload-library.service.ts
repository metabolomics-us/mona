/**
 * Created by wohlgemuth on 6/25/14.
 *
 * handles the upload of library spectra to the system
 */

import {NGXLogger} from 'ngx-logger';
import {MspParserLibService} from 'angular-msp-parser/dist/msp-parser-lib';
import {MgfParserLibService} from 'angular-mgf-parser/dist/mgf-parser-lib';
import {AuthenticationService} from '../authentication.service';
import {MassbankParserLibService} from 'angular-massbank-parser/dist/massbank-parser-lib';
import {HttpClient} from '@angular/common/http';
import {AsyncService} from './async.service';
import {MetadataOptimization} from '../optimization/metadata-optimization.service';
import { Subject } from 'rxjs';
import {Injectable} from '@angular/core';
import {first} from 'rxjs/operators';
import {UploadJobResource} from './upload-job.resource';
import {UploadJobService} from './upload-job.service';

@Injectable()
export class UploadLibraryService{
    completedSpectraCountSub = new Subject<number>();
    failedSpectraCountSub = new Subject<number>();
    uploadedSpectraCountSub = new Subject<number>();
    uploadProcess = new Subject<boolean>();

    completedSpectraCount;
    failedSpectraCount;
    uploadedSpectraCount;
    totalSpectraCount;
    uploadedSpectra;

    uploadStartTime;
    uploadComplete;

    // Context for recording an interactive (small file) upload as an UploadJob for history. Set
    // when a batch starts, cleared once the record is written so each batch is recorded exactly once
    private interactiveRecord: {fileName: string, libraryName: string, expectedTotal: number, token: string,
        librarySubmitterEmail?: string, librarySubmitterFirstName?: string, librarySubmitterLastName?: string,
        librarySubmitterInstitution?: string} = null;

    // A refresh or close kills an interactive batch along with this in memory state.
    // On the next visit a snapshot that is no longer being updated is recorded as an interrupted upload in the history
    private readonly INTERACTIVE_SNAPSHOT_KEY = 'mona.upload.interactive';
    private readonly SNAPSHOT_STALE_AFTER = 15000;
    private recoveryTimer = null;

    constructor(public logger: NGXLogger,
                public mspParserLibService: MspParserLibService,
                public mgfParserLibService: MgfParserLibService,
                public authenticationService: AuthenticationService,
                public massbankParserLibService: MassbankParserLibService,
                public http: HttpClient,
                public asyncService: AsyncService,
                public metadataOptimization: MetadataOptimization,
                public uploadJobResource: UploadJobResource,
                public uploadJobService: UploadJobService){
        this.completedSpectraCount = 0;
        this.failedSpectraCount = 0;
        this.uploadedSpectraCount = 0;
        this.totalSpectraCount = 0;
        this.uploadStartTime = -1;
        this.uploadProcess.next(true);
        this.uploadedSpectra = [];

        // Once the login state is available, check whether a previous visit left an interrupted
        // interactive upload behind and record it in the history
        this.authenticationService.isAuthenticated.subscribe((isAuthenticated: boolean) => {
            if (isAuthenticated) {
                this.recoverInterruptedUpload();
            }
        });
    }

    /**
     * Resolves a spectrum for upload. CTS-based structure enrichment was removed because curation now
     * resolves the InChI, MOL, SMILES and name server-side from any provided identifier, so we only need
     * to confirm the spectrum carries a structure to work from. A compound name on its own cannot be
     * resolved since the CTS name lookup was retired and replaced with CTS-Lite
     * @param spectra type object
     * @returns promise that resolves with the spectrum, or rejects when no structure identifier is present
     */
     obtainKey(spectra): Promise<any> {
        return new Promise((resolve, reject) => {
            if (spectra.inchi || spectra.inchiKey || spectra.smiles) {
                resolve(spectra);
            } else {
                reject('sorry, the given object was invalid. We need an InChI code, InChIKey, or SMILES for this to work!');
            }
        });
    }

    /**
     * assembles a spectra and prepares it for upload
     * @param submitter submitter
     * @param saveSpectrumCallback callback
     * @param spectrumObject spectrum
     * @param additionalData optional
     */
     workOnSpectra(submitter, saveSpectrumCallback, spectrumObject, additionalData): Promise<any> {
        const myPromise = new Promise((resolve, reject) => {
            // if we have a key or an inchi
            if (spectrumObject.inchiKey !== null && spectrumObject.inchi !== null) {
                this.submitSpectrum(spectrumObject, submitter, saveSpectrumCallback, additionalData).then((submittedSpectra) => {
                    // assign our result
                    resolve(submittedSpectra);
                });
            }

            // we need to get a key or inchi code
            else {
                // get the key
                this.obtainKey(spectrumObject).then((spectrumWithKey: any) => {
                    // submit as long as we have a structure identifier, curation resolves the rest server-side
                    if (spectrumWithKey.inchi || spectrumWithKey.molFile || spectrumWithKey.inchiKey || spectrumWithKey.smiles) {
                        this.submitSpectrum(spectrumWithKey, submitter, saveSpectrumCallback, additionalData).then((submittedSpectra) => {
                            resolve(submittedSpectra);
                        });
                    }

                    else {
                        this.logger.error('invalid ' + JSON.stringify(spectrumWithKey));
                        reject(new Error('dropped object from submission, since it was declared invalid, it had no InChI, InChIKey, SMILES or MOL file to resolve a structure from!'));
                    }
                }).catch((error) => {
                    this.logger.warn(error + '\n' + JSON.stringify(spectrumObject));
                    reject(error);
                });
            }
        });

        return myPromise;
    }

    /**
     *
     * @param spectra object of multiple spectrum
     * @param submitter person who submitted
     * @param saveSpectrumCallback helper callback
     * @param additionalData optional
     */
    submitSpectrum(spectra, submitter, saveSpectrumCallback, additionalData): Promise<any> {
        // optimize all our metadata
        const myPromise = new Promise((resolve) => {
            this.metadataOptimization.optimizeMetaData(spectra.meta).then((metaData: object) => {
                const s = this.buildSpectrum();
                if (typeof spectra.id !== 'undefined' && spectra.id !== null) {
                  s.id = spectra.id;
                }
                // assign structure information
                if (typeof spectra.inchiKey !== 'undefined' && spectra.inchiKey !== null) {
                  s.biologicalCompound.inchiKey = spectra.inchiKey;
                }
                if (typeof spectra.inchi !== 'undefined' && spectra.inchi !== null) {
                  s.biologicalCompound.inchi = spectra.inchi;
                }
                if (typeof spectra.smiles !== 'undefined' && spectra.smiles !== null && spectra.smiles !== '') {
                  s.biologicalCompound.metaData.push({category: 'none', computed: false, hidden: false,
                    name: 'SMILES', value: spectra.smiles});
                }

                if (typeof spectra.molFile !== 'undefined' && spectra.molFile !== null) {
                    s.biologicalCompound.molFile = spectra.molFile.toString();
                }

                // assign all the defined names of the spectra
                s.biologicalCompound.names = [];

                if (typeof spectra.name !== 'undefined') {
                    // accessing s.spectrum.name here breaks uploading because s.spectrum is undefined
                    // use spectra.name instead (9/3/25)
                    if (spectra.name !== '') {
                      s.biologicalCompound.names.push({name: spectra.name});
                    }
                }

                if (typeof spectra.names !== 'undefined') {
                    for (let i = 0; i < spectra.names.length; i++) {
                        if (spectra.names[i] !== '') {
                          s.biologicalCompound.names.push({name: spectra.names[i]});
                        }
                    }
                }

                s.biologicalCompound.kind = 'biological';

                s.compound = [s.biologicalCompound];
                s.spectrum = spectra.spectrum;

                if (typeof spectra.tags !== 'undefined') {
                    spectra.tags.forEach((tag) => {
                        s.tags.push(tag);
                    });
                }

                if (typeof spectra.library !== 'undefined') {
                  s.library = spectra.library;
                }

                Object.keys(metaData).forEach((e) => {
                    s.metaData.push(metaData[e]);
                });

                if (typeof additionalData !== 'undefined') {
                    if (typeof additionalData.tags !== 'undefined') {
                        additionalData.tags.forEach((tag) => {
                            for (let i = 0; i < s.tags.length; i++) {
                                if (s.tags[i].text === tag.text) {
                                  return;
                                }
                            }

                            s.tags.push(tag);
                        });
                    }

                    if (typeof additionalData.meta !== 'undefined') {
                        additionalData.meta.forEach((e) => {
                            s.metaData.push(e);
                        });
                    }

                    if (typeof additionalData.comments !== 'undefined') {
                        s.comments.push({comment: additionalData.comments});
                    }
                }

                if (spectra.submitter) {
                  s.submitter = spectra.submitter;
                } else {
                  s.submitter = submitter;
                }
                resolve(s);
                // assign our result
                saveSpectrumCallback(s);
            });
        });
        return myPromise;
    }

    /**
     *
     * @returns Spectrum built spectrum
     */
    buildSpectrum() {
        const spectrum = {
            id: undefined,
            biologicalCompound: {names: [],
                inchi: '',
                inchiKey: '',
                molFile: '',
                metaData: [],
                kind: ''
            },
            spectrum: undefined,
            library: undefined,
            tags: [],
            metaData: [],
            compound: [],
            comments: [],
            submitter: ''
        };
        return spectrum;
    }


    /**
     * Returns the supported format for a filename based on its trailing extension,
     * or null when the extension is not a supported spectra format
     * @param filename name of the uploaded file
     */
    getSupportedExtension(filename: string): string {
      const parts = filename.toLowerCase().split('.');
      if (parts.length < 2) {
        return null;
      }
      const extension = parts.pop();
      return ['msp', 'mgf', 'txt'].indexOf(extension) > -1 ? extension : null;
    }

    /**
     * Checks the content for the distinctive marker of a supported format,
     * mirroring what each parser's countSpectra keys on
     * @param content decoded file content
     * @param format one of msp, mgf or txt
     */
    hasFormatMarker(content: string, format: string): boolean {
      if (format === 'mgf') {
        return content.indexOf('BEGIN IONS') > -1;
      } else if (format === 'txt') {
        return content.indexOf('PK$NUM_PEAK') > -1;
      } else if (format === 'msp') {
        return /num\s?peaks\s*:/i.test(content);
      }
      return false;
    }

    /**
     * Detects the actual format of the content regardless of the file extension,
     * or null when no supported format marker is found
     * @param content decoded file content
     */
    detectFormat(content: string): string {
      const formats = ['mgf', 'txt', 'msp'];
      for (const format of formats) {
        if (this.hasFormatMarker(content, format)) {
          return format;
        }
      }
      return null;
    }

    /**
     * Counts spectra in a file buffer by tallying format markers chunk by chunk,
     * so the progress bar knows the true total before every batch has parsed
     * @param arrayBuffer full file contents
     * @param extension one of msp, mgf or txt
     */
    countSpectraInBuffer(arrayBuffer, extension: string): number {
      const chunkSize = 3 * 1024 * 1024;
      const decoder = new TextDecoder();
      const marker = extension === 'mgf' ? /BEGIN IONS/g
        : extension === 'txt' ? /PK\$NUM_PEAK/g
        : /num\s?peaks\s*:/gi;
      let total = 0;
      let tail = '';

      for (let offset = 0; offset < arrayBuffer.byteLength; offset += chunkSize) {
        const text = tail + decoder.decode(arrayBuffer.slice(offset, offset + chunkSize), {stream: true});
        let lastEnd = 0;
        let match;
        marker.lastIndex = 0;
        while ((match = marker.exec(text)) !== null) {
          total++;
          lastEnd = marker.lastIndex;
        }
        // Carry a short tail into the next chunk so a marker split across the
        // boundary is still found, starting after the last counted match so
        // nothing is counted twice
        tail = text.slice(Math.max(text.length - 31, lastEnd));
      }
      return total;
    }

    /**
     * Loads spectra file and returns the data to a callback function
     * @param file filename
     * @param callback helper callback
     * @param fireUploadProgress upload progress
     */
    async loadSpectraFile(file, callback): Promise<any> {
      let count = 0;

      // Fail fast on unsupported extensions before any file reading happens
      const extension = this.getSupportedExtension(file.name);
      if (extension === null) {
        const nameParts = file.name.split('.');
        const shownExtension = nameParts.length > 1 ? ` .${nameParts.pop().toLowerCase()}` : '';
        throw new Error(`Unsupported file type${shownExtension}. Supported file types: .msp, .mgf, .txt`);
      }

      // In order to process data efficiently and in a smaller footprint, the file needs to be sliced into smaller batches
      // that are individually matched by regex pattern.
      const getFileExtension = () => {
        if (extension === 'msp') {
          return new RegExp(/((?:.*:\s*[^\n]*\n?)+)\n((?:\s*[0-9]*\.?[0-9]+\s+[0-9]*\.?[0-9]+[;\n]?.*\n?)*)/g);
        }
        else if (extension === 'mgf') {
          return new RegExp(/BEGIN IONS([\s\S]*?)END IONS/g);
        }
        else {
          return new RegExp(/.*/g);
        }
      };

      // Rejects mislabeled files by comparing the extension against the format
      // detected from the content, so a wrong parser is never silently applied
      const checkFormatMismatch = (arrayBuffer) => {
        const preview = new TextDecoder().decode(arrayBuffer.slice(0, 1024 * 1024));
        if (!this.hasFormatMarker(preview, extension)) {
          const detected = this.detectFormat(preview);
          if (detected !== null) {
            throw new Error(`File uploaded was .${extension}, but detected as .${detected}`);
          }
        }
      };

      const readFileAsync = () => {
        // Create promise that will resolve with an array buffer of the file
        // we use an array buffer because anything else will crash due to memory
        // constraints in the browser i.e. readAsText() function
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => resolve(reader.result);
          reader.onerror = (error) => reject(error.target.error.message);
          reader.readAsArrayBuffer(file);
        });
      };

      const arrayBufferToStringTxtFile = async (arrayBuffer) => {
        // MassBank Txt files are small so load whole file into memory
        const promiseBuffer = [];
        const decoder = new TextDecoder();
        let decodedText;

        decodedText = decoder.decode(arrayBuffer);
        promiseBuffer.push([decodedText]);
        count++;
        await callback(promiseBuffer, file.name);
      };

      const arrayBufferToString = async (arrayBuffer) => {
        // Start with 3MB by default
        const chunkSize = 3 * 1024 * 1024;
        // Buffer only 150 spectrum at a time
        const bufferSize = 200;
        const decoder = new TextDecoder();
        // offset is where we begin our starting slice index
        let offset = 0;
        let foundSize = 0;
        let lastIndex = 0;
        // buffer of size bufferSize max
        let promiseBuffer = [];
        let decodedText;
        let blocks;
        let slice;

        // Continual loop until we meet conditions to break out when we meet EOF
        while (true) {
          const regex = getFileExtension();
          // Grab a chunk of the arrayBuffer to load into memory for regex matching
          slice = arrayBuffer.slice(offset, offset + chunkSize);
          // Decoder will translate array buffer to readable string value
          decodedText = decoder.decode(slice);
          // Track matches per chunk so an unmatched chunk cannot loop forever
          let matchesInChunk = 0;
          // Every loop we match the next regex value in the slice to grab a spectrum
          // With the /g tag on the regex it will match the entire slice, so everytime
          // we execute .exec() it will return a matched block until blocks is null
          // or, we hit our poolSize limit (would rather hit the poolSize limit
          // because the regex is very finicky and will typically not match the
          // entire ion set if it gets cut off). For future dev, if you realize
          // that the number of uploaded spectra doesn't match the file, then make
          // the buffer smaller or increment the chunkSize (try buffer first).
          while (( blocks = regex.exec(decodedText)) !== null ) {
            // Push full match stored in blocks[0] and file name into our promise buffer
            promiseBuffer.push([blocks[0]]);
            count++;
            matchesInChunk++;
            // regex.lastIndex doesn't seem reliable outside the loop so after every iteration save
            // the regex.lastIndex into lastIndex until we break out.
            lastIndex = regex.lastIndex;
            // Since our regex is sophisticated, we need to pull a reduced amount of spectrum per chunk
            // so that we do not partial match spectrum resulting in incorrect uploads. Reads are fast
            // enough that this doesn't cause a big issue.
            if (promiseBuffer.length === bufferSize) {
              break;
            }
          }
          // Now that we broke out, we need to move our offset so, we take a new chunk from
          // the buffer where we last left off in the regex. In order to get identical sizing
          // to the array buffer, we throw a substring of the decodedText into a blob and then
          // call the .size() function to get an appropriate size of our smaller slice.
          foundSize = new Blob([decodedText.substring(0, lastIndex)]).size;
          offset += foundSize;
          // When our offset is the size of the array buffer we reached EOF. An unmatched
          // chunk also ends the read since the offset can no longer advance, which
          // previously caused an infinite loop on content the regex never matched
          if (matchesInChunk === 0 || offset > arrayBuffer.byteLength - 1) {
            await callback(promiseBuffer, file.name);
            break;
          } else{
            await callback(promiseBuffer, file.name);
            promiseBuffer = [];
            blocks = null;
          }
        }
      };

      const processFiles = async () => {
        // Wait for FileReader to return our arrayBuffer
        let arrayBuff;
        await readFileAsync().then((value) => {
          arrayBuff = value;
        }).catch((reason) => {
          return Promise.reject(reason);
        });
        checkFormatMismatch(arrayBuff);
        // Record the full spectra count up front so the progress bar shows the
        // real total instead of only the batches queued so far
        this.totalSpectraCount += this.countSpectraInBuffer(arrayBuff, extension);
        if (extension === 'txt') {
          await arrayBufferToStringTxtFile(arrayBuff);
        } else {
          await arrayBufferToString(arrayBuff);
        }
        this.logger.debug('File Read Complete: Total of ' + count + ' spectra read.');
      };

      await processFiles().then(() => {
        this.uploadProcess.next(false);
      }).catch((reason) => {
        return Promise.reject(reason);
      });
    }


    /**
     *
     * @param data data
     * @param origin origin
     * @returns number returns count
     */
    countData(data, origin) {
        if (typeof origin !== 'undefined') {
            const extension = this.getSupportedExtension(origin);
            if (extension === 'msp') {
                return this.mspParserLibService.countSpectra(data);
            }
            else if (extension === 'mgf') {
                return this.mgfParserLibService.countSpectra(data);
            }
            else if (extension === 'txt') {
                return this.massbankParserLibService.countSpectra(data);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            return this.mspParserLibService.countSpectra(data);
        }
    }

    /**
     *
     * @param data data being processed
     * @param callback helper callback
     * @param origin optional
     */
    processData(data, callback, origin) {
        // Add origin to spectrum metadata before callback
        const addOriginMetadata = (spectrum) => {
            // Null spectra must be forwarded as-is so callers can count parse failures
            // without crashing on the metadata push
            if (typeof spectrum === 'undefined' || spectrum === null) {
              callback(null);
            } else if (typeof origin !== 'undefined') {
              spectrum.meta.push({name: 'origin', value: origin});
              callback(spectrum);
            } else {
              callback(spectrum);
            }
        };
        // Parse data
        if (typeof origin !== 'undefined') {
            const extension = this.getSupportedExtension(origin);
            if (extension === 'msp') {
                this.logger.debug('uploading msp file...');
                this.mspParserLibService.convertFromData(data, addOriginMetadata);
            }
            else if (extension === 'mgf') {
                this.logger.debug('uploading mgf file...');
                this.mgfParserLibService.convertFromData(data, addOriginMetadata);
            }
            else if (extension === 'txt') {
                this.logger.debug('uploading massbank file...');
                this.massbankParserLibService.convertFromData(data, addOriginMetadata);
            }
            else {
                alert('not supported file format!');
            }
        } else {
            this.mspParserLibService.convertFromData(data, addOriginMetadata);
        }
    }

    /**
     * simples uploader
     * @param files spectra upload files
     * @param saveSpectrumCallback helper callback
     * @param wizardData not sure
     */
    uploadSpectraFiles(files, saveSpectrumCallback, wizardData) {
        for (let i = 0; i < files.length; i++) {
            this.loadSpectraFile(files[i], (data, origin) => {
                this.processData(data, (spectrum) => {
                    this.uploadSpectrum(spectrum, saveSpectrumCallback, wizardData);
                }, origin);
            }).finally();
        }
    }

    /**
     * @param spectra object of spectra
     * @param saveSpectrumCallback helper callback
     */
    uploadSpectra(spectra, saveSpectrumCallback) {
        for (let i = 0; i < spectra.length; i++) {
            this.uploadSpectrum(spectra[i], saveSpectrumCallback, {});
        }
    }

    /**
     *
     * @param wizardData passed data
     * @param saveSpectrumCallback helper callback
     * @param additionalData not sure
     */
    uploadSpectrum(wizardData, saveSpectrumCallback, additionalData) {
        this.authenticationService.currentUser.pipe(first()).subscribe((submitter) => {
            this.uploadedSpectraCount += 1;

            this.asyncService.addToPool(() => {
                const myPromise = new Promise((resolve, reject) => {
                    this.workOnSpectra(submitter, saveSpectrumCallback, wizardData, additionalData).then((data) => {
                        this.updateUploadProgress(true);
                        resolve(data);
                    }).catch((error) => {
                        this.logger.error('found an error: ' + error);
                        reject(error);
                        this.updateUploadProgress(false);
                    });
                });
                return myPromise;
            }, undefined);
        });
    }


    /**
     * Checks if spectra are being processed and uploaded
     */
    isUploading() {
        return this.completedSpectraCount + this.failedSpectraCount < this.uploadedSpectraCount;
    }


    /**
     * Marks the start of an interactive upload so it can be recorded as an UploadJob for history
     * once every spectrum in the batch has been attempted
     * @param fileName the source filenames shown in history
     * @param libraryName the library this upload builds, or null
     * @param expectedTotal number of spectra in the batch, used to detect completion reliably
     * @param token bearer token of the submitter
     * @param submitterOverride the library form's optional Submitter override, or null
     */
    trackInteractiveUpload(fileName, libraryName, expectedTotal, token, submitterOverride: any = null) {
        this.interactiveRecord = {
            fileName, libraryName, expectedTotal, token,
            librarySubmitterEmail: submitterOverride ? submitterOverride.emailAddress : null,
            librarySubmitterFirstName: submitterOverride ? submitterOverride.firstName : null,
            librarySubmitterLastName: submitterOverride ? submitterOverride.lastName : null,
            librarySubmitterInstitution: submitterOverride ? submitterOverride.institution : null
        };
        this.saveInteractiveSnapshot();
        // Tracking may be registered after the batch already finished, which the basic uploader
        // does for pasted spectra because the label needs the id from the upload response. In
        // that case no further progress tick will run, so check for completion right away
        this.recordInteractiveUploadIfComplete();
    }

    // Persists the batch's identity and progress so an upload killed by a refresh or close can
    // still be recorded as interrupted on the next visit
    private saveInteractiveSnapshot() {
        localStorage.setItem(this.INTERACTIVE_SNAPSHOT_KEY, JSON.stringify({
            fileName: this.interactiveRecord.fileName,
            libraryName: this.interactiveRecord.libraryName,
            expectedTotal: this.interactiveRecord.expectedTotal,
            persisted: this.completedSpectraCount,
            failed: this.failedSpectraCount,
            librarySubmitterEmail: this.interactiveRecord.librarySubmitterEmail,
            librarySubmitterFirstName: this.interactiveRecord.librarySubmitterFirstName,
            librarySubmitterLastName: this.interactiveRecord.librarySubmitterLastName,
            librarySubmitterInstitution: this.interactiveRecord.librarySubmitterInstitution,
            updatedAt: new Date().getTime()
        }));
    }

    /**
     * Records an interactive upload that died with its page (refresh or close) as a FAILED history
     * entry with the counts it reached. A snapshot is only claimed once it has stopped updating,
     * so a batch still running in another tab is left alone and rechecked later
     */
    private recoverInterruptedUpload() {
        const raw = localStorage.getItem(this.INTERACTIVE_SNAPSHOT_KEY);
        if (raw === null || this.interactiveRecord !== null) {
            return;
        }

        const snapshot = JSON.parse(raw);

        if (new Date().getTime() - snapshot.updatedAt < this.SNAPSHOT_STALE_AFTER) {
            clearTimeout(this.recoveryTimer);
            this.recoveryTimer = setTimeout(() => this.recoverInterruptedUpload(), this.SNAPSHOT_STALE_AFTER);
            return;
        }

        localStorage.removeItem(this.INTERACTIVE_SNAPSHOT_KEY);

        this.uploadJobResource.recordInteractive({
            fileName: snapshot.fileName,
            libraryName: snapshot.libraryName,
            total: snapshot.expectedTotal,
            persisted: snapshot.persisted,
            failed: snapshot.failed,
            librarySubmitterEmail: snapshot.librarySubmitterEmail,
            librarySubmitterFirstName: snapshot.librarySubmitterFirstName,
            librarySubmitterLastName: snapshot.librarySubmitterLastName,
            librarySubmitterInstitution: snapshot.librarySubmitterInstitution,
            status: 'FAILED',
            errorMessage: 'Upload was interrupted, delete and retry'
        }, this.authenticationService.getCurrentUser().accessToken).subscribe(
            () => {
                this.logger.info('recorded an interrupted interactive upload in history');
                this.uploadJobService.notifyJobsChanged();
            },
            (error) => this.logger.error('failed to record interrupted upload: ' + error)
        );
    }

    /**
     * Updates and broadcasts the upload progress
     */
    updateUploadProgress(success) {
        if (typeof success === 'undefined') {
            // do nothing
        } else if (success) {
            this.completedSpectraCount++;
        } else if (!success) {
            this.failedSpectraCount++;
        }

        // Components will be able to subscribe to these variables to get the counts
        this.uploadedSpectraCountSub.next(this.uploadedSpectraCount);
        this.completedSpectraCountSub.next(this.completedSpectraCount);
        this.failedSpectraCountSub.next(this.failedSpectraCount);
        this.uploadProcess.next(this.completedSpectraCount + this.failedSpectraCount < this.uploadedSpectraCount);

        if (this.interactiveRecord !== null) {
            this.saveInteractiveSnapshot();
        }
        this.recordInteractiveUploadIfComplete();
    }

    /**
     * Once every spectrum in a tracked interactive batch has been attempted, records the upload as
     * a COMPLETE UploadJob so it appears in the My Uploads history next to server side uploads.
     * Runs in this singleton service so it survives the uploader component navigating away
     */
    private recordInteractiveUploadIfComplete() {
        if (this.interactiveRecord === null) {
            return;
        }

        const attempted = this.completedSpectraCount + this.failedSpectraCount;
        if (attempted < this.interactiveRecord.expectedTotal) {
            return;
        }

        const record = this.interactiveRecord;
        // Clear first so a late progress tick cannot record the same batch twice. The snapshot
        // goes with it, this batch finished so there is nothing to recover
        this.interactiveRecord = null;
        localStorage.removeItem(this.INTERACTIVE_SNAPSHOT_KEY);

        this.uploadJobResource.recordInteractive({
            fileName: record.fileName,
            libraryName: record.libraryName,
            total: record.expectedTotal,
            persisted: this.completedSpectraCount,
            failed: this.failedSpectraCount,
            librarySubmitterEmail: record.librarySubmitterEmail,
            librarySubmitterFirstName: record.librarySubmitterFirstName,
            librarySubmitterLastName: record.librarySubmitterLastName,
            librarySubmitterInstitution: record.librarySubmitterInstitution
        }, record.token).subscribe(
            () => {
                this.logger.debug('recorded interactive upload in history');
                // The My Uploads page may already be loaded and idle by now, tell it to refetch
                this.uploadJobService.notifyJobsChanged();
            },
            (error) => this.logger.error('failed to record interactive upload: ' + error)
        );
    }
}
