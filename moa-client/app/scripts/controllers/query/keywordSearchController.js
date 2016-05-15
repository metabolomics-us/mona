
(function() {
    'use strict';

    angular.module('moaClientApp')
        .controller('KeywordSearchController', KeywordSearchController);

    /* @ngInject */
    function KeywordSearchController($scope, $log, $http, rsqlService) {

        //TODO: query object needs to be initialized in a QueryBuilderService
        initForm();

        function initForm() {
            $scope.queryOptions = {
                firstOperand: 'AND',
                secondOperand: 'AND',
                compound: {
                    inchiKey: null
                },
                metadata: {
                    insType: [],
                    msType: [],
                    ionMode: [],
                    exactMass: null,
                    tolerance: 0.5
                }
            };

            $scope.instrumentType = [
                {
                    EI: [{name: 'EI-B'}, {name: 'EI-EBEB'}, {name: 'GC-EI-QQ'}, {name: 'GC-EI-TOF'}]
                },
                {
                    ESI: [{name: 'CE-ESI-TOF'}, {name: 'ESI-FTICR'}, {name: 'ESI-ITFT'}, {name: 'ESI-ITTOF'},
                        {name: 'ESI-QTOF'}, {name: 'HPLC-ESI-TOF'}, {name: 'LC-ESI-IT'}, {name: 'LC-ESI-ITFT'},
                        {name: 'LC-ESI-ITTOF'}, {name: 'LC-ESI-Q'}, {name: 'LC-ESI-QFT'}, {name: 'LC-ESI-QIT'},
                        {name: 'LC-ESI-QQ'}, {name: 'LC-ESI-QTOF'}, {name: 'LC-ESI-TOF'}, {name: 'UPLC-ESI-QTOF'}]
                },
                {
                    Others: [{name: 'APCI-ITFT'}, {name: 'APCI-ITTOF'}, {name: 'CI-B'}, {name: 'FAB-B'},
                        {name: 'FAB-EB'}, {name: 'FAB-EBEB'}, {name: 'FD-B'}, {name: 'FI-B'},
                        {name: 'LC-APCI-Q'}, {name: 'LC-APCI-QTOF'}, {name: 'LC-APPI-QQ'},
                        {name: 'MALDI-QIT'}, {name: 'MALDI-TOF'}, {name: 'MALDI-TOFTOF'}]
                }
            ];

            $scope.msType = [{name: 'MS'}, {name: 'MS1'}, {name: 'MS2'}, {name: 'MS3'}, {name: 'MS4'}];
            $scope.ionMode = [{name: 'Positive'}, {name: 'Negative'}];
        }

        /**
         * handles when user check select all in UI. Our implementation in searchForm.html
         * uses ng-model and ng-change. Since ng-change will updates the 'selected' property
         * of the instrument name, we do not need to update on single selection. When user
         * clicks submit, we will loop through instrument type, and add selected==true to query
         */
        $scope.insTypeSelectAll = function (index, insCategory) {
            var curIns = $scope.instrumentType[index];

            angular.forEach(curIns[insCategory], function (value, key) {
                value.selected = curIns.selectAll;
            });
        };


        $scope.resetForm = function() {
            initForm();
        };

        $scope.submitQuery = function () {
            // add and filter query options, and update query cache
            rsqlService.filterKeywordSearchOptions($scope.queryOptions, $scope.instrumentType, $scope.msType, $scope.ionMode);
            $scope.query = rsqlService.getQuery();

            $log.info($scope.query);

            // filter query
            // service will build query,
            // directive submits rsql query string
            // on success change location to browse, with results
            // how to get data to browse controller?
            // use rsqlService to store the data?
            // then on browse resolve data?

            /** RESET FORM AFTER WE SUBMIT QUERY*/
            //TODO: store query in Cache, unless user click submit again, clear query
            $scope.resetForm();


        };

    }

})();
