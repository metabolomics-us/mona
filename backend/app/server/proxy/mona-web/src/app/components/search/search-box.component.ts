/**
 * Created by sajjan on 5/12/15.
 */
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Component, OnInit} from "@angular/core";

@Component({
    selector: 'search-box',
    templateUrl: '../../views/navbar/searchBox.html'
})
export class SearchBoxComponent implements OnInit {
    public inputError;
    public searchBoxQuery;

    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService){}

    ngOnInit() {
        this.inputError = false;
    }

    performSimpleQuery(query){
        console.log(query);
        // Handle empty query
        if (typeof query === 'undefined' || query === '') {
            return;
        }

        query = query.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        this.spectraQueryBuilderService.prepareQuery();

        // Handle InChIKey
        if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(query)) {
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', query, undefined);
        }

        else if (/^[A-Z]{14}$/.test(query)) {
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', query, true);
        }

        // Handle SPLASH
        else if (/^splash[0-9]{2}/.test(query)) {
            this.spectraQueryBuilderService.addSplashToQuery(query);
        }

        // Handle full text search
        else {
            this.spectraQueryBuilderService.setTextSearch(query);
        }
        this.searchBoxQuery = '';
        this.spectraQueryBuilderService.executeQuery();
    }

}
