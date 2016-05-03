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
            $scope.instrumentType = [
                {EI: ['EI-B', 'EI-EBEB', 'GC-EI-QQ', 'GC-EI-TOF']},
                {ESI: ['CE-ESI-TOF', 'ESI-FTICR', 'ESI-ITFT', 'ESI-ITTOF',
                       'ESI-QTOF', 'HPLC-ESI-TOF', 'LC-ESI-IT', 'LC-ESI-ITFT',
                       'LC-ESI-ITTOF', 'LC-ESI-Q', 'LC-ESI-QFT', 'LC-ESI-QIT',
                       'LC-ESI-QQ', 'LC-ESI-QTOF', 'LC-ESI-TOF', 'UPLC-ESI-QTOF']},
                {Others: ['APCI-ITFT', 'APCI-ITTOF', 'CI-B', 'FAB-B', 'FAB-EB',
                          'FAB-EBEB', 'FD-B', 'FI-B', 'LC-APCI-Q', 'LC-APCI-QTOF',
                          'LC-APPI-QQ', 'MALDI-QIT', 'MALDI-TOF', 'MALDI-TOFTOF']}
            ];

            $scope.msType = ['All', 'MS', 'MS1', 'MS2', 'MS3', 'MS4'];
            $scope.ionMode = ['Positive','Negative', 'Both'];
        }

    }

})();
