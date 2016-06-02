(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('AdvancedSearchController', AdvancedSearchController);

    /* @ngInject */
    function AdvancedSearchController($scope, SpectraQueryBuilderService, queryStringBuilder, $location, $log) {

        initForm();
        $scope.queryStrings = [];

        function initForm() {
            $scope.compoundQuery = {
                metadata: [],
                operator: ['AND', 'AND', 'AND'],
                exactMass: null,
                tolerance: 0.5
            };

            $scope.metadataQuery = {
                metadata: [],
                operator: 'AND',
                exactMass: null,
                tolerance: 0.5
            };
        }


        $scope.submitAdvQuery = function () {

            filterQueryOptions();
            queryStringBuilder.buildAdvanceQuery();
            // save query
            // change location
        };

        function filterQueryOptions() {

            var compound = $scope.compoundQuery;
            var metaData = $scope.metadataQuery;

            var filtered = SpectraQueryBuilderService.prepareQuery();

            // store operators
            filtered.operand = {meta: metaData.operator.toLowerCase()};
            filtered.operand.compound = [];

            for (var i = 0; i < compound.operator.length; i++) {
                filtered.operand.compound.push(compound.operator[i].toLowerCase());
            }

            // filter class
            if (angular.isDefined(compound.className)) {
                filtered.compound.push({classification: compound.className});
            }

            // filter compound name or inchikey
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(compound.name)) {
                filtered.compound.push({inchiKey: compound.name})
            }

            // filter compound metadata
            filtered.compoundMetada = addMetaData(compound.metadata);


            // filter compound measurement
            if (compound.exactMass !== null) {
                filtered.compoundDa = {exactMass: compound.exactMass, tolerance: compound.tolerance};
            }


            // filter metadata
            filtered.metadata = addMetaData(metaData.metadata);


            //filter metadata measurement
            if (metaData.exactMass !== null) {
                filtered.metaDa = {exactMass: metaData.exactMass, tolerance: metaData.tolerance};
            }

            SpectraQueryBuilderService.setQuery(filtered);
        }


        function addMetaData(metadata) {
            var addedMeta = [];
            for (var i = 0, l = metadata.length; i < l; i++) {
                var curMeta = metadata[i];
                if (curMeta.name !== '' && curMeta.value !== '' && angular.isDefined(curMeta.selected)) {
                    addedMeta.push({
                        name: curMeta.name,
                        operator: curMeta.selected.value,
                        value: curMeta.value
                    });
                }
            }
            return addedMeta;
        }
    }
})();
