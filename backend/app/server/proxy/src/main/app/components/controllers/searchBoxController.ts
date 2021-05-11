/**
 * Created by sajjan on 5/12/15.
 */

import * as angular from 'angular';

class SearchBoxController {
    private static $inject = ['$location', '$route', 'SpectraQueryBuilderService'];
    private $location;
    private $route;
    private SpectraQueryBuilderService;
    private inputError;

    constructor($location, $route, SpectraQueryBuilderService){
        this.$location = $location;
        this.$route = $route;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
    }

    $onInit() {
        this.inputError = false;
    }

    performSimpleQuery(searchBoxQuery){
        // Handle empty query
        if (angular.isUndefined(searchBoxQuery) || searchBoxQuery === '') {
            return;
        }

        searchBoxQuery = searchBoxQuery.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
        this.SpectraQueryBuilderService.prepareQuery();

        // Handle InChIKey
        if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(searchBoxQuery)) {
            this.SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', searchBoxQuery);
        }

        else if (/^[A-Z]{14}$/.test(searchBoxQuery)) {
            this.SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', searchBoxQuery, true);
        }

        // Handle SPLASH
        else if (/^splash[0-9]{2}/.test(searchBoxQuery)) {
            this.SpectraQueryBuilderService.addSplashToQuery(searchBoxQuery);
        }

        // Handle full text search
        else {
            this.SpectraQueryBuilderService.setTextSearch(searchBoxQuery);
        }

        this.SpectraQueryBuilderService.executeQuery();
    }

}


let SearchBoxComponent = {
    selector: "searchBox",
    templateUrl: "../../views/navbar/searchBox.html",
    bindings: {},
    controller: SearchBoxController
}

angular.module('moaClientApp')
    .component(SearchBoxComponent.selector, SearchBoxComponent)
