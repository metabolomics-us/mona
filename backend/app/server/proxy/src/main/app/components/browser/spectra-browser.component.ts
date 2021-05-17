/**
 * Created by sajjan on 6/11/14.
 *
 * This controller is handling the browsing of spectra
 */

import * as angular from 'angular';

class SpectraBrowserController {
    private static $inject = ['$scope', 'Spectrum', 'SpectraQueryBuilderService', '$location', 'SpectrumCache', 'MetadataService', 'CookieService', '$timeout', '$log', 'toaster', 'Analytics'];
    private $scope;
    private Spectrum;
    private SpectraQueryBuilderService;
    private $location;
    private SpectrumCache;
    private MetadataService;
    private CookieService;
    private $timeout;
    private $log;
    private toaster;
    private Analytics;
    private spectra;
    private pagination;
    private searchSplash;
    private editQuery;
    private startTime;
    private query;
    private textQuery;
    private duration;
    private search;

    constructor($scope, Spectrum, SpectraQueryBuilderService, $location, SpectrumCache, MetadataService, CookieService, $timeout, $log, toaster, Analytics) {
        this.$scope = $scope;
        this.Spectrum = Spectrum;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.$location = $location;
        this.SpectrumCache = SpectrumCache;
        this.MetadataService = MetadataService;
        this.CookieService = CookieService;
        this.$timeout = $timeout;
        this.$log = $log;
        this.toaster = toaster;
        this.Analytics = Analytics;
    }

    $onInit = () => {
        /**
         * contains all local objects and is our model
         * @type {Array}
         */
        this.spectra = [];
        /**
         * loads more spectra into the given view
         */
        this.pagination = {
            loading: true,

            // Pagination parameters
            currentPage: 1,
            itemsPerPage: 10,
            maxSize: 10,
            totalSize: -1,

            // Page size parameters
            itemsPerPageSelection: '10',
            itemsPerPageOptions: [10, 25, 50],

            // Table view parameters
            table: false,
            tableColumnOptions: ["ID", "Name", "Structure", "Mass Spectrum", "Accurate Mass"],
            tableColumnSelected: ["ID", "Name", "Structure", "Mass Spectrum", "Accurate Mass"]
        }
        /**
         * Handle search splash
         */
        this.searchSplash = true;
        /**
         * Query dropdown and editor
         */
        this.editQuery = false;

        // Get unique metadata values for dropdown
        this.MetadataService.metaDataNames({},
            (data) => {
                data.sort((a, b) => {
                    return parseInt(b.count) - parseInt(a.count);
                }).filter((x) => {
                    return x.name !== 'Last Auto-Curation';
                }).map((x) => {
                    this.pagination.tableColumnOptions.push(x.name);
                })
            }
        );

        // Handle similarity search
        if (this.$location.path() === '/spectra/similaritySearch') {
            this.$log.debug('Executing similarity search...');
            this.pagination.loading = true;

            if (this.SpectraQueryBuilderService.hasSimilarityQuery()) {
                this.submitSimilarityQuery();
            } else {
                this.$location.path('/spectra/search').search({type: 'similarity'});
            }
        }

        // Handle all other queries
        else {
            this.$log.debug('Executing spectrum query...');

            // Handle InChIKey queries
            if (this.$location.search().hasOwnProperty('inchikey')) {
                this.$log.info('Accepting InChIKey query from URL: ' + this.$location.search().inchikey);

                if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(this.$location.search().inchikey)) {
                    this.SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.$location.search().inchikey);
                } else {
                    this.SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.$location.search().inchikey, true);
                }

                this.SpectraQueryBuilderService.executeQuery(true);
            }

            // Handle SPLASH queries
            if (this.$location.search().hasOwnProperty('splash')) {
                this.$log.info('Accepting SPLASH query from URL: ' + this.$location.search().splash);

                this.SpectraQueryBuilderService.addSplashToQuery(this.$location.search().splash);
                this.SpectraQueryBuilderService.executeQuery(true);
            }

            // Handle general queries
            if (this.$location.search().hasOwnProperty('query') || this.$location.search().hasOwnProperty('text')) {
                this.$log.info('Accepting RSQL query from URL: "' + this.$location.search().query + '", and text search: "'+ this.$location.search().text + '"');

                this.query = this.$location.search().query;
                this.textQuery = this.$location.search().text;
            }

            // Handle page number
            if (this.$location.search().hasOwnProperty('page')) {
                let page = parseInt(this.$location.search().page);

                if (!Number.isNaN(page)) {
                    this.$log.debug('Setting current page to '+ this.$location.search().page);
                    this.pagination.currentPage = page;
                }
            }

            // Handle page size
            if (this.$location.search().hasOwnProperty('size')) {
                this.setPageSize(this.$location.search().size);
            } else {
                let itemsPerPage = this.CookieService.get('spectraBrowser-pagination-itemsPerPage');

                if (angular.isDefined(itemsPerPage) && itemsPerPage !== this.pagination.itemsPerPage) {
                    this.setPageSize(itemsPerPage);
                }
            }

            this.submitQuery();
        }

        // Watch for changing in pagination options
        this.setAndWatchPaginationOptions();
    }

    /**
     * Get total exact mass as accurate mass of spectrum
     */
    addMetadataMap = (spectra) => {
        for (let i = 0; i < spectra.length; i++) {
            let metaDataMap = {};

            if (angular.isDefined(spectra[i].compound)) {
                for (let j = 0; j < spectra[i].compound.length; j++) {
                    spectra[i].compound[j].metaData.forEach((metaData) => {
                        if (metaData.name === 'total exact mass') {
                            metaDataMap[metaData.name] = parseFloat(metaData.value).toFixed(4);
                        } else {
                            metaDataMap[metaData.name] = metaData.value;
                        }

                        if (metaData.unit) {
                            metaDataMap[metaData.name] += ' ' + metaData.unit;
                        }
                    });

                    if (spectra[i].compound.kind === 'biological')
                        break;
                }
            }

            spectra[i].metaData.forEach((metaData) => {
                if (metaData.name == 'mass accuracy' || metaData.name == "mass error") {
                    metaDataMap[metaData.name] = parseFloat(metaData.value).toFixed(4);
                } else {
                    metaDataMap[metaData.name] = metaData.value;
                }

                if (metaData.unit) {
                    metaDataMap[metaData.name] += ' ' + metaData.unit;
                }
            });

            spectra[i].metaDataMap = metaDataMap;
        }

        return spectra
    };

    setPageSize = (pageSize) => {
        let size = parseInt(pageSize);

        if (!Number.isNaN(size)) {
            this.$log.debug('Setting current page size to ' + this.$location.search().size);
            this.pagination.itemsPerPage = size;
            this.pagination.itemsPerPageSelection = size.toString();

            if (this.pagination.itemsPerPageOptions.indexOf(size) == -1) {
                this.pagination.itemsPerPageOptions.push(size);
                this.pagination.itemsPerPageOptions.sort(function (a, b) {
                    return parseInt(a) - parseInt(b);
                });
            }

            this.CookieService.update('spectraBrowser-pagination-itemsPerPage', this.pagination.itemsPerPageSelection);
        }
    };

    setAndWatchPaginationOptions = () => {
        // Load cookies
        let itemsPerPage = this.CookieService.get('spectraBrowser-pagination-itemsPerPage');
        let tableView = this.CookieService.getBooleanValue('spectraBrowser-pagination-table', false);
        let tableColumnsSelected = this.CookieService.get('spectraBrowser-pagination-tableColumnsSelected');

        if (tableView) {
            this.pagination.table = tableView;
        }

        if (angular.isDefined(tableColumnsSelected)) {
            this.pagination.tableColumnSelected = JSON.parse(tableColumnsSelected);
        }


        // Watch pagination options
        this.$scope.$watch('pagination.itemsPerPageSelection', () => {
            let size = parseInt(this.pagination.itemsPerPageSelection);

            if (!Number.isNaN(size)) {
                this.$log.info('Updating search to use page size to ' + size);
                this.$location.search('size', size).replace();
            }
        });

        this.$scope.$watch(()=> this.pagination.table, () =>
            this.CookieService.update('spectraBrowser-pagination-table', this.pagination.table.toString())
        );
        //this.$scope.$watch('pagination.table', function () {
        //    this.CookieService.update('spectraBrowser-pagination-table', this.pagination.table.toString());
        //});

        this.$scope.$watch(()=> this.pagination.tableColumnSelected, () =>
            this.CookieService.update('spectraBrowser-pagination-tableColumnsSelected', JSON.stringify(this.pagination.tableColumnSelected))
        );
        //this.$scope.$watch('pagination.tableColumnSelected', function () {
        //    this.CookieService.update('spectraBrowser-pagination-tableColumnsSelected', JSON.stringify(this.pagination.tableColumnSelected));
        //});
    };

    hideSplash = () => {
        this.$timeout(() => {
            this.searchSplash = false;
        }, 50)
    };

    updateQuery = (query) => {
        this.$location.search('query', query);
    };

    /**
     * Start a new search
     */
    searchSpectra = () => {
        this.$location.path('spectra/search').search({});
    };

    /**
     * Reset the current query
     */
    resetQuery = () => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.SpectraQueryBuilderService.executeQuery();
    };

    /**
     * Submits our query to the server
     */
    submitQuery = () => {
        this.calculateResultCount();
        this.loadSpectra();
    };

    /**
     * Submits similarity query to the server
     */
    submitSimilarityQuery = () => {
        this.startTime = Date.now();

        this.Spectrum.searchSimilarSpectra(
            this.SpectraQueryBuilderService.getSimilarityQuery(),
             (data) => {
                this.searchSuccess(data);
                this.pagination.itemsPerPage = data.length;
                this.pagination.totalSize = data.length;
            },
            this.searchError
        );
    };

    /**
     * Calculates the number of results for the given query
     */
    calculateResultCount = () => {
        this.Spectrum.searchSpectraCount({
            endpoint: 'count',
            query: this.query,
            text: this.textQuery
        }, (data) => {
            this.pagination.totalSize = data.count;
        });
    };

    /**
     * returns the display url for the spectrum for the given index
     * @param id
     * @param index
     */
    viewSpectrum = (id, index) => {
        // SpectrumCache.setBrowserSpectra($scope.spectra);
        // SpectrumCache.setSpectrum($scope.spectra[index]);

        return '/spectra/display/' + id;
    };


    /**
     * Execute query
     */
    loadPage = () => {
        this.search('page', this.pagination.currentPage);
    };

    loadSpectra = () => {
        //search utilizing our compiled query so that it can be easily refined over time
        this.pagination.loading = true;
        this.spectra = [];

        $(window).scrollTop(0);

        // Note the start time for timing the spectrum search
        this.startTime = Date.now();

        let currentPage = this.pagination.currentPage - 1;

        this.$log.debug('Submitted query (page ' + currentPage + '): ' + this.query);

        // Log query with google analytics
        this.Analytics.trackEvent('query', 'execute', this.query, currentPage);

        if (this.query === undefined && this.textQuery === undefined) {
            this.Spectrum.searchSpectra({
                size: this.pagination.itemsPerPage,
                page: currentPage
            }, this.searchSuccess, this.searchError);
        } else {
            this.Spectrum.searchSpectra({
                endpoint: 'search',
                query: this.query,
                text: this.textQuery,
                page: currentPage,
                size: this.pagination.itemsPerPage
            }, this.searchSuccess, this.searchError);
        }
    };

    searchSuccess(data) {
        this.duration = (Date.now() - this.startTime) / 1000;

        if (data.length > 0) {
            // Add data to spectra object
            this.spectra = this.addMetadataMap(data);
        }

        this.hideSplash();
        this.pagination.loading = false;
    }

    searchError(error) {
        this.hideSplash();
        this.pagination.loading = false;

        this.toaster.pop({
            type: 'error',
            title: 'Unable to complete request',
            body: 'Please try again later.'
        });
    }
}

let SpectraBrowserComponent = {
    selector: "spectraBrowser",
    templateUrl: "../../views/spectra/browse/spectra.html",
    bindings: {},
    controller: SpectraBrowserController
}

angular.module('moaClientApp')
    .component(SpectraBrowserComponent.selector, SpectraBrowserComponent);
