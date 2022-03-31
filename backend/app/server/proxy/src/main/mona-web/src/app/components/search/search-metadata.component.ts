import {Component, OnInit, ViewChild} from '@angular/core';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {faSearch, faPlusSquare, faTrash} from '@fortawesome/free-solid-svg-icons';
import {Metadata} from '../../services/persistence/metadata.resource';
import {NgbTypeahead} from '@ng-bootstrap/ng-bootstrap';
import {Observable, Subject, merge, OperatorFunction} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map} from 'rxjs/operators';

@Component({
  selector: 'search-metadata',
  templateUrl: '../../views/spectra/query/searchMetadata.html'
})
export class SearchMetadataComponent implements OnInit {
  faSearch = faSearch;
  faPlusSquare = faPlusSquare;
  faTrash = faTrash;

  metadataResults;
  metadataOperators;
  metadataCurrentSelection;
  metadataOperatorSelection;
  metadataValueSelection: any;
  metaDataValueResults;
  metadataQueries;

  @ViewChild('instance', {static: true}) instance: NgbTypeahead;
  focus$ = new Subject<string>();
  click$ = new Subject<string>();

  constructor(public metadata: Metadata) {
    this.metadataValueSelection = '';
    this.metadataQueries = [];
  }

  ngOnInit() {
    this.metadataResults = [];
    this.metaDataValueResults = [];
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
    this.metadataOperatorSelection = this.metadataOperators[0];
    this.metadata.metaDataNames().subscribe((data) => {
      this.metadataResults = data.map((x) => {
        return x.name;
      });
      this.metadataCurrentSelection = data[0].name;
      this.queryValues();
    });
  }

  submitQuery() {
    console.log('submitted');
  }

  search: OperatorFunction<string, readonly string[]> = (text$: Observable<string>) => {
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    const clicksWithClosedPopup$ = this.click$.pipe(filter(() => !this.instance.isPopupOpen()));
    const inputFocus$ = this.focus$;

    return merge(debouncedText$, inputFocus$, clicksWithClosedPopup$).pipe(
      map(term => (term === '' ? this.metaDataValueResults
        : this.metaDataValueResults.filter(v => v.toLowerCase().indexOf(term.toLowerCase()) > -1)).slice(0, 10))
    );
  }

  queryValues(): void {
    this.metadata.queryValues({name: this.metadataCurrentSelection, search: this.metadataValueSelection})
      .subscribe((res) => {
        this.metaDataValueResults = res.values.map((x) => {
          return x.value;
        });
      });
  }

  addQuery(): void {
    this.metadataQueries.push({metadata: this.metadataCurrentSelection,
      operator: this.metadataOperatorSelection, value: this.metadataValueSelection});
    this.metadataValueSelection = '';
  }

  removeQuery(i: number): void {
    this.metadataQueries.splice(i, 1);
  }

  resetQueries(){
    this.metadataQueries = [];
    this.metadataValueSelection = '';
  }
}
