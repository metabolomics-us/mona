/**
 * Created by sajjan on 4/20/15.
 */

import * as angular from 'angular';
import {AuthenticationService} from "../../services/authentication.service";

class AdvancedUploaderController{
	private $scope;
	private $rootScope;
	private $window;
	private $location;
	private UploadLibraryService;
	private gwCtsService;
	private TagService;
	private $q;
	private $filter;
	private AsyncService;
	private $log;
	private REST_BACKEND_SERVER;
	private $http;
	private AuthenticationService;
	// Loaded spectra data/status
	private spectraLoaded;
	private currentSpectrum;
	private spectra;
	private spectrumErrors;
	private spectraIndex;
	private loadedSpectra;
	private totalSpectra;
	private spectrum;
	private tags;
	private showIonTable;
	/**
	 * Sort order for the ion table - default m/z ascending
	 */
	private ionTableSort;
	private ionTableSortReverse;

	// Parameters provided for trimming spectra
	private ionCuts;





	constructor($scope, $rootScope, $window, $location, UploadLibraryService, gwCtsService, TagService, $q, $filter, AsyncService, $log, REST_BACKEND_SERVER, $http, AuthenticationService){
		this.$scope = $scope;
		this.$rootScope = $rootScope;
		this.$window = $window;
		this.$location = $location;
		this.UploadLibraryService = UploadLibraryService;
		this.gwCtsService = gwCtsService;
		this.TagService = TagService;
		this.$q = $q;
		this.$filter = $filter;
		this.AsyncService = AsyncService;
		this.$log = $log;
		this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
		this.$http = $http;
		this.AuthenticationService = AuthenticationService;
	}

	$onInit = () => {
		this.spectraLoaded = 0;
		this.spectra = [];
		this.spectrumErrors = {};
		this.spectraIndex = 0;
		this.ionCuts = {
		};
		this.ionTableSort = 'ion';
		this.ionTableSortReverse = false;

		this.$scope.$on('AddSpectrum', (event, spectrum) => {
			this.spectra.push(spectrum);
			this.loadedSpectra++;
			this.spectraLoaded = 2;

			// Force update of current spectrum if needed
			this.setSpectrum(this.spectraIndex);
		});

		this.TagService.query(
			(data) => {
				this.tags = data;
			},
			(error) => {
				this.$log.error('failed: ' + error);
			}
		);
	}

	sortIonTable = (column) => {
		if (column === 'ion') {
			this.ionTableSortReverse = (this.ionTableSort === '+ion') ? !this.ionTableSortReverse : false;
			this.ionTableSort = '+ion';
		}
		else if (column === 'intensity') {
			this.ionTableSortReverse = (this.ionTableSort === '-intensity') ? !this.ionTableSortReverse : false;
			this.ionTableSort = '-intensity';
		}
		else if (column === 'annotation') {
			this.ionTableSortReverse = (this.ionTableSort === '-annotation') ? !this.ionTableSortReverse : false;
			this.ionTableSort = '-annotation';
		}
	};


	/**
	 * Perform mass spectrum trimming
	 */

	performIonCuts =  (index) => {
		if (angular.isUndefined(index)) {
			index = this.spectraIndex;
		}

		let cutIons = [];
		let retainedIons = [];

		let limit = 0;

		if (angular.isDefined(this.ionCuts.absAbundance)) {
			limit = this.ionCuts.absAbundance;
		}

		if (angular.isDefined(this.ionCuts.basePeak)) {
			let basePeakCut = this.ionCuts.basePeak * this.spectra[index].basePeak / 100
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

		if (angular.isDefined(this.ionCuts.nIons) && retainedIons.length > this.ionCuts.nIons) {
			retainedIons.sort( (a, b) => {
				return this.spectra[index].ions[b].intensity - this.spectra[index].ions[a].intensity;
			});

			for (let i = this.ionCuts.nIons; i < retainedIons.length; i++) {
				this.spectra[index].ions[retainedIons[i]].selected = false;
			}
		}
	};

	performAllIonCuts =  () => {
		for (let i = 0; i < this.spectra.length; i++) {
			this.performIonCuts(i);
		}
	};

	resetIonCuts =  () => {
		for (let i = 0; i < this.currentSpectrum.ions.length; i++) {
			this.currentSpectrum.ions[i].selected = true;
		}
	};


	/**
	 * Add a new name to the list
	 */

	addName = () => {
		if (this.currentSpectrum.names[this.currentSpectrum.names.length - 1] !== '') {
			this.currentSpectrum.names.push('');
		}
	};


	/**
	 * Handle metadata functionality
	 */

	addMetadataField = () => {
		this.currentSpectrum.meta.push({name: '', value: ''});
		this.$scope.$apply();
		$('#metadata_editor').scrollTop($('#metadata_editor')[0].scrollHeight);
	};

	removeMetadataField = (index) => {
		this.spectra[this.spectraIndex].meta.splice(index, 1);
	};

	applyMetadataToAll = (index) => {
		let metadata = this.currentSpectrum.meta[index];

		for (let i = 0; i < this.spectra.length; i++) {
			if (i !== this.spectraIndex) {
				this.spectra[i].meta.push(metadata);
			}
		}
	};

	applyTagsToAll = () => {
		let tags = this.currentSpectrum.tags;

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
							break
						}
					}

					if (!hasTag) {
						this.spectra[i].tags.push(tags[j]);
					}
				}
			}
		}
	};


	/**
	 * Parse spectra
	 * @param files
	 */
	parseFiles = (files) => {
		this.spectraLoaded = 1;

		this.loadedSpectra = 0;
		this.totalSpectra = 0;

		for (let i = 0; i < files.length; i++) {
			this.UploadLibraryService.loadSpectraFile(files[i],
				 (data, origin) => {
					this.UploadLibraryService.processData(data, (spectrum) => {
						this.AsyncService.addToPool(() => {
							// Create list of ions
							spectrum.basePeak = 0;

							spectrum.ions = spectrum.spectrum.split(' ').map((x) => {
								x = x.split(':');
								let annotation = '';

								for (let i = 0; i < spectrum.meta.length; i++) {
									if (spectrum.meta[i].category === 'annotation' && spectrum.meta[i].value === x[0]) {
										annotation = spectrum.meta[i].name;
									}
								}

								let intensity = parseFloat(x[1]);

								if (intensity > spectrum.basePeak) {
									spectrum.basePeak = intensity;
								}

								return {
									ion: parseFloat(x[0]),
									intensity: intensity,
									annotation: annotation,
									selected: true
								}
							});

							// Get structure from InChIKey if no InChI is provided
							if (angular.isDefined(spectrum.inchiKey) && angular.isUndefined(spectrum.inchi)) {
								this.gwCtsService.convertInchiKeyToMol(spectrum.inchiKey, (molecule) => {
									if (molecule !== null) {
										spectrum.molFile = molecule;
									}
								});
							}

							// Remove annotations and origin from metadata
							spectrum.hiddenMetadata = spectrum.meta.filter((metadata) => {
								return metadata.name === 'origin' || (angular.isDefined(metadata.category) && metadata.category === 'annotation');
							});

							spectrum.meta = spectrum.meta.filter((metadata) => {
								return metadata.name !== 'origin' && (angular.isUndefined(metadata.category) || metadata.category !== 'annotation');
							});

							// Add an empty metadata field if none exist
							if (spectrum.meta.length === 0) {
								spectrum.meta.push({name: '', value: ''});
							}

							this.$scope.$broadcast('AddSpectrum', spectrum);

							let defered = this.$q.defer();
							defered.resolve(true);
							return defered.promise;
						});
					}, origin);
				},
				 (progress) => {
				}
			);
		}
	};


	/**
	 * Handle MOL file input
	 */
	parseMolFile = (file) => {
		if (file.length > 0) {
			let fileReader = new FileReader();

			fileReader.onload = (event) => {
				let data = event.target.result as String;
				console.log(data);

				// Accept only the first MOL file
				let sep1 = data.indexOf('$$$$'), sep2 = data.indexOf('M  END');
				console.log(sep1 + " " + sep2);

				if (sep1 > -1 || sep2 > -1) {
					if (sep1 === -1 || (sep1 > -1 && sep2 > -1 && sep1 > sep2)) {
						sep1 = sep2;
					}

					data = data.substring(0, sep1);
				}

				this.currentSpectrum.molFile = data;
				this.$scope.$apply();
			};

			fileReader.readAsText(file[0]);
		}
	};

	convertMolToInChI = () => {
		if (angular.isDefined(this.currentSpectrum.molFile) && this.currentSpectrum.molFile !== '') {
			this.gwCtsService.convertToInchiKey(this.currentSpectrum.molFile, (result) => {
				this.currentSpectrum.inchiKey = result.inchikey;
			});
		}
	};


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

			if (angular.isDefined(this.spectra[i].inchiKey) && this.spectra[i].inchiKey !== '') {
				msp += 'InChIKey: ' + this.spectra[i].inchiKey + '\n';
			}

			if (angular.isDefined(this.spectra[i].inchi) && this.spectra[i].inchi !== '') {
				msp += 'InChI: ' + this.spectra[i].inchi + '\n';
			}

			if (angular.isDefined(this.spectra[i].smiles) && this.spectra[i].smiles !== '') {
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
			let ions = [];

			for (let j = 0; j < this.spectra[i].ions.length; j++) {
				if (this.spectra[i].ions[j].selected) {
					ions.push(this.spectra[i].ions[j]);
				}
			}

			ions.sort(function (a, b) {
				return a[0] - b[0];
			});

			msp += 'Num Peaks: ' + ions.length + '\n';

			for (let j = 0; j < ions.length; j++) {
				msp += ions[j].ion + ' ' + ions[j].intensity + (ions[j].annotation !== '' ? ' ' + ions[j].annotation : '') + '\n';
			}
		}

		// Export file
		// http://stackoverflow.com/a/18197341/406772
		let pom = document.createElement('a');
		pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(msp));
		pom.setAttribute('download', 'export.msp');
		pom.style.display = 'none';

		document.body.appendChild(pom);
		pom.click();
		document.body.removeChild(pom);
	};


	/**
	 *
	 */
	waitForLogin = () => {
		this.$scope.$on('auth:login-success', (event, data, status, headers, config) => {
			if (this.spectraLoaded === 2) {
				this.uploadFile();
			}
		});
	};


	/**
	 * Upload current data
	 */
	validateSpectra = () => {
		let invalid = [];

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

			if ((angular.isUndefined(this.spectra[i].inchi) || this.spectra[i].inchi === '') &&
				(angular.isUndefined(this.spectra[i].molFile) || this.spectra[i].molFile === '') &&
				(angular.isUndefined(this.spectra[i].smiles) || this.spectra[i].smiles === '')) {
				this.spectra[i].errors.push('This spectrum requires a structure in order to upload. Please provide a MOL file or InChI code!');
			}


			if (this.spectra[i].errors.length > 0) {
				invalid.push(i);
			}
		}

		if (invalid.length > 0) {
			this.setSpectrum(invalid[0]);
			this.$scope.error = 'There are some errors in the data you have provided.  The';
			this.$window.scrollTo(0, 0);
		}

		return true;
		//return (invalid.length === 0);
	};


	uploadFile = () => {
		if (this.validateSpectra()) {
			// Reset the spectrum count if necessary
			if (!this.UploadLibraryService.isUploading()) {
				this.UploadLibraryService.completedSpectraCount = 0;
				this.UploadLibraryService.failedSpectraCount = 0;
				this.UploadLibraryService.uploadedSpectraCount = 0;
				this.UploadLibraryService.uploadStartTime = new Date().getTime();
			}

			// Re-add origin and annotations to metadata:
			for (let i = 0; i < this.spectra.length; i++) {
				this.spectra[i].meta.push.apply(this.spectra[i].meta, this.spectra[i].hiddenMetadata);
			}

			this.UploadLibraryService.uploadSpectra(this.spectra,  (spectrum) => {
				let req = {
					method: 'POST',
					url: this.REST_BACKEND_SERVER + '/rest/spectra',
					headers: {
						'Content-Type': 'application/json',
						'Authorization': 'Bearer ' + spectrum.submitter.access_token
					},
					data: JSON.stringify(spectrum)
				};

				this.$http(req).then((data) => {
						this.$log.info('Spectra successfully Upload!');
						this.$log.info('Reference ID: ' + data.data.id);
						this.$log.info(data);
						this.UploadLibraryService.uploadedSpectra.push(data.data);
					},
					 (err) => {
						this.$log.info('ERROR');
						this.$log.info(err);
					});

				//spectrum.$batchSave(spectrum.submitter.access_token);
			}, this.spectrum);

			this.$location.path('/upload/status');
		}
	};


	/**
	 *
	 */


	isLoadingSpectra = () => {
		return this.AsyncService.hasPooledTasks();
	};


	/**
	 * provides us with an overview of all our tags
	 * @param query
	 * @returns {*}
	 * Performs initialization and acquisition of data used by the wizard
	 */
	loadTags = (query) => {
		let deferred = this.$q.defer();

		// First filters by the query and then removes any tags already selected
		deferred.resolve(this.$filter('filter')(this.tags, query));

		return deferred.promise;
	};

	/*
		 * Handle switching between spectra
		 */
	setSpectrum = (index) => {
		this.spectraIndex = index;
		this.currentSpectrum = this.spectra[this.spectraIndex];
		this.showIonTable = this.currentSpectrum.ions.length < 500;
	};

	previousSpectrum = () => {
		this.setSpectrum((this.spectraIndex + this.spectra.length - 1) % this.spectra.length);
	};

	nextSpectrum = () => {
		this.setSpectrum((this.spectraIndex + this.spectra.length + 1) % this.spectra.length);
	};

	removeCurrentSpectrum = () => {
		this.spectra.splice(this.spectraIndex, 1);

		if (this.spectra.length === 0) {
			this.resetFile();
		} else if (this.spectraIndex === this.spectra.length) {
			this.setSpectrum(this.spectraIndex - 1);
		} else {
			this.setSpectrum(this.spectraIndex);
		}
	};

	resetFile = () => {
		this.spectraLoaded = 0;
		this.spectraIndex = 0;
		this.spectra = [];

		// Clear pool
		this.AsyncService.resetPool();

		// Scroll to top of the page
		this.$window.scrollTo(0, 0);
	};
}

let AdvancedUploaderComponent = {
	selector: "advancedUploader",
	templateUrl: "../../views/spectra/upload/advancedUploader.html",
	bindings: {},
	controller: AdvancedUploaderController
}


angular.module('moaClientApp')
	.component(AdvancedUploaderComponent.selector, AdvancedUploaderComponent);
