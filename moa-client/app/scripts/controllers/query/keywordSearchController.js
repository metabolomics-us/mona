(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('KeywordSearchController', KeywordSearchController);

    /* @ngInject */
    function KeywordSearchController($scope, $log, $http, $timeout, rsqlService) {

        $scope.showForm = false;
        $scope.searchSplash = false;

        $scope.query = {};

        initForm();
        function initForm() {
            $scope.showForm = true;
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
                        {name: 'Capillary Electrophoresis (CE)'}]
                },
                {
                    IM: [{name: 'Atmospheric Pressure Chemical Ionization (APCI)'},
                        {name: 'Chemical Ionization (CI)'},
                        {name: 'Electron Impact (EI)'},
                        {name: 'Electrospray Ionization (ESI)'},
                        {name: 'Fast Atom Bombardment (FAB)'},
                        {name: 'Matrix Assisted Laser Desorption Ionization (MALDI)'}]
                }
            ];

            $scope.msType = [{name: 'MS1'}, {name: 'MS2'}, {name: 'MS3'}, {name: 'MS4'}];
            $scope.ionMode = [{name: 'Positive'}, {name: 'Negative'}];
        }

        $scope.hideSplash = function () {
            $timeout(function () {
                $scope.searchSplash = false;
            }, 1000)
        };

        $scope.showSplash = function () {
            $scope.searchSplash = true;
        };


        $scope.submitQuery = function () {
            // add and filter query options, and update query cache
            $scope.showSplash();
            rsqlService.filterKeywordSearchOptions($scope.queryOptions, $scope.instrumentType, $scope.msType, $scope.ionMode);
            $scope.query = rsqlService.getQuery();
            var res = encodeURIComponent('metaData=q=\'name=="ion mode" and value=="negative"\'');
            $log.info(res);

            var start = new Date().getTime();
            $http({
                method: 'GET',
                url: 'http://0.0.0.0:9292/cream.fiehnlab.ucdavis.edu:8080/rest/spectra/search?query=' + res
            }).then(function(response) {
                $log.log('success');
                $log.info(response);
                $scope.hideSplash();
                var end = new Date().getTime();
                $log.warn(end - start);
            }, function(response) {
                $log.log('fail');
                $log.info(response);

                var end = new Date().getTime();
                $log.warn(end - start);
            });


            // filter query
            // service will build query,
            // directive submits rsql query string
            // on success change location to browse, with results
            // how to get data to browse controller?
            // use rsqlService to store the data?
            // then on browse resolve data?

            /** RESET FORM AFTER WE SUBMIT QUERY*/
                //TODO: store query in Cache, unless user click submit again, clear query
            //$scope.hideSplash();

        };

    }
})();
