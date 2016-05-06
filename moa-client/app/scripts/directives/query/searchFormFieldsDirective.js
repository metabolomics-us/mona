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

            $scope.selectedInstruments = [];

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

            $scope.msType = ['MS', 'MS1', 'MS2', 'MS3', 'MS4'];
            $scope.ionMode = ['Positive', 'Negative'];


            /**
             * handles when user check select all in UI. Our implementation in searchForm.html
             * uses ng-model and ng-change. Since ng-change will updates the 'selected' property
             * of the instrument name, we do not need to update on single selection. When user
             * clicks submit, we will loop through instrument type, and add selected==true to query
             */
            $scope.isSelectAll = function (index, insCategory) {
                var curIns = $scope.instrumentType[index];

                angular.forEach(curIns[insCategory], function (value, key) {
                    $log.info(curIns.selectAll);
                    value.selected = curIns.selectAll;
                });
            };

            //TODO on Submit, loop through instrumentType aggregate SelectALl && name if selected !== undefined && true
            // implement single check
            $scope.toggleSelection = function (insName, index, insCategory) {
                $log.info($scope.instrumentType[index][insCategory]);

            };

        }

    }

})();
