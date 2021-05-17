import * as angular from 'angular';

class AdvancedSearchController{
    private static $inject = ['$scope', 'SpectraQueryBuilderService', 'queryStringBuilder', '$location', '$log'];

    private $scope;
    private SpectraQueryBuilderService;
    private queryStringBuilder;
    private $location;
    private $log;
    private queryStrings;
    private compoundQuery;
    private metadataQuery;

    constructor($scope, SpectraBuilderService, queryStringBuilder, $location, $log){
        this.$scope = $scope;
        this.SpectraQueryBuilderService = SpectraBuilderService;
        this.queryStringBuilder = queryStringBuilder;
        this.$location = $location;
        this.$log = $log;
    }

    $onInit = () => {
        this.initForm();
        this.queryStrings = [];
    }

    initForm() {
        this.compoundQuery = {
            metadata: [],
            operator: ['AND', 'AND', 'AND'],
            exactMass: null,
            tolerance: 0.5
        };

        this.metadataQuery = {
            metadata: [],
            operator: 'AND',
            exactMass: null,
            tolerance: 0.5
        };
    }

    submitAdvQuery =  () => {
        this.filterQueryOptions();
        this.queryStringBuilder.buildAdvanceQuery();
        this.$location.path('/spectra/browse');
    };

    filterQueryOptions() {

        let compound = this.compoundQuery;
        let metaData = this.metadataQuery;

        let filtered = this.SpectraQueryBuilderService.prepareQuery();

        // store operators
        filtered.operand = {
            metadata: [metaData.operator.toLowerCase()],
            compound: []
        };

        for (let i = 0; i < compound.operator.length; i++) {
            filtered.operand.compound.push(compound.operator[i].toLowerCase());
        }

        // filter compound name or inchikey
        if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(compound.name)) {
            filtered.compound.push({inchiKey: compound.name})
        }
        else if (/^[A-Z]{14}$/.test(compound.name)) {
            filtered.compound.push({partInchi: compound.name});
        }
        else {
            if (angular.isDefined(compound.name)) {
                filtered.compound.push({name: compound.name});
            }
        }

        // filter class
        if (angular.isDefined(compound.className)) {
            filtered.compound.push({classification: compound.className});
        }

        // filter compound metadata
        filtered.compound.metadata = this.addMetaData(compound.metadata);


        // filter compound measurement
        if (compound.exactMass !== null) {
            filtered.compoundDa = [{'exact mass': compound.exactMass}, {tolerance: compound.tolerance}];
        }


        // filter metadata
        filtered.metadata = this.addMetaData(metaData.metadata);


        //filter metadata measurement
        if (metaData.exactMass !== null) {
            filtered.metadataDa = [{'exact mass': metaData.exactMass, tolerance: metaData.tolerance}];
        }

        this.SpectraQueryBuilderService.setQuery(filtered);
    }


    addMetaData(metadata) {
        let addedMeta = [];
        for (let i = 0, l = metadata.length; i < l; i++) {
            let curMeta = metadata[i];
            if (curMeta.name !== '' && curMeta.value !== '' && angular.isDefined(curMeta.selected)) {
                let inputMetaValues = {
                    name: curMeta.name,
                    operator: curMeta.selected.value,
                    value: curMeta.value,
                    tolerance: Number
                };

                if (angular.isDefined(curMeta.tolerance)) {
                    inputMetaValues.tolerance = curMeta.tolerance;
                }

                addedMeta.push(inputMetaValues);
            }
        }
        return addedMeta;
    }

}

let AdvancedSearchComponent = {
    selector: "advancedSearch",
    bindings: {},
    controller: AdvancedSearchController
}

angular.module('moaClientApp')
    .component(AdvancedSearchComponent.selector, AdvancedSearchComponent);

