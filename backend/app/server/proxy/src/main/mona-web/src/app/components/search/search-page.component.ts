/**
 * Created by nolanguzman on 10/31/2021
 */
import {Component} from '@angular/core';
import {faSearch} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'search',
    templateUrl: '../../views/spectra/query/search.html'
})
export class SearchComponent {
  readonly faSearch = faSearch;
}
