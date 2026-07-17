/**
 * Created by sajjan on 5/12/15.
 * Updated by nolanguzman on 10/31/2021
 */
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {faSearch} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'search-box',
    templateUrl: '../../views/navbar/searchBox.html'
})
export class SearchBoxComponent implements OnInit {
    // Keyword searches below this length cannot use the trigram indexes and are rejected
    static readonly MIN_KEYWORD_LENGTH = 3;

    inputError;
    searchBoxQuery;
    faSearch = faSearch;

    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService, public router: Router){}

    ngOnInit() {
        this.inputError = false;
    }

    performSimpleQuery(query) {
        // Handle empty query
        if (typeof query === 'undefined' || query === '') {
            return;
        }

        query = query.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        this.inputError = false;

        // Handle InChIKey
        if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(query)) {
            this.spectraQueryBuilderService.prepareQuery();
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', query, undefined);
        }

        else if (/^[A-Z]{14}$/.test(query)) {
            this.spectraQueryBuilderService.prepareQuery();
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', query, true);
        }

        // Handle SPLASH
        else if (/^splash[0-9]{2}/.test(query)) {
            this.spectraQueryBuilderService.prepareQuery();
            this.spectraQueryBuilderService.addSplashToQuery(query);
        }

        // Handle full text search via the trigram backed keyword endpoint, since the generic
        // filter path cannot answer a contains search across the joined tables efficiently
        else {
            if (query.length < SearchBoxComponent.MIN_KEYWORD_LENGTH) {
                this.inputError = true;
                return;
            }

            this.searchBoxQuery = '';
            this.router.navigate(['/spectra/browse'], {queryParams: {keyword: query}}).then();
            return;
        }
        this.searchBoxQuery = '';
        this.spectraQueryBuilderService.executeQuery();
    }

}
