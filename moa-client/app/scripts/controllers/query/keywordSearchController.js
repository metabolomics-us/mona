(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('KeywordSearchController', KeywordSearchController);

    /* @ngInject */
    function KeywordSearchController($scope, $log, rsqlService, $location, Spectrum) {

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
                    SI: [{name: 'Liquid Chromatography (LC)'},
                        {name: 'Gas Chromatography (GC)'},
                        {name: 'Direct Injection/Infusion (DI)'},
                        {name: 'Capillary ElectrophotetQueryis (CE)'}]
                },
                {
                    IM: [{name: 'Atmospheric PtetQuerysure Chemical Ionization (APCI)'},
                        {name: 'Chemical Ionization (CI)'},
                        {name: 'Electron Impact (EI)'},
                        {name: 'Electrospray Ionization (ESI)'},
                        {name: 'Fast Atom Bombardment (FAB)'},
                        {name: 'Matrix Assisted Laser Desorption Ionization (MALDI)'}]
                }
            ];

            $scope.msType = [{name: 'MS1'}, {name: 'MS2'}, {name: 'MS3'}, {name: 'MS4'}];
            $scope.ionMode = [{name: 'Positive'}, {name: 'Negative'}];
        })();



        $scope.submitQuery = function () {
            // add and filter query options, and update query cache
            //$scope.showSplash();

            rsqlService.filterKeywordSearchOptions($scope.queryOptions, $scope.instrumentType, $scope.msType, $scope.ionMode);

            var testQuery = encodeURIComponent('metaData=q=\'name=="ion mode" and value=="negative"\'');
            rsqlService.setQuery(testQuery);

            var query = rsqlService.getQuery();
            $log.info(query);
            if (query !== '') {
                $location.path('/spectra/browse');
            }

            //var response = Spectrum.searchSpectra({query: testQuery}, function(data) {
            //    $log.info(data);
            //});
            //$log.info(response);

            // show splash on submit
            // submit rest request
                // on success, route to spectra controller
                // how to pass data to SpectraController? that controller grabs query already,
                    // on submit, just save to queryCache
                        // display splash on browse route
                        // show splash on browser

        };

    }
})();
