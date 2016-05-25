(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('KeywordSearchController', KeywordSearchController);

    /* @ngInject */
    function KeywordSearchController($scope, $log, rsqlService, $location, SpectraQueryBuilderService) {

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
            // build a query object
            // save it to cache
            // build query string
                // save it to cache
            // change location

            /*** DATA MODEL & Work Flow
             build a query schema
             query = {
                compound: [{name, value}, {inchikey: value}]
                metadata: [{name,value}]
                tags: []
            }
             addMetadata to query
             get metadata array, append metadata, build string

             removeMetadata
             get metadata array, remove index of, build string

             I need to keep the query array of object in a service, and build the rsqlQuery string from that object

             filter out search form
             create query object
             save to cache
             build string

             */
            filterKeywordSearchOptions($scope.queryOptions, $scope.instrumentType, $scope.msType, $scope.ionMode);
            var query = rsqlService.getQuery();
            $location.path('/spectra/browse');
        };


        /**
         * filter user input and add it to query cache
         * @param options
         * @param instruments
         * @param ms
         * @param ionMode
         */
        function filterKeywordSearchOptions(options, instruments, ms, ionMode) {

            var filtered = SpectraQueryBuilderService.prepareQuery();

            // filter compound
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(options.compound.name)) {
                options.compound.inchiKey = options.compound.name;
                delete options.compound.name;
            }
            else {
                delete options.compound.inchiKey;
            }

            // filter exact mass
            if (options.metadata.exactMass === null) {
                delete options.metadata.tolerance;
                delete options.metadata.exactMass;
            }

            // filter instruments
            for (var i = 0; i < instruments.length; i++) {
                var curInstrument = instruments[i];
                for (var j in curInstrument) {
                    angular.forEach(curInstrument[j], function (value, key) {
                        if (value.selected === true)
                            options.metadata.insType.push(value.name);
                    });

                }
            }

            // add ion mode
            angular.forEach(ionMode, function (value, key) {
                if (value.selected === true) {
                    options.metadata.ionMode.push(value.name);
                }
            });

            // add ms type to query
            angular.forEach(ms, function (value, key) {
                if (value.selected === true) {
                    options.metadata.msType.push(value.name);
                }
            });

            // remove empty fields
            if (typeof(options.metadata.insType) !== 'undefined' && options.metadata.insType.length === 0) {
                delete options.metadata.insType;
            }

            if (typeof(options.metadata.msType) !== 'undefined' && options.metadata.msType.length === 0) {
                delete options.metadata.msType;
            }

            if (typeof(options.metadata.ionMode) !== 'undefined' && options.metadata.ionMode.length === 0) {
                delete options.metadata.ionMode;
            }
            $log.info(options);
            buildRsqlQuery(options);
        }

    }
})();
