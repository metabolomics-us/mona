(function () {
    'use strict';

    KeywordSearchController.$inject = ['$scope', 'SpectraQueryBuilderService', 'queryStringBuilder', '$log', '$location', 'QueryCache'];
    angular.module('moaClientApp')
        .controller('KeywordSearchController', KeywordSearchController);

    /* @ngInject */
    function KeywordSearchController($scope, SpectraQueryBuilderService, queryStringBuilder, $log, $location, QueryCache) {

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

            $scope.sourceIntroduction = [
                {name: 'Liquid Chromatography', abv: 'LC'},
                {name: 'Gas Chromatography', abv: 'GC'},
                {name: 'Capillary Electrophoresis', abv: 'CE'}
            ];


            $scope.instrumentType = [
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
            // TODO improve query building routines
            filterKeywordSearchOptions($scope.queryOptions, $scope.sourceIntroduction, $scope.msType, $scope.ionMode);
            queryStringBuilder.buildQuery();

            var chromatography = [];

            angular.forEach($scope.sourceIntroduction, function (value, key) {
                if (value.selected === true) {
                    chromatography.push(value.abv);
                }
            });

            if (chromatography.length > 0) {
                var queryString = QueryCache.getSpectraQuery('string');

                if (queryString == "/rest/spectra")
                    queryString = "";

                if (queryString != "")
                    queryString = queryString + " and ";

                if (chromatography.length > 1)
                    queryString = queryString + "(";

                for (var i = 0; i < chromatography.length; i++) {
                    if (i > 0)
                        queryString = queryString + " or ";
                    queryString = queryString + "tags=q='text==\"" + chromatography[i] + '-MS\"\''
                }

                if (chromatography.length > 1)
                    queryString = queryString + ")";
                QueryCache.setSpectraQueryString(queryString);
            }

            $location.path('/spectra/browse');
        };


        /**
         * filter user input and add it to query cache
         * @param options
         * @param instruments
         * @param ms
         * @param ionMode
         */
        function filterKeywordSearchOptions(options, sourceIntroduction, ms, ionMode) {

            var filtered = SpectraQueryBuilderService.prepareQuery();

            filtered.operand = [];
            filtered.operand.push(options.firstOperand.toLowerCase());
            filtered.operand.push(options.secondOperand.toLowerCase());

            // filter compound
            if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(options.compound.name)) {
                filtered.compound.push({inchiKey: options.compound.name});
            }
            else if (/^[A-Z]{14}$/.test(options.compound.name)) {
                filtered.compound.push({partInchi: options.compound.name});
            }
            else {
                if (angular.isDefined(options.compound.name)) {
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
                filtered.compoundDa.push({'total exact mass': options.metadata.exactMass});
                filtered.compoundDa.push({tolerance: options.metadata.tolerance});
            }

            // filter formula
            if (angular.isDefined(options.metadata.formula)) {
                filtered['molecular formula'] = options.metadata.formula;
            }

            /**
             * our model for metadata fields. Elements in each property will be
             * created with 'or' operator and properties will be concat with 'and' operator
             */
            filtered.groupMeta = {
                // 'source introduction': [],
                'ion mode': [],
                'ms level': []
            };

            // add source introduction
            // angular.forEach(sourceIntroduction, function (value, key) {
            //     if (value.selected === true) {
            //         filtered.groupMeta['source introduction'].push(value.name.toLowerCase());
            //     }
            // });

            // add ion mode
            angular.forEach(ionMode, function (value, key) {
                if (value.selected === true) {
                    filtered.groupMeta['ion mode'].push(value.name.toUpperCase());
                }
            });

            // add ms type to query
            angular.forEach(ms, function (value, key) {
                if (value.selected === true) {
                    filtered.groupMeta['ms level'].push(value.name);
                }
            });

            SpectraQueryBuilderService.setQuery(filtered);
        }

    }
})();
