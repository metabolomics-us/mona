(function () {
    'use strict';

    angular.module('moaClientApp')
        .directive('searchFormFields', searchFormFields);

    function searchFormFields() {
        var directive = {
            templateUrl: 'views/spectra/query/searchForm.html',
            controller: fieldsController
        };
        return directive;

        function fieldsController($scope, $log) {

            //TODO on Submit, loop through instrumentType aggregate SelectALl && name if selected !== undefined && true

            $scope.query = {
                compound: {
                    firstOperator: 'AND',
                    secondOperator: 'AND'
                },
                insType: {},
                msType: {},
                ionMode: {}
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

            $scope.msType = [{name: 'MS', selected: true}, {name: 'MS1', selected: true}, {name: 'MS2', selected: true},
                {name: 'MS3', selected: true}, {name: 'MS4', selected: true}];
            $scope.msType.selectAll = true;

            $scope.ionMode = [{name: 'Positive'}, {name: 'Negative'}];


            /**
             * handles when user check select all in UI. Our implementation in searchForm.html
             * uses ng-model and ng-change. Since ng-change will updates the 'selected' property
             * of the instrument name, we do not need to update on single selection. When user
             * clicks submit, we will loop through instrument type, and add selected==true to query
             */
            $scope.insTypeSelectAll = function (index, insCategory) {
                var curIns = $scope.instrumentType[index];

                angular.forEach(curIns[insCategory], function (value, key) {
                    $log.info(curIns.selectAll);
                    value.selected = curIns.selectAll;
                });
            };


            $scope.msIonSelectAll = function (selection) {
                angular.forEach(selection, function (value, key) {
                    value.selected = selection.selectAll;
                });
                $log.info($scope.ionMode);
            };

            $scope.submitQuery = function() {
                // get query options
                // get instrument types
                // get ms & ion
            };


        }

    }

})();
