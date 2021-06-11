/**
 * links a metadata field to a query builder and executes the spectra query for us
 */
import * as angular from 'angular';

class MetadataQueryDirective {
    constructor() {
        return {
            restrict: 'A',
            templateUrl: '../../views/templates/query/metadataQuery.html',
            replace: true,
            transclude: true,
            scope: {
                metaData: '=value',
                compound: '=compound',
                classification: '=classification'
            },
            controller: MetadataQueryController,
            controllerAs: '$ctrl'
        };
    }
}

class MetadataQueryController {
    private static $inject = ['$scope', 'SpectraQueryBuilderService', '$location', '$log'];
    private $scope;
    private SpectraQueryBuilderService;
    private $location;
    private $log;

    constructor($scope, SpectraQueryBuilderService, $location, $log) {
        this.$scope = $scope;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.$location = $location;
        this.$log = $log;
    }

    /**
     * Create a new query based on the selected metadata value
     */
    newQuery = () => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    };

    /**
     * Add selected metadata value to the current query
     */
    addToQuery = () => {
        if (angular.isDefined(this.$scope.compound)) {
            this.SpectraQueryBuilderService.addCompoundMetaDataToQuery(this.$scope.metaData.name, this.$scope.metaData.value);
        } else if (angular.isDefined(this.$scope.classification)) {
            this.SpectraQueryBuilderService.addClassificationToQuery(this.$scope.metaData.name, this.$scope.metaData.value);
        } else {
            this.SpectraQueryBuilderService.addMetaDataToQuery(this.$scope.metaData.name, this.$scope.metaData.value);
        }

        this.SpectraQueryBuilderService.executeQuery();
    };
}

angular.module('moaClientApp')
    .directive('metadataQuery', MetadataQueryDirective);
