/**
 * Created by sajjan on 6/11/14.
 *
 * This controller is handling the browsing of spectra
 */
import {Spectrum} from "../../services/persistence/spectrum.resource";
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Location} from "@angular/common";
import {SpectrumCacheService} from "../../services/cache/spectrum-cache.service";
import {Metadata} from "../../services/persistence/metadata.resource";
import {CookieMain} from "../../services/cookie/cookie-main.service";
import {NGXLogger} from "ngx-logger";
import {ToasterConfig, ToasterService} from "angular2-toaster";
import {GoogleAnalyticsService} from "ngx-google-analytics";
import {AuthenticationService} from "../../services/authentication.service";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import * as angular from 'angular';
import {Component, Inject, OnChanges, OnInit, SimpleChanges} from "@angular/core";
import {map, debounceTime, distinctUntilChanged} from "rxjs/operators";
import {BehaviorSubject} from "rxjs";

@Component({
    selector: 'spectra-browser',
    templateUrl: '../../views/spectra/browse/spectra.html'
})
export class SpectraBrowserComponent implements OnInit, OnChanges{
    private spectra;
    private pagination;
    private searchSplash;
    private editQuery;
    private startTime;
    private query;
    private textQuery;
    private duration;
    private inchikeyParam;
    private splashParam;
    private queryParam;
    private textParam;
    private sizeParam;
    private pageParam;
    private toasterOptions;
    status;


    constructor(@Inject(Spectrum) private spectrum: Spectrum, @Inject(SpectraQueryBuilderService)private spectraQueryBuilderService: SpectraQueryBuilderService, @Inject(Location) private location: Location,
                @Inject(SpectrumCacheService) private spectrumCache: SpectrumCacheService, @Inject(Metadata) private metadata: Metadata, @Inject(CookieMain) private cookie: CookieMain,
                @Inject(NGXLogger) private logger: NGXLogger, @Inject(ToasterService) private toaster: ToasterService, @Inject(GoogleAnalyticsService) private $gaProvider: GoogleAnalyticsService,
                @Inject(ActivatedRoute) private route: ActivatedRoute, @Inject(Router) private router: Router,
                @Inject(AuthenticationService) private authenticationService: AuthenticationService) {
    }

    ngOnInit(){
        this.toasterOptions = new ToasterConfig({
            positionClass: 'toast-center',
            timeout: 0,
            showCloseButton: true
        });
        this.inchikeyParam, this.splashParam, this.queryParam, this.sizeParam, this.pageParam, this.textParam = undefined;
        this.route.queryParamMap.pipe(
            map((params: ParamMap) => {
                this.inchikeyParam = params.get('inchikey');
                this.splashParam = params.get('splash');
                this.queryParam = params.get('query');
                this.textParam = params.get('text');
                this.sizeParam = params.get('size');
                this.pageParam = parseInt(params.get('page'));
        }));

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
        this.metadata.metaDataNames().then(
            (res: any) => {
                res.sort((a, b) => {
                    return parseInt(b.count) - parseInt(a.count);
                }).filter((x) => {
                    return x.name !== 'Last Auto-Curation';
                }).map((x) => {
                    this.pagination.tableColumnOptions.push(x.name);
                })
            }
        );

        // Handle similarity search
        if (this.location.path() === '/spectra/similaritySearch') {
            this.logger.info('Executing similarity search...');
            this.pagination.loading = true;

            if (this.spectraQueryBuilderService.hasSimilarityQuery()) {
                this.submitSimilarityQuery();
            } else {
                this.router.navigate(['/spectra/search', {type: 'similarity'}]).then();
            }
        }

        // Handle all other queries
        else {
            this.logger.debug('Executing spectrum query...');

            // Handle InChIKey queries
            if(typeof this.inchikeyParam !== 'undefined') {
                this.logger.info('Accepting InChIKey query from URL: ' + this.inchikeyParam);

                if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(this.inchikeyParam)) {
                    this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.inchikeyParam, undefined);
                } else {
                    this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.inchikeyParam, true);
                }

                this.spectraQueryBuilderService.executeQuery(true);
            }

            // Handle SPLASH queries
            if (typeof this.splashParam !== 'undefined') {
                this.logger.info('Accepting SPLASH query from URL: ' + this.splashParam);

                this.spectraQueryBuilderService.addSplashToQuery(this.splashParam);
                this.spectraQueryBuilderService.executeQuery(true);
            }

            // Handle general queries
            if (typeof this.queryParam !== 'undefined' || typeof this.textParam !== 'undefined') {
                this.logger.info('Accepting RSQL query from URL: "' + this.queryParam + '", and text search: "'+ this.textParam + '"');
                this.query = this.queryParam;
                this.textQuery = this.textParam;
            }

            // Handle page number
            if (typeof this.pageParam !== 'undefined') {
                if (!Number.isNaN(this.pageParam)) {
                    this.logger.debug('Setting current page to '+ this.pageParam);
                    this.pagination.currentPage = this.pageParam;
                }
            }

            // Handle page size
            if (typeof this.sizeParam !== 'undefined') {
                this.setPageSize(this.sizeParam);
            } else {
                let itemsPerPage = this.cookie.get('spectraBrowser-pagination-itemsPerPage');

                if (typeof itemsPerPage !== 'undefined' && itemsPerPage !== this.pagination.itemsPerPage) {
                    this.setPageSize(itemsPerPage);
                }
            }

            this.submitQuery();
        }

        // Watch for changing in pagination options
        this.setAndWatchPaginationOptions();
    }

    ngOnChanges(changes: SimpleChanges) {
        console.log(changes);
        this.logger.info(changes);
    }

    /**
     * Get total exact mass as accurate mass of spectrum
     */
    addMetadataMap = (spectra) => {
        for (let i = 0; i < spectra.length; i++) {
            let metaDataMap = {};

            if (typeof spectra[i].compound !== 'undefined') {
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
            this.logger.debug('Setting current page size to ' + this.sizeParam);
            this.pagination.itemsPerPage = size;
            this.pagination.itemsPerPageSelection = size.toString();

            if (this.pagination.itemsPerPageOptions.indexOf(size) == -1) {
                this.pagination.itemsPerPageOptions.push(size);
                this.pagination.itemsPerPageOptions.sort( (a, b) => {
                    return parseInt(a) - parseInt(b);
                });
            }

            this.cookie.update('spectraBrowser-pagination-itemsPerPage', this.pagination.itemsPerPageSelection);
        }
    };

    setAndWatchPaginationOptions = () => {
        // Load cookies
        let itemsPerPage = this.cookie.get('spectraBrowser-pagination-itemsPerPage');
        let tableView = this.cookie.getBooleanValue('spectraBrowser-pagination-table', false);
        let tableColumnsSelected = this.cookie.get('spectraBrowser-pagination-tableColumnsSelected');

        if (tableView) {
            this.pagination.table = tableView;
        }

        this.logger.info(tableColumnsSelected);
        console.log(tableColumnsSelected);
        if (typeof tableColumnsSelected !== 'undefined' && tableColumnsSelected !== '') {
            this.pagination.tableColumnSelected = JSON.parse(tableColumnsSelected);
        }


        // Watch pagination options
       /* this.itemsPerPageSelectionSubject.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            map((x) => {
                let size = parseInt(this.pagination.itemsPerPageSelection);
                if(!Number.isNaN(size)) {
                    this.logger.info('Updating search to use page size to ' + size);
                    this.router.navigate(['/spectra/search', {size: size}]).then();
                }
            })
        );

        this.tableSubject.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            map((x) => {
                this.cookie.update('spectraBrowser-pagination-table', this.pagination.table.toString())
            })
        );

        this.tableColumnSelectedSubject.pipe(
            debounceTime(400),
            distinctUntilChanged(),
            map((x) => {
                this.cookie.update('spectraBrowser-pagination-tableColumnsSelected', JSON.stringify(this.pagination.tableColumnSelected))
            })
        ); */
    };

    hideSplash = () => {
        setTimeout(() => {
            this.searchSplash = false;
        }, 50)
    };

    updateQuery = (query) => {
        this.router.navigate(['/spectra/search', {query: query}]).then();
    };

    /**
     * Start a new search
     */
    searchSpectra = () => {
        this.router.navigate(['/spectra/search', {}]).then();
    };

    /**
     * Reset the current query
     */
    resetQuery = () => {
        this.spectraQueryBuilderService.prepareQuery();
        this.spectraQueryBuilderService.executeQuery();
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

        this.spectrum.searchSimilarSpectra(
            this.spectraQueryBuilderService.getSimilarityQuery()).subscribe(
             (res) => {
                 //let data = res.data;
                 this.searchSuccess(res);
                 this.pagination.itemsPerPage = res.length;
                 this.pagination.totalSize = res.length;
            },
            this.searchError
        );
    };

    /**
     * Calculates the number of results for the given query
     */
    calculateResultCount = () => {
        this.spectrum.searchSpectraCount({
            endpoint: 'count',
            query: this.query,
            text: this.textQuery
        }).then((res: any) => {
            //let data = res.data;
            this.pagination.totalSize = res.count;
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
        this.router.navigate(['/spectra/search', {page: this.pagination.currentPage}])
        this.logger.debug(this.pagination.currentPage);
    };

    loadSpectra = () => {
        //search utilizing our compiled query so that it can be easily refined over time
        this.pagination.loading = true;
        this.spectra = [];

        $(window).scrollTop(0);

        // Note the start time for timing the spectrum search
        this.startTime = Date.now();

        let currentPage = this.pagination.currentPage - 1;

        this.logger.debug('Submitted query (page ' + currentPage + '): ' + this.query);

        // Log query with google analytics
        this.$gaProvider.event('query', 'execute', this.query, currentPage);

        if (this.query === undefined && this.textQuery === undefined) {
            this.spectrum.searchSpectra({
                size: this.pagination.itemsPerPage,
                page: currentPage
            }).then(this.searchSuccess, this.searchError);

        } else {
            this.spectrum.searchSpectra({
                endpoint: 'search',
                query: this.query,
                text: this.textQuery,
                page: currentPage,
                size: this.pagination.itemsPerPage
            }).then(this.searchSuccess, this.searchError);
        }
    };

    searchSuccess = (res) => {
        //let data = res.data;
        this.duration = (Date.now() - this.startTime) / 1000;

        if (res.length > 0) {
            // Add data to spectra object
            this.spectra = this.addMetadataMap(res);
        }

        this.hideSplash();
        this.pagination.loading = false;
    }

    searchError = (error) => {
        this.hideSplash();
        this.pagination.loading = false;

        this.toaster.pop({
            type: 'error',
            title: 'Unable to complete request',
            body: 'Please try again later.'
        });
    }
}
