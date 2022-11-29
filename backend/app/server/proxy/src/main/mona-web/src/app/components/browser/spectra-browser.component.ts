/**
 * Created by sajjan on 6/11/14.
 * Updated by nolanguzman on 10/31/2021
 * This controller is handling the browsing of spectra
 */
import {Spectrum} from '../../services/persistence/spectrum.resource';
import {SpectrumModel} from "../../mocks/spectrum.model";
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {Location} from '@angular/common';
import {SpectrumCacheService} from '../../services/cache/spectrum-cache.service';
import {Metadata} from '../../services/persistence/metadata.resource';
import {CookieMain} from '../../services/cookie/cookie-main.service';
import {NGXLogger} from 'ngx-logger';
import {ToasterConfig, ToasterService} from 'angular2-toaster';
import {GoogleAnalyticsService} from 'ngx-google-analytics';
import {AuthenticationService} from '../../services/authentication.service';
import {FeedbackCacheService} from '../../services/feedback/feedback-cache.service';
import {MassDeletionService} from '../../services/persistence/mass-deletion.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Component, OnInit} from '@angular/core';
import {first} from 'rxjs/operators';
import {faEdit, faTable, faList, faSearch, faSync, faServer, faSpinner, faTrash} from '@fortawesome/free-solid-svg-icons';
import {faBookmark} from '@fortawesome/free-regular-svg-icons';
import {BehaviorSubject} from 'rxjs';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {MassDeleteModalComponent} from './mass-delete-modal.component';

@Component({
    selector: 'spectra-browser',
    templateUrl: '../../views/spectra/browse/spectra.html'
})
export class SpectraBrowserComponent implements OnInit{
    spectra: SpectrumModel[];
    pagination;
    searchSplash;
    editQuery;
    startTime;
    query;
    duration;
    inchikeyParam;
    splashParam;
    queryParam;
    sizeParam;
    pageParam;
    tableParam;
    toasterOptions;
    itemsPerPageSelectionSubject;
    tableSubject;
    tableColumnSelectedSubject;
    initial;
    status;
    faEdit = faEdit;
    faTable = faTable;
    faList = faList;
    faSearch = faSearch;
    faSync = faSync;
    faServer = faServer;
    faSpinner = faSpinner;
    faBookmark = faBookmark;
    faTrash = faTrash;

    constructor(public spectrum: Spectrum, public spectraQueryBuilderService: SpectraQueryBuilderService,  public location: Location,
                public spectrumCache: SpectrumCacheService,  public metadata: Metadata,  public cookie: CookieMain,
                public logger: NGXLogger,  public toaster: ToasterService,  public $gaProvider: GoogleAnalyticsService,
                public route: ActivatedRoute,  public router: Router, public feedbackCache: FeedbackCacheService,
                public authenticationService: AuthenticationService, public activatedRoute: ActivatedRoute,
                public massDeletion: MassDeletionService, public modalService: NgbModal) {
    }

    ngOnInit() {
      this.toasterOptions = new ToasterConfig({
        positionClass: 'toast-center',
        timeout: 0,
        showCloseButton: true
      });

      /**
       * contains all local objects and is our model
       */
      this.spectra = [];

      this.initial = true;
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
        tableColumnOptions: ['ID', 'Name', 'Structure', 'Mass Spectrum', 'Accurate Mass'],
        tableColumnSelected: ['ID', 'Name', 'Structure', 'Mass Spectrum', 'Accurate Mass']
      };
      this.itemsPerPageSelectionSubject = new BehaviorSubject<string>(this.pagination.itemsPerPageSelection);
      this.tableSubject = new BehaviorSubject<boolean>(this.pagination.table);
      this.tableColumnSelectedSubject = new BehaviorSubject<object>(this.pagination.tableColumnSelected);
      /**
       * Handle search splash
       */
      this.searchSplash = true;
      /**
       * Query dropdown and editor
       */
      this.editQuery = false;

      this.activatedRoute.queryParams.subscribe(params => {
          this.logger.debug('Reading parameters from url');
          this.inchikeyParam = params.inchikey || undefined;
          this.splashParam = params.splash || undefined;
          this.queryParam = params.query || undefined;
          this.sizeParam = params.size || undefined;
          this.pageParam = parseInt(params.page, 10);
          this.tableParam = params.table || undefined;
          this.loadData();
      });

      // Watch for changing in pagination options
      this.setAndWatchPaginationOptions();
    }

  loadData() {
        // Get unique metadata values for dropdown
        this.metadata.metaDataNames().pipe(first()).subscribe(
          (res: any) => {
            res.sort((a, b) => {
              return parseInt(b.count, 10) - parseInt(a.count, 10);
            }).filter((x) => {
              return x.name !== 'Last Auto-Curation';
            }).map((x) => {
              this.pagination.tableColumnOptions.push(x.name);
            });
          }
        );

        // Handle similarity search
        if (this.location.path().split('?')[0] === '/spectra/similaritySearch') {
          this.pagination.loading = true;

          if (this.spectraQueryBuilderService.hasSimilarityQuery()) {
            this.logger.info('Executing similarity search...');
            this.submitSimilarityQuery();
          } else {
            this.router.navigate(['/spectra/search', {type: 'similarity'}]).then();
          }
        }

        // Handle all other queries
        else {
          this.logger.debug('Executing spectrum query...');

          // Handle InChIKey queries
          if (typeof this.inchikeyParam !== 'undefined') {
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
          if (typeof this.queryParam !== 'undefined') {
            this.logger.info('Accepting RSQL query from URL: "' + this.queryParam + '"');
            this.query = this.queryParam;
          }

          // Handle page number
          if (typeof this.pageParam !== 'undefined') {
            if (!Number.isNaN(this.pageParam)) {
              this.logger.debug('Setting current page to ' + this.pageParam);
              this.pagination.currentPage = this.pageParam;
            }
          }

          // Handle page size
          if ((typeof this.sizeParam !== 'undefined') && (parseInt(this.sizeParam, 10) !== this.pagination.itemsPerPage)) {
            this.setPageSize(this.sizeParam);
          } else {
            const itemsPerPage = this.cookie.get('spectraBrowser-pagination-itemsPerPage');
            if ((typeof itemsPerPage !== 'undefined') && (parseInt(itemsPerPage, 10) !== this.pagination.itemsPerPage)) {
              this.setPageSize(itemsPerPage);
            }
          }
          this.submitQuery();
        }
    }

    setItemsPerPageSelection() {
      this.itemsPerPageSelectionSubject.next(this.pagination.itemsPerPageSelection);
    }

    setTable() {
      this.tableSubject.next(this.pagination.table);
      this.router.navigate([], {queryParams: {table: this.pagination.table},
         queryParamsHandling: 'merge', replaceUrl: true, skipLocationChange: false}).then();
    }

    setTableColumnsSelection() {
      this.tableColumnSelectedSubject.next(this.pagination.tableColumnSelected);
    }
    /**
     * Get total exact mass as accurate mass of spectrum
     */
    addMetadataMap(spectra: SpectrumModel[]) {
        for (let i = 0; i < spectra.length; i++) {
            const metaDataMap = {};

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

                    if (spectra[i].compound[j].kind === 'biological') {
                        break;
                    }
                }
            }

            spectra[i].metaData.forEach((metaData) => {
                if (metaData.name === 'mass accuracy' || metaData.name === 'mass error') {
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

        return spectra;
    }

    setPageSize(pageSize) {
        const size = parseInt(pageSize, 10);

        if (!Number.isNaN(size)) {
            this.logger.info('Setting current page size to ' + this.sizeParam);
            this.pagination.itemsPerPage = size;
            this.pagination.itemsPerPageSelection = size.toString();
            this.setItemsPerPageSelection();

            if (this.pagination.itemsPerPageOptions.indexOf(size) === -1) {
                this.pagination.itemsPerPageOptions.push(size);
                this.pagination.itemsPerPageOptions.sort( (a, b) => {
                    return parseInt(a, 10) - parseInt(b, 10);
                });
            }

            this.cookie.update('spectraBrowser-pagination-itemsPerPage', this.pagination.itemsPerPageSelection);
        }
    }

    setAndWatchPaginationOptions() {
        // Load cookies
        const itemsPerPage = this.cookie.get('spectraBrowser-pagination-itemsPerPage');
        const tableView: boolean = typeof this.tableParam !== 'undefined' ? JSON.parse(this.tableParam) : this.cookie.getBooleanValue('spectraBrowser-pagination-table', false);
        const tableColumnsSelected = this.cookie.get('spectraBrowser-pagination-tableColumnsSelected');
        if (tableView) {
            this.logger.info('Setting Table View');
            this.pagination.table = tableView;
            this.setTable();
        }

        this.logger.info(tableColumnsSelected);
        if (typeof tableColumnsSelected !== 'undefined' && tableColumnsSelected !== '') {
            this.pagination.tableColumnSelected = JSON.parse(tableColumnsSelected);
            this.setTableColumnsSelection();
        }


        this.itemsPerPageSelectionSubject.subscribe(
            (x) => {
                const size = parseInt(this.pagination.itemsPerPageSelection, 10);
                if (!Number.isNaN(size)) {
                    this.logger.info('Updating search to use page size to ' + size);
                    this.router.navigate([], {queryParams:
                        {size: size.toString()}, queryParamsHandling: 'merge', skipLocationChange: false, replaceUrl: true}).then();
                }
            }
        );

        this.tableSubject.subscribe(
            (x) => {
                this.cookie.update('spectraBrowser-pagination-table', this.pagination.table.toString());
            }
        );

        this.tableColumnSelectedSubject.subscribe(
              (x) => {
                this.cookie.update('spectraBrowser-pagination-tableColumnsSelected', JSON.stringify(this.pagination.tableColumnSelected));
            }
        );
        this.initial = false;
    }

    hideSplash() {
      this.searchSplash = false;
    }

    updateQuery(query) {
        this.router.navigate([], {queryParams: {query}}).then();
    }

    /**
     * Start a new search
     */
    searchSpectra() {
        this.router.navigate(['/spectra/search']).then();
    }

    /**
     * Reset the current query
     */
    resetQuery() {
        this.spectraQueryBuilderService.prepareQuery();
        this.query = undefined;
        this.spectraQueryBuilderService.executeQuery();
    }

    /**
     * Submits our query to the server
     */
    submitQuery() {
        this.calculateResultCount();
        this.loadSpectra();
    }

    /**
     * Submits similarity query to the server
     */
    submitSimilarityQuery() {
      this.startTime = Date.now();
      this.pagination.loading = true;
      this.spectra = [];

      if (this.initial && !this.sizeParam) {
        this.hideSplash();
        this.pagination.loading = false;
      } else {
        this.spectrum.searchSimilarSpectra(
          this.spectraQueryBuilderService.getSimilarityQuery())
          .subscribe(
            (res) => {
              this.pagination.itemsPerPage = res.length;
              this.pagination.totalSize = res.length;
              this.searchSuccess(res);
            },
            this.searchError
          );
      }
    }

    /**
     * Calculates the number of results for the given query
     */
    calculateResultCount() {
        this.spectrum.searchSpectraCount({
            query: this.query
        }).pipe(first()).subscribe((res: any) => {
            this.pagination.totalSize = res.count;
        });
    }

    /**
     * returns the display url for the spectrum for the given index
     * @param id takes spectrum id
     * @param index not needed
     */
    viewSpectrum(id) {
        return '/spectra/display/' + id;
    }


    /**
     * Execute query
     */
    loadPage() {
        this.router.navigate([],
          {
            queryParams: {page: this.pagination.currentPage},
            queryParamsHandling: 'merge'
          }).then();
        this.logger.debug(this.pagination.currentPage);
    }

    loadSpectra() {
        // search utilizing our compiled query so that it can be easily refined over time
        this.pagination.loading = true;
        this.spectra = [];

        window.scrollTo(0, 0);

        // Note the start time for timing the spectrum search
        this.startTime = Date.now();

        const currentPage = this.pagination.currentPage - 1;

        this.logger.info('Submitted query (page ' + currentPage + '): ' + this.query);

        // Log query with google analytics
        this.$gaProvider.event('query', 'execute', this.query, currentPage);

        if (this.initial && !this.sizeParam) {
          this.hideSplash();
          this.pagination.loading = false;
        } else if (this.query === undefined) {
            this.logger.info('submitting empty query');
            this.spectrum.searchSpectra({
                size: this.pagination.itemsPerPage,
                page: currentPage
            }).pipe(first()).subscribe(this.searchSuccess, this.searchError);

        } else {
            this.logger.info('fetching spectra from query');
            this.spectrum.searchSpectra({
                endpoint: 'search',
                query: this.query,
                page: currentPage,
                size: this.pagination.itemsPerPage
            }).pipe(first()).subscribe(this.searchSuccess, this.searchError);
        }
    }

    searchSuccess = (res: SpectrumModel[]) => {
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
            title: 'Unexpected Error Occurred During Search',
            body: `If this error continues to occur, please report the following error on YouTrack \n${error}`
        });
    }

    isSimilaritySearch() {
      if (this.location.path().split('?')[0] === '/spectra/similaritySearch') {
        return true;
      } else {
        return false;
      }
    }

    getFeedback(id): any {
      this.feedbackCache.resolveFeedback(id).pipe(first()).subscribe((res) => {
        return res;
      });
    }

    executeBatchDelete() {
      const modalRef = this.modalService.open(MassDeleteModalComponent, {scrollable: true});
      modalRef.result.then((x) => {
        if (x) {
          this.massDeletion.executeDeletion(this.authenticationService.getCurrentUser().accessToken).subscribe((res) => {
            this.toaster.pop({
              type: 'success',
              title: 'Batch Delete Executed',
              body: `Batch Delete was successful, refreshing page.`
            });
            setTimeout(() => {
              this.resetQuery();
            }, 2000);
          }, (error) => {
            this.toaster.pop({
              type: 'error',
              title: 'Problem Executing Batch Delete',
              body: `${error.message}`
            });
          });
        }
      });
    }

    queryUserSpectra() {
      this.spectraQueryBuilderService.prepareQuery();
      this.spectraQueryBuilderService.addUserToQuery(this.authenticationService.getCurrentUser().emailAddress);
      this.spectraQueryBuilderService.executeQuery();
    }
}
