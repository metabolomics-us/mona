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

        function fieldsController($scope) {
            $scope.selectAll ={
                EI: false
            };

            $scope.instrumentType = [
                {EI: ['EI-B', 'EI-EBEB', 'GC-EI-QQ', 'GC-EI-TOF'], selected: []},
                {ESI: ['CE-ESI-TOF', 'ESI-FTICR', 'ESI-ITFT', 'ESI-ITTOF',
                       'ESI-QTOF', 'HPLC-ESI-TOF', 'LC-ESI-IT', 'LC-ESI-ITFT',
                       'LC-ESI-ITTOF', 'LC-ESI-Q', 'LC-ESI-QFT', 'LC-ESI-QIT',
                       'LC-ESI-QQ', 'LC-ESI-QTOF', 'LC-ESI-TOF', 'UPLC-ESI-QTOF']},
                {Others: ['APCI-ITFT', 'APCI-ITTOF', 'CI-B', 'FAB-B', 'FAB-EB',
                          'FAB-EBEB', 'FD-B', 'FI-B', 'LC-APCI-Q', 'LC-APCI-QTOF',
                          'LC-APPI-QQ', 'MALDI-QIT', 'MALDI-TOF', 'MALDI-TOFTOF']}
            ];


            $scope.msType = ['MS', 'MS1', 'MS2', 'MS3', 'MS4'];
            $scope.selectMS = true;

            $scope.ionMode = ['Positive','Negative'];
            $scope.selectIon = true;

            $scope.selectInstrumentType = function(index, instrument) {

                if ($scope.selectAll.EI === true) {
                    $scope.instrumentType[0].selected = $scope.instrumentType[0].EI;
                }


                else {
                    var idx = $scope.instrumentType[index].selected.indexOf(instrument);

                    if (idx > -1) {
                        $scope.instrumentType[index].selected.splice(idx, 1);
                    }
                    else {
                        $scope.instrumentType[index].selected.push(instrument);
                    }
                }

                //console.log($scope.selectAll.EI);
                console.log($scope.instrumentType[index].selected);
                //console.log($scope.instrumentType[index].EI);
                //{{instrumentType[0].EI.selected.indexOf(n) > -1}}
            }
        }

    }

})();
