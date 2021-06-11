/**
 * Creates or updates a query with the given submitter information
 */

import * as angular from 'angular';

class SubmitterQueryDirective {
    constructor() {
        return {
            replace: true,
            transclude: true,
            templateUrl: '../../views/templates/query/submitterQuery.html',
            restrict: 'A',
            scope: {
                submitter: '=submitter'
            },
            controller: SubmitterQueryController,
            controllerAs: '$ctrl'
        };
    }
}

class SubmitterQueryController {
    private static $inject = ['$scope', 'SpectraQueryBuilderService', 'AuthenticationService'];
    private $scope;
    private SpectraQueryBuilderService;
    private AuthenticationService;

    constructor($scope, SpectraQueryBuilderService, AuthenticationService) {
        this.$scope = $scope;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.AuthenticationService = AuthenticationService;
    }

    /**
     * Create a new query based on the selected submitter
     */
    newQuery = () => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    };

    /**
     * Add selected submitter to the current query
     */
    addToQuery = () =>{
        this.SpectraQueryBuilderService.addUserToQuery(this.$scope.submitter.id);
        this.SpectraQueryBuilderService.executeQuery();
    };

    /**
     * Curate spectra based on selected submitter
     */
    curateSpectra = () => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.SpectraQueryBuilderService.addUserToQuery(this.$scope.submitter.id);

        let query = this.SpectraQueryBuilderService.getRSQLQuery();
        // TODO Add curation functionality
        // Spectrum.curateSpectraByQuery(query, function(data) {});
    }
}

angular.module('moaClientApp')
    .directive('submitterQuery', SubmitterQueryDirective);

