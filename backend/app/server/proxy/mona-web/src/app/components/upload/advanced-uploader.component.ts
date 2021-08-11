/**
 * Created by sajjan on 4/20/15.
 */
import {AuthenticationService} from '../../services/authentication.service';
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
import {Observable} from 'rxjs';
import {faSpinner, faExclamationTriangle, faMinusSquare, faPlusSquare, faSave} from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'advanced-uploader',
  templateUrl: '../../views/spectra/upload/advancedUploader.html'
})
export class AdvancedUploaderComponent implements OnInit{
  // Loaded spectra data/status
  public spectraLoaded;
  public currentSpectrum;
  public spectra;
  public spectrumErrors;
  public spectraIndex;
  public loadedSpectra;
  public totalSpectra;
  public spectrum;
  public tags;
  public showIonTable;
  public addSpectra;
  public error;
  public filenames;
  public fileUpload;
  public convMolUpload;
  /**
   * * Sort order for the ion table - default m/z ascending
   */

  public ionTableSort;

  // Parameters provided for trimming spectra
  public ionCuts;

  // Icons
  faSpinner = faSpinner;
  faExclamationTriangle = faExclamationTriangle;
  faMinusSquare = faMinusSquare;
  faPlusSquare = faPlusSquare;
  faSave = faSave;

	constructor( public authenticationService: AuthenticationService,  public location: Location,
				          public uploadLibraryService: UploadLibraryService,  public ctsService: CtsService,
				          public tagService: TagService,  public asyncService: AsyncService,  public logger: NGXLogger,
				          public element: ElementRef, public filterPipe: FilterPipe,  public http: HttpClient,
              public router: Router){}

	ngOnInit(): void {
		this.spectraLoaded = 0;
		this.spectra = [];
		this.spectrumErrors = {};
		this.spectraIndex = 0;
		this.ionCuts = {
		};
		this.ionTableSort = '-ion';

		this.addSpectra = new EventEmitter<any>();

		this.addSpectra.subscribe((data) => {
			this.spectra.push(data);
			this.loadedSpectra++;
			this.spectraLoaded = 2;

			// Force update of current spectrum if needed
			this.setSpectrum(this.spectraIndex);
		});

		this.tagService.query().then(
			(data) => {
				this.tags = data;
			},
			(error) => {
				this.logger.error('failed: ' + error);
			}
		);
	}

  sortIonTable = (column) => {
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

	performIonCuts =  (index) => {
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
	}

	performAllIonCuts =  () => {
		for (let i = 0; i < this.spectra.length; i++) {
			this.performIonCuts(i);
		}
	}

	resetIonCuts =  () => {
	  for (const i of this.currentSpectrum.ions) {
	    i.selected = true;
    }
	}


	/**
	 * Add a new name to the list
	 */

	addName = () => {
		if (this.currentSpectrum.names[this.currentSpectrum.names.length - 1] !== '') {
			this.currentSpectrum.names.push('');
		}
	}


	/**
	 * Handle metadata functionality
	 */

	addMetadataField = () => {
		this.currentSpectrum.meta.push({name: '', value: ''});
		this.element.nativeElement.getElementById('metadata_editor').scrollTop = 0;
	}

	removeMetadataField = (index) => {
		this.spectra[this.spectraIndex].meta.splice(index, 1);
	}

	applyMetadataToAll = (index) => {
		const metadata = this.currentSpectrum.meta[index];

		for (let i = 0; i < this.spectra.length; i++) {
			if (i !== this.spectraIndex) {
				this.spectra[i].meta.push(metadata);
			}
		}
	}

	applyTagsToAll = () => {
		const tags = this.currentSpectrum.tags;
		console.log(this.currentSpectrum);
		console.log(this.currentSpectrum.tags);
		this.logger.info(this.currentSpectrum);
		this.logger.info(this.currentSpectrum.tags);

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


	/**
	 * Parse spectra
	 * @param event Contains an event which serves the files
	 */
	parseFiles = (event) => {
		this.spectraLoaded = 1;

		this.loadedSpectra = 0;
		this.totalSpectra = 0;

		for (let i = 0; i < event.target.files.length; i++) {
			this.uploadLibraryService.loadSpectraFile(event.target.files[i],
				 (data, origin) => {
					this.uploadLibraryService.processData(data, (spectrum) => {
						this.asyncService.addToPool(() => {
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

							this.addSpectra.emit(spectrum);

							return new Promise((resolve => {
								resolve(true);
							}));
						}, undefined);
					}, origin);
				},
				 (progress) => {
				}
			);
		}
	}


	/**
	 * Handle MOL file input
	 */
	parseMolFile = (file) => {
		if (file.length > 0) {
			const fileReader = new FileReader();

			fileReader.onload = (event) => {
				let data = event.target.result as string;
				console.log(data);

				// Accept only the first MOL file
				let sep1 = data.indexOf('$$$$');
				const sep2 = data.indexOf('M  END');
				console.log(sep1 + ' ' + sep2);

				if (sep1 > -1 || sep2 > -1) {
					if (sep1 === -1 || (sep1 > -1 && sep2 > -1 && sep1 > sep2)) {
						sep1 = sep2;
					}

					data = data.substring(0, sep1);
				}

				this.currentSpectrum.molFile = data;
			};

			fileReader.readAsText(file[0]);
		}
	}

	convertMolToInChI = () => {
		if (typeof this.currentSpectrum.molFile !== 'undefined' && this.currentSpectrum.molFile !== '') {
			this.ctsService.convertToInchiKey(this.currentSpectrum.molFile, (result) => {
				this.currentSpectrum.inchiKey = result.inchikey;
			}, undefined);
		}
	}


	/**
	 * Export a file as MSP
	 */
	exportFile = () => {
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
	waitForLogin = () => {
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
	validateSpectra = () => {
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
		// return (invalid.length === 0);
	}


	uploadFile = () => {
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
							Authorization: 'Bearer ' + spectrum.submitter.access_token
					}}).subscribe((data: any) => {
						this.uploadLibraryService.uploadedSpectra.push(data);
					},
					 (err) => {
						this.logger.info('ERROR');
						this.logger.info(err);
					});

				// spectrum.$batchSave(spectrum.submitter.access_token);
			});
			this.router.navigate(['/upload/status']).then();
		}
	}


	/**
	 *
	 */


	isLoadingSpectra = () => {
		return this.asyncService.hasPooledTasks();
	}


	/**
	 * provides us with an overview of all our tags
	 * @param query: Contains query from tags selected
	 * @returns observable: filters the tags and pushes to an observable
	 * Performs initialization and acquisition of data used by the wizard
	 */
	loadTags = (query) => {
		return new Observable((observer => {
			observer.next(this.filterPipe.transform(this.tags, query));
		}));
	}

	/*
		 * Handle switching between spectra
		 */
	setSpectrum = (index) => {
		this.spectraIndex = index;
		this.currentSpectrum = this.spectra[this.spectraIndex];
		this.showIonTable = this.currentSpectrum.ions.length < 500;
	}

	previousSpectrum = () => {
		this.setSpectrum((this.spectraIndex + this.spectra.length - 1) % this.spectra.length);
	}

	nextSpectrum = () => {
		this.setSpectrum((this.spectraIndex + this.spectra.length + 1) % this.spectra.length);
	}

	removeCurrentSpectrum = () => {
		this.spectra.splice(this.spectraIndex, 1);

		if (this.spectra.length === 0) {
			this.resetFile();
		} else if (this.spectraIndex === this.spectra.length) {
			this.setSpectrum(this.spectraIndex - 1);
		} else {
			this.setSpectrum(this.spectraIndex);
		}
	}

	resetFile = () => {
		this.spectraLoaded = 0;
		this.spectraIndex = 0;
		this.spectra = [];

		// Clear pool
		this.asyncService.resetPool();

		// Scroll to top of the page
		window.scrollTo(0, 0);
	}
}
