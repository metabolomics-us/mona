/**
 * Created by wohlgemuth on 7/11/14.
 */

import * as angular from 'angular';

class QuerySpectrumModalController{
    private static $inject = ['$scope', '$uibModalInstance', 'SpectraQueryBuilderService'];

    private $scope;
    private $uibModalInstance;
    private SpectraQueryBuilderService;
    private queryAccordion;
    private selectedTags;
    private metadataQuery;
    private query;

    constructor($scope, $uibModalInstance, SpectraQueryBuilderService){
        this.$scope = $scope;
        this.$uibModalInstance = $uibModalInstance;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
    }

    $onInit = () => {
        /**
         * Store accordion status
         * @type {{name: boolean}}
         */
        this.queryAccordion = {name: true};

        /**
         * Tags selected in query window
         * @type {{}}
         */
        this.selectedTags = {};

        /**
         * Store all metadata query data
         * @type {{name: string, value: string}[]}
         */
        this.metadataQuery = [];


        /**
         * contains our build query object
         * @type {{}}
         */
        this.query = {};
    }

    cancelDialog = () => {
        this.$uibModalInstance.dismiss('cancel');
    };

    /**
     * closes the dialog and finishes and builds the query
     */
    submitQuery = () => {

        //compile initial query
        this.SpectraQueryBuilderService.compileQuery(this.query);

        //refine by metadata
        for (let i = 0, l = this.metadataQuery.length; i < l; i++) {
            this.SpectraQueryBuilderService.addMetaDataToQuery(this.metadataQuery[i]);
        }

        //add tags to query
        for (let key in this.selectedTags) {
            if (this.selectedTags.hasOwnProperty(key) && this.selectedTags[key] !== false) {
                this.SpectraQueryBuilderService.addTagToQuery(key, false, this.selectedTags[key]);
            }
        }

        //submit the final query
        this.$uibModalInstance.close(this.SpectraQueryBuilderService.getQuery());
    };
}

let QuerySpectrumModalComponent = {
    selector: "querySpectrumModal",
    bindings: {},
    controller: QuerySpectrumModalController
}

angular.module('moaClientApp')
    .controller(QuerySpectrumModalComponent.selector, QuerySpectrumModalComponent);


