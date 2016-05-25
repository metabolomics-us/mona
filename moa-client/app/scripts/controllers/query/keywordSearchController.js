(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('KeywordSearchController', KeywordSearchController);

    /* @ngInject */
    function KeywordSearchController($scope, $log, rsqlService, $location) {

        (function initForm() {
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
                    SI: [{name: 'Liquid Chromatography', abv: '(LC)'},
                        {name: 'Gas Chromatography', abv: '(GC)'},
                        {name: 'Direct Injection/Infusion', abv: '(DI)'},
                        {name: 'Capillary ElectrophotetQueryis', abv: '(CE)'}]
                },
                {
                    IM: [{name: 'Atmospheric PtetQuerysure Chemical Ionization', abv: '(APCI)'},
                        {name: 'Chemical Ionization', abv: '(CI)'},
                        {name: 'Electron Impact', abv: '(EI)'},
                        {name: 'Electrospray Ionization', abv: '(ESI)'},
                        {name: 'Fast Atom Bombardment', abv: '(FAB)'},
                        {name: 'Matrix Assisted Laser Desorption Ionization', abv: '(MALDI)'}]
                }
            ];

            $scope.msType = [{name: 'MS1'}, {name: 'MS2'}, {name: 'MS3'}, {name: 'MS4'}];
            $scope.ionMode = [{name: 'Positive'}, {name: 'Negative'}];
        })();


        $scope.submitQuery = function () {
            rsqlService.filterKeywordSearchOptions($scope.queryOptions, $scope.instrumentType, $scope.msType, $scope.ionMode);
            var query = rsqlService.getQuery();
            $location.path('/spectra/browse');
        };

    }
})();
