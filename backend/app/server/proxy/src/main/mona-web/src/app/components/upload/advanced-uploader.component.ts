/**
 * Created by sajjan on 4/20/15.
 * Updated by nolanguzman on 10/31/2021
 */
import {AuthenticationService} from '../../services/authentication.service';
import {AdvancedUploadModalComponent} from './advanced-upload-modal.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {FilterPipe} from '../../filters/filter.pipe';
import {ElementRef} from '@angular/core';
import {Location} from '@angular/common';
import {UploadLibraryService} from '../../services/upload/upload-library.service';
import {CtsService} from 'angular-cts-service/dist/cts-lib';
import {TagService} from '../../services/persistence/tag.resource';
import {AsyncService} from '../../services/upload/async.service';
import {NGXLogger} from 'ngx-logger';
import {Component, EventEmitter, OnInit} from '@angular/core';
import {environment} from '../../../environments/environment';
import {Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {
  faSpinner, faExclamationTriangle, faMinusSquare, faPlusSquare,
  faSave, faCloudUploadAlt, faUser, faArrowLeft, faArrowRight,
  faSignInAlt, faFolderOpen, faQuestionCircle, faTrash, faInfoCircle, faFileExport,
} from '@fortawesome/free-solid-svg-icons';
import {first} from 'rxjs/operators';
import {ToasterConfig, ToasterService} from 'angular2-toaster';
import {CompoundConversionService} from '../../services/compound-conversion.service';

@Component({
  selector: 'advanced-uploader',
  templateUrl: '../../views/spectra/upload/advancedUploader.html'
})
export class AdvancedUploaderComponent implements OnInit{
  // Loaded spectra data/status
  spectraLoaded;
  currentSpectrum;
  batchTagList;
  spectra;
  spectrumErrors;
  spectraIndex;
  loadedSpectra;
  totalSpectra;
  spectrum;
  tags;
  showIonTable;
  addSpectra;
  error;
  filenames;
  fileUpload;
  convMolUpload;
  files;
  showLibraryForm;
  compoundProcessing;
  compoundMolError;
  compoundError;

  // library variables
  library = {
    id: null,
    library: null,
    description: null,
    link: null,
    tag: {
      ruleBased: false,
      text: null
    },
    submitter: {
      id: null,
      emailAddress: null,
      firstName: null,
      lastName: null,
      institution: null
    }
  };
  libraryPrefix;
  libraryIDNum;
  /**
   * * Sort order for the ion table - default m/z ascending
   */

  ionTableSort;

  // Parameters provided for trimming spectra
  ionCuts;

  toasterOptions;

  // Icons
  faSpinner = faSpinner;
  faExclamationTriangle = faExclamationTriangle;
  faMinusSquare = faMinusSquare;
  faPlusSquare = faPlusSquare;
  faSave = faSave;
  faCloudUploadAlt = faCloudUploadAlt;
  faUser = faUser;
  faArrowLeft = faArrowLeft;
  faArrowRight = faArrowRight;
  faSignInAlt = faSignInAlt;
  faFolderOpen = faFolderOpen;
  faQuestionCircle = faQuestionCircle;
  faTrash = faTrash;
  faInfoCircle = faInfoCircle;
  faFileExport = faFileExport;

	constructor( public authenticationService: AuthenticationService,  public location: Location,
				          public uploadLibraryService: UploadLibraryService,  public ctsService: CtsService,
				          public tagService: TagService,  public asyncService: AsyncService,  public logger: NGXLogger,
				          public element: ElementRef, public filterPipe: FilterPipe,  public http: HttpClient,
              public router: Router, public modalService: NgbModal, public toaster: ToasterService,
              public compoundConversionService: CompoundConversionService){}

	ngOnInit() {
		this.spectraLoaded = 0;
		this.spectra = [];
		this.spectrumErrors = {};
		this.spectraIndex = 0;
		this.fileUpload = null;
		this.files = null;
		this.convMolUpload = null;
		this.showLibraryForm = false;
		this.libraryPrefix = null;
		this.libraryIDNum = 1;
		this.ionCuts = {};
		this.ionTableSort = '-ion';
    this.batchTagList = [];

		this.addSpectra = new EventEmitter<any>();

		this.addSpectra.subscribe((data) => {
      this.spectra.push(data);
      this.loadedSpectra++;
      this.spectraLoaded = 2;

      // Force update of current spectrum if needed
      this.setSpectrum(this.spectraIndex);
		});

		this.tagService.allTags().subscribe(
			(data) => {
			  if (data.length > 0) {
			    this.tags = data.map(x => x.text);
        } else{
          this.tags = [''];
        }
			},
			(error) => {
				this.logger.error('failed: ' + error);
			}
		);
		this.toasterOptions = new ToasterConfig({
      positionClass: 'toast-top-center',
      timeout: 0,
      showCloseButton: true,
      mouseoverTimerStop: true
    });
	}

  sortIonTable(column) {
    if (column === 'ion') {
      if (this.ionTableSort === '+ion') { this.ionTableSort = '-ion'; } else { this.ionTableSort = '+ion'; }
    }
    else if (column === 'intensity') {
      if (this.ionTableSort === '+intensity') { this.ionTableSort = '-intensity'; } else { this.ionTableSort = '+intensity'; }
    }
    else if (column === 'annotation') {
      if (this.ionTableSort === '+annotation') { this.ionTableSort = '-annotation'; } else { this.ionTableSort = '+annotation'; }
    }
  }


	/**
	 * Perform mass spectrum trimming
	 */

	performIonCuts(index) {
		if (typeof index === 'undefined') {
			index = this.spectraIndex;
		}

		const cutIons = [];
		const retainedIons = [];

		let limit = 0;

		if (typeof this.ionCuts.absAbundance !== 'undefined') {
			limit = this.ionCuts.absAbundance;
		}

		if (typeof this.ionCuts.basePeak !== 'undefined') {
			const basePeakCut = this.ionCuts.basePeak * this.spectra[index].basePeak / 100;
			limit = basePeakCut > limit ? basePeakCut : limit;
		}

		for (let i = 0; i < this.spectra[index].ions.length; i++) {
			if (this.spectra[index].ions[i].intensity < limit) {
				this.spectra[index].ions[i].selected = false;
				cutIons.push(i);
			} else {
				retainedIons.push(i);
			}
		}

		if (typeof this.ionCuts.nIons !== 'undefined' && retainedIons.length > this.ionCuts.nIons) {
			retainedIons.sort( (a, b) => {
				return this.spectra[index].ions[b].intensity - this.spectra[index].ions[a].intensity;
			});

			for (let i = this.ionCuts.nIons; i < retainedIons.length; i++) {
				this.spectra[index].ions[retainedIons[i]].selected = false;
			}
		}
		this.toaster.pop({
      type: 'success',
      title: 'Ion Cuts Performed on Spectrum',
      body: `Ion cuts were performed successfully on spectrum, see Ion Table for cuts.`
    });
	}

	performAllIonCuts() {
		for (let i = 0; i < this.spectra.length; i++) {
			this.performIonCuts(i);
		}
	}

	resetIonCuts() {
	  for (const i of this.currentSpectrum.ions) {
	    i.selected = true;
    }
	  this.toaster.pop({
      type: 'success',
      title: 'Ion Cuts Reset',
      body: `Ion cuts were successfully reset on spectrum.`
    });
	}


	/**
	 * Add a new name to the list
	 */

	addName() {
		if (this.currentSpectrum.names[this.currentSpectrum.names.length - 1] !== '') {
			this.currentSpectrum.names.push('');
		}
	}

  removeName(index: number) {
    this.currentSpectrum.names.splice(index, 1);
  }

  trackByIndex(index: number, item: any) {
    return index;
  }


  /**
	 * Handle metadata functionality
	 */
	addMetadataField() {
		this.currentSpectrum.meta.push({name: '', value: ''});
		this.element.nativeElement.getElementById('metadata_editor').scrollTop = 0;
	}

	removeMetadataField(index) {
		this.spectra[this.spectraIndex].meta.splice(index, 1);
	}

	applyMetadataToAll(index) {
		const metadata = this.currentSpectrum.meta[index];

		for (let i = 0; i < this.spectra.length; i++) {
			if (i !== this.spectraIndex) {
				this.spectra[i].meta.push(metadata);
			}
		}
	}

	applyTagsToAll() {
		const tags = this.currentSpectrum.tags;

		for (let i = 0; i < this.spectra.length; i++) {
			if (i !== this.spectraIndex) {
				if (!this.spectra[i].tags) {
					this.spectra[i].tags = [];
				}

				for (let j = 0; j < tags.length; j++) {
					let hasTag = false;

					for (let k = 0; k < this.spectra[i].tags.length; k++) {
						if (this.spectra[i].tags[k].text === tags[j].text) {
							hasTag = true;
							break;
						}
					}

					if (!hasTag) {
						this.spectra[i].tags.push(tags[j]);
					}
				}
			}
		}
	}

	setFiles(event) {
	  if (event.target.files.length > 0) {
	    this.files = event.target.files;
    } else {
	    this.files = undefined;
	    this.fileUpload = null;
    }
  }

  getFileOriginName(s): string {
	  return s.hiddenMetadata.find((e) => e.name === 'origin').value;
  }

	batchProcessSTP(data, origin): Promise<any> {
	  return new Promise((resolve, reject) => {
      this.uploadLibraryService.processData(data, (spectrum) => {
        if (spectrum === null) {
          reject(true);
        } else {
          if (this.showLibraryForm) {
            spectrum.id = `${this.libraryPrefix}${String(this.libraryIDNum).padStart(6, '0')}`;
            this.libraryIDNum += 1;
            if (this.library.link === null) {
              this.library.link = 'http://massbank.us';
            }
            spectrum.library = this.library;
            spectrum.tags = [];
            if (this.batchTagList.length > 0) {
              for (const tag of this.batchTagList) {
                spectrum.tags.push({ruleBased: false, text: tag.text});
              }
            }
            spectrum.tags.push(this.library.tag);
            if (this.library.submitter.emailAddress !== null) {
              this.library.submitter.id = this.library.submitter.emailAddress;
              spectrum.submitter = this.library.submitter;
            }
          }
          this.uploadLibraryService.uploadSpectra([spectrum],   (res) => {
            try {
              this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra`, res,
                {
                  headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${this.authenticationService.getCurrentUser().accessToken}`
                  }
                })
                .pipe(first())
                .subscribe((r: any) => {
                  // If no errors, just resolve the promise and return true
                  resolve(true);
                },
                (err) => {
                  this.logger.info('ERROR');
                  this.logger.info(err);
                  reject(err);
                });
            } catch (error) {
              reject(error);
            }
          });
        }
      }, origin);
    });
  }

  batchProcess(data, origin): Promise<any> {
    return new Promise((resolve, reject) => {
      this.uploadLibraryService.processData(data, (spectrum) => {
        if (typeof spectrum !== 'undefined' && spectrum !== null) {
          if (this.showLibraryForm) {
            spectrum.id = `${this.libraryPrefix}${String(this.libraryIDNum).padStart(6, '0')}`;
            this.libraryIDNum += 1;
            if (this.library.link === null) {
              this.library.link = 'http://massbank.us';
            }
            spectrum.library = this.library;
            spectrum.tags = [];
            if (this.batchTagList.length > 0) {
              for (const tag of this.batchTagList) {
                spectrum.tags.push({ruleBased: false, text: tag.text});
              }
            }
            spectrum.tags.push(this.library.tag);
            if (this.library.submitter.emailAddress !== null) {
              this.library.submitter.id = this.library.submitter.emailAddress;
              spectrum.submitter = this.library.submitter;
            }
          }
          this.asyncService.addToPool(async () => {
            // Create list of ions
            spectrum.basePeak = 0;
            spectrum.ions = spectrum.spectrum.split(' ').map((x) => {
              x = x.split(':');
              let annotation = '';

              for (let y = 0; y < spectrum.meta.length; y++) {
                if (spectrum.meta[y].category === 'annotation' && spectrum.meta[y].value === y[0]) {
                  annotation = spectrum.meta[y].name;
                }
              }

              const intensity = parseFloat(x[1]);

              if (intensity > spectrum.basePeak) {
                spectrum.basePeak = intensity;
              }

              return {
                ion: parseFloat(x[0]),
                intensity,
                annotation,
                selected: true
              };
            });

            // Get structure from InChIKey if no InChI is provided
            if (typeof spectrum.inchiKey !== 'undefined' && typeof spectrum.inchi === 'undefined') {
              this.ctsService.convertInchiKeyToMol(spectrum.inchiKey, (molecule) => {
                if (molecule !== null) {
                  spectrum.molFile = molecule;
                }
              }, undefined);
            }

            // Remove annotations and origin from metadata
            spectrum.hiddenMetadata = spectrum.meta.filter((metadata) => {
              return metadata.name === 'origin' || (typeof metadata.category !== 'undefined' && metadata.category === 'annotation');
            });

            spectrum.meta = spectrum.meta.filter((metadata) => {
              return metadata.name !== 'origin' && (typeof metadata.category === 'undefined' || metadata.category !== 'annotation');
            });

            // Add an empty metadata field if none exist
            if (spectrum.meta.length === 0) {
              spectrum.meta.push({name: '', value: ''});
            }
            await this.addSpectra.emit(spectrum);
            resolve(true);
          }, undefined);
          resolve(true);
        } else {
          reject();
        }

      }, origin);
    });
  }

	straightThroughProcessing() {
	  let promiseBuffer = [];
	  // Move to the upload status page then execute the upload process
   this.router.navigate(['/upload/status']).then(() => {
     // set timeout for 1 second, so we can navigate to upload status page first
     setTimeout( () => {
       for (const file of this.files) {
         this.uploadLibraryService.loadSpectraFile(file, async (data, origin) => {
           // Receive async batch from loadSpectraFile
           for (const item of data) {
             // Create an array of promises that will process the files and then upload to server
             promiseBuffer.push(this.batchProcessSTP(item, origin));
           }
           // Execute batch of promises at once but await so that the batch finishes first before moving to new batch
           // otherwise we may overload the browser and crash it. May experiment with this.
           await Promise.all(promiseBuffer.map(p => p.catch((reason) => {
             return Promise.reject(reason);
           })));
           // Reset our array so we can go again.
           promiseBuffer = [];
         }).then().catch((reason) => {
           this.router.navigate(['/upload/advanced']).then();
           setTimeout(() => {
             this.toaster.pop({
               type: 'error',
               title: 'Error Occurred While Parsing File',
               body: reason
             });
           }, 500);
         });
         }
       }, 1000);
   });
  }

	/**
	 * Parse spectra
	 * @param event Contains an event which serves the files
	 */
	parseFiles() {
	  this.uploadLibraryService.completedSpectraCount = 0;
	  this.uploadLibraryService.failedSpectraCount = 0;
	  this.uploadLibraryService.uploadedSpectraCount = 0;
	  this.libraryIDNum = 1;
	  let promiseBuffer = [];
	  let totalSize = 0;

	  if (this.files && this.files.length) {
      for (let y = 0; y < this.files.length; y++) {
        totalSize += this.files[y].size;
      }
      // If the file is larger than 10MB then use straight through processing
      if (totalSize > 10 * 1024 * 1024) {
        const modalRef = this.modalService.open(AdvancedUploadModalComponent);
        modalRef.result.then((res) => {
          if (res) {
            this.uploadLibraryService.isSTP = true;
            this.straightThroughProcessing();
          }
        });
        return;
      } else {
        this.uploadLibraryService.isSTP = false;
        for (let i = 0; i < this.files.length; i++) {
          this.uploadLibraryService.loadSpectraFile(this.files[i],
            async (data, origin) => {
              for (const item of data) {
                promiseBuffer.push(this.batchProcess(item, origin));
              }
              await Promise.all(promiseBuffer.map(p => p.catch((reason) => {
                return Promise.reject(reason);
              })));
              promiseBuffer = [];
            }).catch((reason) => {
            setTimeout(() => {
              this.toaster.pop({
                type: 'error',
                title: 'Error Occurred While Parsing File',
                body: reason
              });
            }, 500);
          });
        }
      }
    }
	}


	/**
	 * Handle MOL file input
	 */
	parseMolFile(event) {
		if (event.target.files.length > 0) {
			const fileReader = new FileReader();

			fileReader.onload = (e) => {
				let data = e.target.result as string;

				// Accept only the first MOL file
				let sep1 = data.indexOf('$$$$');
				const sep2 = data.indexOf('M  END');

				if (sep1 > -1 || sep2 > -1) {
					if (sep1 === -1 || (sep1 > -1 && sep2 > -1 && sep1 > sep2)) {
						sep1 = sep2;
					}

					data = data.substring(0, sep1);
				}

				this.currentSpectrum.molFile = data;
        this.convertMolToInChI();
			};

			fileReader.readAsText(event.target.files[0]);
		}
	}

  // Was not working anymore 8/29/2025
	// convertMolToInChI() {
	// 	if (typeof this.currentSpectrum.molFile !== 'undefined' && this.currentSpectrum.molFile !== '') {
	// 	  this.ctsService.convertToInchiKey(this.currentSpectrum.molFile, (result) => {
	// 			this.currentSpectrum.inchiKey = result.inchikey;
	// 		}, undefined);
	// 	}
	// }

  /**
   * Pull names from CTS given an InChIKey and update the currentSpectrum
   */
  pullNames(inchiKey) {
    // Only pull if there are no names provided
    if (this.currentSpectrum.names.length === 0 || (this.currentSpectrum.names.length === 1 && this.currentSpectrum.names[0] === '')) {
      this.compoundConversionService.InChIKeyToName(
        inchiKey,
        (data) => {
          this.currentSpectrum.names = this.currentSpectrum.names.filter((x) => {
            return x !== '';
          });

          Array.prototype.push.apply(this.currentSpectrum.names, data);

          this.compoundProcessing = false;
        },
        () => {
          this.compoundProcessing = false;
        }
      );
    } else {
      this.compoundProcessing = false;
    }
  }


  convertMolToInChI() {
    if (typeof this.currentSpectrum.molFile !== 'undefined' && this.currentSpectrum.molFile !== '') {
      this.compoundProcessing = false;

      this.compoundConversionService.parseMOL(
        this.currentSpectrum.molFile,
        (response) => {
          this.currentSpectrum.inchi = response.inchi;
          this.currentSpectrum.smiles = response.smiles;
          this.currentSpectrum.inchiKey = response.inchiKey;

          this.pullNames(response.inchiKey);
        },
        (response) => {
          this.compoundMolError = 'Unable to process provided MOL data!';
          this.compoundProcessing = false;
        }
      );
    } else {
      this.compoundMolError = '';
    }
  }


  /**
   * Pull compound summary given an InChI
   */
  processInChI(inchi) {
    this.compoundConversionService.parseInChI(
      this.currentSpectrum.inchi,
      (response) => {
        this.logger.debug('Parse Inchi response: ' + response);
        this.currentSpectrum.smiles = response.smiles;
        this.currentSpectrum.inchiKey = response.inchiKey;
        this.currentSpectrum.molFile = response.molData;

        this.pullNames(response.inchiKey);
      },
      (response) => {
        this.compoundError = 'Unable to process provided InChI!';
        this.compoundProcessing = false;
      }
    );
  }

  /**
   * Pull compound summary given an InChIKey
   */
  processInChIKey(inchiKey) {
    this.compoundConversionService.getInChIByInChIKey(
      inchiKey,
      (data) => {
        this.logger.info('InChi By InchiKey Reponse: ' + data);
        this.currentSpectrum.inchi = data[0];
        this.processInChI(data[0]);
      },
      (response) => {
        if (response.status === 200) {
          this.compoundError = 'No results found for provided InChIKey!';
        } else {
          this.compoundError = 'Unable to process provided InChIKey!';
        }

        this.compoundProcessing = false;
      }
    );
  }

  /**
   * Convert an array of names to an InChIKey based on the first result
   * @param names array of compound names
   * @param callback callback function to get name
   */
  namesToInChIKey(names, callback) {
    if (names.length === 0) {
      callback(null);
    } else {
      this.compoundConversionService.nameToInChIKey(names[0], (molecule) => {
        if (molecule !== null) {
          callback(molecule);
        } else {
          this.namesToInChIKey(names.slice(1), callback);
        }
      }, (error) => {
        this.namesToInChIKey(names.slice(1), callback);
      });
    }
  }

  /**
   * Generate MOL file from available compound information
   */
  retrieveCompoundData() {
    this.logger.info('Retrieving MOL data...');

    this.compoundError = undefined;
    this.compoundProcessing = true;


    // Process InChI
    if (this.currentSpectrum.inchi) {
      this.processInChI(this.currentSpectrum.inchi);
    }

    // Process SMILES
    else if (this.currentSpectrum.smiles) {
      this.compoundConversionService.parseSMILES(
        this.currentSpectrum.smiles,
        (response) => {
          this.logger.debug('Parse smiles response ' + response);
          this.currentSpectrum.inchi = response.inchi;
          this.currentSpectrum.inchiKey = response.inchiKey;
          this.currentSpectrum.molFile = response.molData;

          this.pullNames(response.inchiKey);
        },
        (response) => {
          this.compoundError = 'Unable to process provided SMILES!';
          this.compoundProcessing = false;
        }
      );
    }

    // Process InChIKey
    else if (this.currentSpectrum.inchiKey) {
      this.processInChIKey(this.currentSpectrum.inchiKey);
    }

    // Process names
    else if (this.currentSpectrum.names.length > 0) {
      this.namesToInChIKey(this.currentSpectrum.names, (inchiKey) => {
        this.logger.debug('Name to inchikey response: ' + inchiKey);
        if (inchiKey !== null) {
          this.logger.info('Found InChIKey: ' + inchiKey);
          this.currentSpectrum.inchiKey = inchiKey;
          this.processInChIKey(inchiKey);
        } else {
          this.compoundError = 'Unable to find a match for provided name!';
          this.compoundProcessing = false;
        }
      });
    }

    else {
      this.compoundError = 'Please provide compound details';
      this.compoundProcessing = false;
    }
  }

  resetCompound() {
    this.currentSpectrum.molFile = '';
    this.currentSpectrum.inchi = '';
    this.currentSpectrum.inchiKey = '';
    this.currentSpectrum.smiles = '';
    this.currentSpectrum.names = [''];
  }


  /**
   * Export a file as MSP
   */
	exportFile() {
		let msp = '';

		for (let i = 0; i < this.spectra.length; i++) {
			// Add separator
			if (i > 0) {
				msp += '\n\n';
			}

			// Add names
			msp += 'Name: ' + (this.spectra[i].names.length === 0 ? 'Unknown' : this.spectra[i].names[0]) + '\n';

			if (typeof this.spectra[i].inchiKey !== 'undefined' && this.spectra[i].inchiKey !== '') {
				msp += 'InChIKey: ' + this.spectra[i].inchiKey + '\n';
			}

			if (typeof this.spectra[i].inchi !== 'undefined' && this.spectra[i].inchi !== '') {
				msp += 'InChI: ' + this.spectra[i].inchi + '\n';
			}

			if (typeof this.spectra[i].smiles !== 'undefined' && this.spectra[i].smiles !== '') {
				msp += 'SMILES: ' + this.spectra[i].smiles + '\n';
			}

			for (let j = 1; j < this.spectra[i].names.length; j++) {
				msp += 'Synon: ' + this.spectra[i].names[j] + '\n';
			}

			// Add metadata
			for (let j = 0; j < this.spectra[i].meta.length; j++) {
				msp += this.spectra[i].meta[j].name + ': ' + this.spectra[i].meta[j].value + '\n';
			}

			// Add mass spectrum
			const ions = [];

			for (let j = 0; j < this.spectra[i].ions.length; j++) {
				if (this.spectra[i].ions[j].selected) {
					ions.push(this.spectra[i].ions[j]);
				}
			}

			ions.sort((a, b) => {
				return a[0] - b[0];
			});

			msp += 'Num Peaks: ' + ions.length + '\n';

			for (let j = 0; j < ions.length; j++) {
				msp += ions[j].ion + ' ' + ions[j].intensity + (ions[j].annotation !== '' ? ' ' + ions[j].annotation : '') + '\n';
			}
		}

		// Export file
		// http://stackoverflow.com/a/18197341/406772
		const pom = this.element.nativeElement.createElement('a');
		pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(msp));
		pom.setAttribute('download', 'export.msp');
		pom.style.display = 'none';

		this.element.nativeElement.body.appendChild(pom);
		pom.click();
		this.element.nativeElement.body.removeChild(pom);
	}


	/**
	 *
	 */
	waitForLogin() {
		this.authenticationService.isAuthenticated.subscribe((authenticate) => {
			if (authenticate) {
				if (this.spectraLoaded === 2) {
					this.uploadFile();
				}
			}
		});
	}


	/**
	 * Upload current data
	 */
	validateSpectra() {
		const invalid = [];

		for (let i = 0; i < this.spectra.length; i++) {
			this.spectra[i].errors = [];

			let ionCount = 0;

			for (let j = 0; j < this.spectra[i].ions.length; j++) {
				if (this.spectra[i].ions[j].selected) {
					ionCount++;
				}
			}

			if (ionCount === 0) {
				this.spectra[i].errors.push('This spectrum has no selected ions!  It cannot be uploaded.');
			}

			if ((typeof this.spectra[i].inchi === 'undefined' || this.spectra[i].inchi === '') &&
				(typeof this.spectra[i].molFile === 'undefined' || this.spectra[i].molFile === '') &&
				(typeof this.spectra[i].smiles === 'undefined' || this.spectra[i].smiles === '')) {
				this.spectra[i].errors.push('This spectrum requires a structure in order to upload. Please provide a MOL file or InChI code!');
			}


			if (this.spectra[i].errors.length > 0) {
				invalid.push(i);
			}
		}

		if (invalid.length > 0) {
			this.setSpectrum(invalid[0]);
			this.error = 'There are some errors in the data you have provided.  The';
			window.scrollTo(0, 0);
		}

		return true;
	}


	uploadFile() {
		if (this.validateSpectra()) {
			// Reset the spectrum count if necessary
			if (!this.uploadLibraryService.isUploading()) {
				this.uploadLibraryService.completedSpectraCount = 0;
				this.uploadLibraryService.failedSpectraCount = 0;
				this.uploadLibraryService.uploadedSpectraCount = 0;
				this.uploadLibraryService.uploadStartTime = new Date().getTime();
			}

			// Re-add origin and annotations to metadata:
			for (let i = 0; i < this.spectra.length; i++) {
				 this.spectra[i].meta.push.apply(this.spectra[i].meta, this.spectra[i].hiddenMetadata);
			}
			this.uploadLibraryService.uploadSpectra(this.spectra,  (spectrum) => {
				this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra`, spectrum,
					{headers: {
							'Content-Type': 'application/json',
							Authorization: `Bearer ${this.authenticationService.getCurrentUser().accessToken}`
					}}).pipe(
            first()
        ).subscribe((data: any) => {
					  this.logger.debug('Spectra was uploaded');
					  if (!this.uploadLibraryService.isSTP) {
              this.uploadLibraryService.uploadedSpectra.push(data.id);
            }
					},
					 (err) => {
						this.logger.info(err);
					});
			});
			this.router.navigate(['/upload/status']).then();
		}
	}

	isLoadingSpectra() {
		return this.asyncService.hasPooledTasks();
	}

	/**
	 * provides us with an overview of all our tags
	 * @param query: Contains query from tags selected
	 * @returns observable: filters the tags and pushes to an observable
	 * Performs initialization and acquisition of data used by the wizard
	 */
	loadTags() {
	  // this.tagService.query().pipe(map(data => data.map(x => x.text)));
	  return this.tags.map(x => x.text);
	}

	/*
		 * Handle switching between spectra
		 */
	setSpectrum(index) {
		this.spectraIndex = index;
		this.currentSpectrum = this.spectra[this.spectraIndex];
		this.showIonTable = this.currentSpectrum.ions.length < 500;
	}

	previousSpectrum() {
		this.setSpectrum((this.spectraIndex + this.spectra.length - 1) % this.spectra.length);
	}

	nextSpectrum() {
		this.setSpectrum((this.spectraIndex + this.spectra.length + 1) % this.spectra.length);
	}

	removeCurrentSpectrum() {
		this.spectra.splice(this.spectraIndex, 1);

		if (this.spectra.length === 0) {
			this.resetFile();
		} else if (this.spectraIndex === this.spectra.length) {
			this.setSpectrum(this.spectraIndex - 1);
		} else {
			this.setSpectrum(this.spectraIndex);
		}
		this.toaster.pop({
      type: 'success',
      title: 'Removed Spectrum',
      body: `Spectrum at index: ${this.spectraIndex + 1} has been removed.`
    });
	}

	resetFile() {
		this.spectraLoaded = 0;
		this.spectraIndex = 0;
		this.spectra = [];

		this.filenames = null;
		this.fileUpload = null;

		// Clear pool
		this.asyncService.resetPool();

		// Scroll to top of the page
		window.scrollTo(0, 0);
	}

	goToDocumentation() {
	  this.router.navigate(['/documentation/uploadLibrary']).then();
  }

}
