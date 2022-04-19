import {Component, OnInit, ViewChild} from '@angular/core';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {AuthenticationService} from '../../services/authentication.service';
import {faSearch, faPlusSquare, faTrash} from '@fortawesome/free-solid-svg-icons';
import {Metadata} from '../../services/persistence/metadata.resource';
import {NgbAccordion, NgbTypeahead} from '@ng-bootstrap/ng-bootstrap';
import {Observable, Subject, merge, OperatorFunction} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map} from 'rxjs/operators';
import {TagService} from '../../services/persistence/tag.resource';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'search-advanced',
  templateUrl: '../../views/spectra/query/searchAdvanced.html'
})
export class SearchAdvancedComponent implements OnInit {
  faSearch = faSearch;
  faPlusSquare = faPlusSquare;
  faTrash = faTrash;

  metadataResults;
  compoundMetadataResults;
  metadataOperators;
  compoundMetadataOperators;
  metadataCurrentSelection;
  compoundMetadataCurrentSelection;
  metadataOperatorSelection;
  compoundMetadataOperatorSelection;
  metadataValueSelection: any;
  compoundMetadataValueSelection;
  metaDataValueResults;
  compoundMetadataValueResults;
  metadataQueries;
  compoundMetadataQueries;

  libraryTags;
  queryTags;

  submitterToggle;
  noisyToggle;
  cleanToggle;

  @ViewChild('instance', {static: true}) instance: NgbTypeahead;
  @ViewChild('instance2', {static: true}) instance2: NgbTypeahead;

  @ViewChild('optionalTags') optionalTags: NgbAccordion;
  @ViewChild('queryTables') queryTables: NgbAccordion;

  focus$ = new Subject<string>();
  click$ = new Subject<string>();

  focus2$ = new Subject<string>();
  click2$ = new Subject<string>();

  constructor(public metadata: Metadata, public spectraQueryBuilderService: SpectraQueryBuilderService,
              public auth: AuthenticationService, public tagService: TagService, public logger: NGXLogger) {
    this.metadataValueSelection = '';
    this.compoundMetadataValueSelection = '';
    this.metadataQueries = [];
    this.compoundMetadataQueries = [];
  }

  ngOnInit() {
    this.spectraQueryBuilderService.prepareQuery();
    this.metadataResults = [];
    this.metaDataValueResults = [];
    this.compoundMetadataResults = [];
    this.compoundMetadataValueResults = [];
    this.queryTags = [];
    this.libraryTags = [];
    this.metadataOperators = [
      {value: '==', display: 'Equals'},
      {value: '!=', display: 'Not Equal'},
      {value: '=like=', display: 'Like'},
      {value: '=notlike=', display: 'Not Like'},
      {value: '=lt=', display: 'Less than'},
      {value: '=le=', display: 'Less than or Equal to'},
      {value: '=gt=', display: 'Greater than'},
      {value: '=ge=', display: 'Greater than or Equal to'}
    ];
    this.metadata.metaDataNames().subscribe((data) => {
      this.metadataResults = data.map((x) => {
        return {name: x.name, count: x.count};
      });
      this.metadataCurrentSelection = this.metadataResults[0];
      this.metadataOperatorSelection = this.metadataOperators[0];
      this.queryValues();
    });
    this.metadata.compoundMetaDataNames().subscribe((data) => {
      this.compoundMetadataResults = data.map((x) => {
        return {name: x.name, count: x.count};
      });
      this.compoundMetadataCurrentSelection = this.compoundMetadataResults[0];
      this.compoundMetadataOperatorSelection = this.metadataOperators[0];
      this.queryCompoundValues();
    });
    this.tagService.allTags().subscribe(
      (tags: any) => {
        if (tags.length > 0) {
          this.queryTags = tags.filter((x) => {
            return x.category !== 'library' && !x.ruleBased;
          });

          this.libraryTags = tags.filter((x) => {
            return x.category === 'library';
          });
        }
      },
      (error) => {
        this.logger.error('Tag pull failed: ' + error);
      }
    );
    this.submitterToggle = false;
    this.noisyToggle = false;
    this.cleanToggle = false;
  }

  submitQuery() {
    this.spectraQueryBuilderService.prepareQuery();
    if (this.metadataQueries.length > 0) {
      for (let i = 0; i < this.metadataQueries.length; i++) {
        this.spectraQueryBuilderService.addMetaDataToQuery(this.metadataQueries[i].metadata.name,
          this.metadataQueries[i].value, null, this.metadataQueries[i].operator.value);
      }
    }
    if (this.compoundMetadataQueries.length > 0) {
      for (let ii = 0; ii < this.compoundMetadataQueries.length; ii++) {
        this.spectraQueryBuilderService.addCompoundMetaDataToQuery(this.compoundMetadataQueries[ii].metadata.name,
          this.compoundMetadataQueries[ii].value, null, this.compoundMetadataQueries[ii].operator.value);
      }
    }
    if (this.submitterToggle) {
      this.spectraQueryBuilderService.addUserToQuery(this.auth.getCurrentUser().emailAddress);
    }

    if (this.cleanToggle) {
      this.spectraQueryBuilderService.addMetaDataToQuery('normalized entropy', '0.8', null, '=le=');
      this.spectraQueryBuilderService.addMetaDataToQuery('spectral entropy', '3.0', null, '=le=');
    }

    if (this.noisyToggle) {
      this.spectraQueryBuilderService.addMetaDataToQuery('normalized entropy', '0.8', null, '=gt=');
      this.spectraQueryBuilderService.addMetaDataToQuery('spectral entropy', '3.0', null, '=gt=');
    }

    // Handle library tags
    const libraryTags = this.libraryTags.reduce((result, element) => {
      if (element.selected) {
        result.push(element.text);
      }
      return result;
    }, []);

    const queryTags = this.queryTags.reduce((result, element) => {
      if (element.selected) {
        result.push(element.text);
      }
      return result;
    }, []);

    if (libraryTags.length > 0) {
      this.spectraQueryBuilderService.addTagToQuery(libraryTags, undefined);
    }

    if (queryTags.length > 0) {
      this.spectraQueryBuilderService.addTagToQuery(queryTags, undefined);
    }

    this.spectraQueryBuilderService.executeQuery();
  }

  searchMetadata: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.click$.pipe(filter(() => !this.instance.isPopupOpen()));
    const inputFocus$ = this.focus$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map(term => (term === '' ? this.metaDataValueResults
        : this.metaDataValueResults.filter(v => v.toLowerCase().indexOf(term.toLowerCase()) > -1)).slice(0, 10))
    );
  }

  searchCompoundMetadata: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.click2$.pipe(filter(() => !this.instance2.isPopupOpen()));
    const inputFocus$ = this.focus2$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map(term => (term === '' ? this.compoundMetadataValueResults
        : this.compoundMetadataValueResults.filter(v => v.toLowerCase().indexOf(term.toLowerCase()) > -1)).slice(0, 10))
    );
  }

  queryValues(): void {
    this.metadata.queryValues({name: this.metadataCurrentSelection.name, search: this.metadataValueSelection})
      .subscribe((res) => {
        this.metaDataValueResults = res.values.map((x) => {
          return x.value;
        });
      });
  }

  queryCompoundValues(): void {
    this.metadata.queryCompoundValues({name: this.compoundMetadataCurrentSelection.name, search: this.compoundMetadataValueSelection})
      .subscribe((res) => {
        this.compoundMetadataValueResults = res.values.map((x) => {
          return x.value;
        });
      });
  }

  addQuery(): void {
    this.metadataQueries.push({metadata: this.metadataCurrentSelection,
      operator: this.metadataOperatorSelection, value: this.metadataValueSelection});
    this.metadataValueSelection = '';
    if (!this.queryTables.isExpanded('metadataQueryTable')) {
      this.queryTables.expand('metadataQueryTable');
    }
  }

  addCompoundQuery(): void {
    this.compoundMetadataQueries.push({metadata: this.compoundMetadataCurrentSelection,
      operator: this.compoundMetadataOperatorSelection, value: this.compoundMetadataValueSelection});
    this.compoundMetadataValueSelection = '';
    if (!this.queryTables.isExpanded('compoundMetadataQueryTable')) {
      this.queryTables.expand('compoundMetadataQueryTable');
    }
  }

  removeQuery(i: number): void {
    this.metadataQueries.splice(i, 1);
  }

  removeCompoundQuery(i: number): void {
    this.compoundMetadataQueries.splice(i, 1);
  }

  resetQueries(){
    this.metadataQueries = [];
    this.metadataValueSelection = '';
    this.compoundMetadataQueries = [];
    this.compoundMetadataValueSelection = '';
  }
}
