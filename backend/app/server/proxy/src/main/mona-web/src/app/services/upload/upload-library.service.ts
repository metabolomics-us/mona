/**
 * Created by wohlgemuth on 6/25/14.
 *
 * handles the upload of library spectra to the system
 */

import {NGXLogger} from 'ngx-logger';
import {Spectrum} from '../persistence/spectrum.resource';
import {MspParserLibService} from 'angular-msp-parser/dist/msp-parser-lib';
import {MgfParserLibService} from 'angular-mgf-parser/dist/mgf-parser-lib';
import {ChemifyService} from 'angular-cts-service/dist/cts-lib';
import {CtsService} from 'angular-cts-service/dist/cts-lib';
import {AuthenticationService} from '../authentication.service';
import {MassbankParserLibService} from 'angular-massbank-parser/dist/massbank-parser-lib';
import {HttpClient} from '@angular/common/http';
import {AsyncService} from './async.service';
import {MetadataOptimization} from '../optimization/metadata-optimization.service';
import { Subject } from 'rxjs';
import {Injectable} from '@angular/core';
import {first} from 'rxjs/operators';

@Injectable()
export class UploadLibraryService{
    completedSpectraCountSub = new Subject<number>();
    failedSpectraCountSub = new Subject<number>();
    uploadedSpectraCountSub = new Subject<number>();
    uploadProcess = new Subject<boolean>();

    completedSpectraCount;
    failedSpectraCount;
    uploadedSpectraCount;
    uploadedSpectra;

    uploadStartTime;
    isSTP;
    uploadComplete;

    constructor(public logger: NGXLogger,
                public mspParserLibService: MspParserLibService,
                public mgfParserLibService: MgfParserLibService,
                public chemifyService: ChemifyService,
                public ctsService: CtsService,
                public authenticationService: AuthenticationService,
                public massbankParserLibService: MassbankParserLibService,
                public http: HttpClient,
                public asyncService: AsyncService,
                public metadataOptimization: MetadataOptimization){
        this.completedSpectraCount = 0;
        this.failedSpectraCount = 0;
        this.uploadedSpectraCount = 0;
        this.uploadStartTime = -1;
        this.uploadProcess.next(true);
        this.isSTP = false;
        this.uploadedSpectra = [];
    }

    /**
     * obtains a promise for us to get to the an inchi key for a spectra object
     * @param spectra type object
     * @returns observable to subscribe to
     */
     obtainKey(spectra): Promise<any> {
        /**
         * helper function to resolve the correct inchi by name
         * @param spectra type object
         */
        const resolveByName = (spec, resolve, reject) => {
            if (spec.name) {
                this.chemifyService.nameToInChIKey(spec.name, (key) => {
                    if (key === null) {
                        reject('sorry no InChI Key found for ' + spec.name + ', at name to InChI key!');
                    }
                    else {
                        spec.inchiKey = key;
                        resolve(spec);
                    }
                }, undefined);
            }

            // if we have a bunch of names
            else if (spec.names && spec.names.length > 0) {

                this.chemifyService.nameToInChIKey(spec.names[0], (key) => {
                    if (key === null) {
                        reject('sorry no InChI Key found for ' + spec.names[0] + ', at names to InChI key!');
                    }
                    else {
                        spec.inchiKey = key;
                        resolve(spec);
                    }
                }, undefined);
            }

            // we got nothing so we give up
            else {
                reject('sorry given object was invalid, we need a name, or an InChI code, InChI Key or an array with names for this to work!');
            }
        };

        const myPromise = new Promise((resolve, reject) => {
            if (spectra.inchi) {
                // no work needed
                resolve(spectra);
            }
            // in case we got a smiles
            else if (spectra.smiles) {
                this.ctsService.convertSmileToInChICode(spectra.smiles, (data) => {
                    spectra.inchi = data.inchicode;
                    spectra.inchiKey = data.inchikey;

                    resolve(spectra);
                }, undefined);
            }

            // in case we got an inchi
            else if (spectra.inchiKey) {
                this.ctsService.convertInchiKeyToMol(spectra.inchiKey, (molecule) => {
                    if (molecule === null && spectra.inchi === null) {
                        resolveByName(spectra, resolve, reject);
                    }
                    else {
                        if (molecule !== null) {
                            spectra.molFile = molecule;
                        }
                        resolve(spectra);
                    }
                }, undefined);
            }

            else {
                resolveByName(spectra, resolve, reject);
            }
        });

        return myPromise;
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
                    // only if we have an inchi or a molfile we can submit this file
                    if (spectrumWithKey.inchi !== null || spectrumWithKey.molFile !== null) {
                        this.submitSpectrum(spectrumWithKey, submitter, saveSpectrumCallback, additionalData).then((submittedSpectra) => {
                            resolve(submittedSpectra);
                        });
                    }

                    else {
                        this.logger.error('invalid ' + JSON.stringify(spectrumWithKey));
                        reject(new Error('dropped object from submission, since it was declared invalid, it had neither an InChI or a Molfile, which means the provide InChI key most likely was not found!'));
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
                if (spectra.id) {
                  s.id = spectra.id;
                }
                // assign structure information
                if (spectra.inchiKey !== null) {
                  s.biologicalCompound.inchiKey = spectra.inchiKey;
                }
                if (spectra.inchi !== null) {
                  s.biologicalCompound.inchi = spectra.inchi;
                }

                if (typeof spectra.molFile !== 'undefined' && spectra.molFile !== null) {
                    s.biologicalCompound.molFile = spectra.molFile.toString();
                }

                // assign all the defined names of the spectra
                s.biologicalCompound.names = [];

                if (typeof spectra.name !== 'undefined') {
                    if (s.spectrum.name !== '') {
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

                s.biologicalCompound.metaData = [];
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
            compound: undefined,
            comments: [],
            submitter: ''
        };
        return spectrum;
    }


    /**
     * Loads spectra file and returns the data to a callback function
     * @param file filename
     * @param callback helper callback
     * @param fireUploadProgress upload progress
     */
    async loadSpectraFile(file, callback): Promise<any> {
      let count = 0;
      // In order to process data efficiently and in a smaller footprint, the file needs to be sliced into smaller batches
      // that are individually matched by regex pattern.
      const getFileExtension = () => {
        if (file.name.toLowerCase().indexOf('.msp') > 0) {
          return new RegExp(/((?:.*:\s*[^\n]*\n?)+)\n((?:\s*[0-9]*\.?[0-9]+\s+[0-9]*\.?[0-9]+[;\n]?.*\n?)*)/g);
        }
        else if (file.name.toLowerCase().indexOf('.mgf') > 0) {
          return new RegExp(/BEGIN IONS([\s\S]*?)END IONS/g);
        }
        else if (file.name.toLowerCase().indexOf('.txt') > 0) {
          return new RegExp(/.*/g);
        }
      };

      const readFileAsync = () => {
        // Create promise that will resolve with an array buffer of the file
        // we use an array buffer because anything else will crash due to memory
        // constraints in the browser i.e. readAsText() function
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => resolve(reader.result);
          reader.onerror = () => reject;
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
        // Start with 2MB by default
        const chunkSize = 2 * 1024 * 1024;
        // Buffer only 200 spectrum at a time
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
          // Every loop we match the next regex value in the slice to grab a spectrum
          // With the /g tag on the regex it will match the entire slice, so everytime
          // we execute .exec() it will return a matched block until blocks is null
          // or we hit our poolSize limit.
          while (( blocks = regex.exec(decodedText)) !== null ) {
            // Push full match stored in blocks[0] and file name into our promise buffer
            promiseBuffer.push([blocks[0]]);
            count++;
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
          // Now that we broke out, we need to move our offset so we take a new chunk from
          // the buffer where we last left off in the regex. In order to get identical sizing
          // to the array buffer, we throw a substring of the decodedText into a blob and then
          // call the .size() function to get an appropriate size of our smaller slice.
          foundSize = new Blob([decodedText.substring(0, lastIndex)]).size;
          offset += foundSize;
          // When our offset is the size of the array buffer, then we reached EOF so send
          // the last promiseBuffer and break out.
          if (offset > arrayBuffer.byteLength - 1) {
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
        const arrayBuff = await readFileAsync();
        if (file.name.toLowerCase().indexOf('.txt') > 0) {
          await arrayBufferToStringTxtFile(arrayBuff);
        } else {
          await arrayBufferToString(arrayBuff);
        }
        this.logger.debug('File Read Complete: Total of ' + count + ' spectra read.');
      };

      await processFiles().then(() => {
        // Once we finished our read, set isSTP to false so the spectra upload progress bar shows completed.
        this.isSTP = false;
        this.uploadProcess.next(false);
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
            if (origin.toLowerCase().indexOf('.msp') > 0) {
                return this.mspParserLibService.countSpectra(data);
            }
            else if (origin.toLowerCase().indexOf('.mgf') > 0) {
                return this.mgfParserLibService.countSpectra(data);
            }
            else if (origin.toLowerCase().indexOf('.txt') > 0) {
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
            if (typeof origin !== 'undefined') {
                spectrum.meta.push({name: 'origin', value: origin});
            }
            callback(spectrum);
        };
        // Parse data
        if (typeof origin !== 'undefined') {
            if (origin.toLowerCase().indexOf('.msp') > 0) {
                // this.logger.info('uploading msp file...');
                this.mspParserLibService.convertFromData(data, addOriginMetadata);
            }
            else if (origin.toLowerCase().indexOf('.mgf') > 0) {
                // this.logger.info('uploading mgf file...');
                this.mgfParserLibService.convertFromData(data, addOriginMetadata);
            }
            else if (origin.toLowerCase().indexOf('.txt') > 0) {
                // this.logger.info('uploading massbank file...');
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
    }
}
