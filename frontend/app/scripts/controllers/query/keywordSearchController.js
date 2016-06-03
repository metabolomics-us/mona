(function () {
    'use strict';

    angular.module('moaClientApp')
        .controller('KeywordSearchController', KeywordSearchController);

    /* @ngInject */
    function KeywordSearchController($scope, SpectraQueryBuilderService, queryStringBuilder, $log, $location) {

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
                        {name: 'Capillary Electrophoresis', abv: '(CE)'}]
                },
                {
                    IM: [{name: 'Atmospheric Pressure Chemical Ionization', abv: '(APCI)'},
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
            filterKeywordSearchOptions($scope.queryOptions, $scope.instrumentType, $scope.msType, $scope.ionMode);
            queryStringBuilder.buildQuery();
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

            filtered.operand = [];
            filtered.operand.push(options.firstOperand.toLowerCase());
            filtered.operand.push(options.secondOperand.toLowerCase());

            // filter compound
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(options.compound.name)) {
                filtered.compound.push({inchiKey: options.compound.name});
            }
            else {
                if(angular.isDefined(options.compound.name)) {
                    filtered.compound.push({name: options.compound.name});
                }
            }

            // filter class
            if (angular.isDefined(options.compound.className)) {
                filtered.compound.push({classification: options.compound.className});
            }

            filtered.compoundDa = [];
            // filter exact mass
            if (options.metadata.exactMass !== null) {
                filtered.compoundDa.push({'exact mass': options.metadata.exactMass});
                filtered.compoundDa.push({tolerance: options.metadata.tolerance});
            }

            // filter formula
            if (angular.isDefined(options.metadata.formula)) {
                filtered.formula = options.metadata.formula;
            }

            /**
             * our model for metadata fields. Elements in each property will be
             * created with 'or' operator and properties will be concat with 'and' operator
             */
            filtered.groupMeta = {
                'instrument type': [],
                'ion mode': [],
                'ms type': []
            };

            // filter instruments
            for (var i = 0; i < instruments.length; i++) {
                var curInstrument = instruments[i];
                for (var j in curInstrument) {
                    angular.forEach(curInstrument[j], function (value, key) {
                        if (value.selected === true)
                            filtered.groupMeta['instrument type'].push(value.name);
                    });
                }
            }

            // add ion mode
            angular.forEach(ionMode, function (value, key) {
                if (value.selected === true) {
                    filtered.groupMeta['ion mode'].push(value.name.toLowerCase());
                }
            });

            // add ms type to query
            angular.forEach(ms, function (value, key) {
                if (value.selected === true) {
                    filtered.groupMeta['ms type'].push(value.name);
                }
            });
            SpectraQueryBuilderService.setQuery(filtered);
        }

    }
})();
