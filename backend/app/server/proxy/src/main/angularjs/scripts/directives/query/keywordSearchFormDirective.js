(function () {
    'use strict';

    angular.module('moaClientApp')
        .directive('keywordSearchForm', keywordSearchForm);

    function keywordSearchForm() {
        KeywordSearchController.$inject = ['$scope', 'SpectraQueryBuilderService', '$log'];
        function KeywordSearchController($scope, SpectraQueryBuilderService, $log) {

            $scope.query = {
                exactMassTolerance: 0.5
            };

            /**
             * Form fields
             */
            $scope.sourceIntroduction = [
                {name: 'Liquid Chromatography', abv: 'LC'},
                {name: 'Gas Chromatography', abv: 'GC'},
                {name: 'Capillary Electrophoresis', abv: 'CE'}
            ];

            $scope.ionizationMethod = [
                {name: 'Atmospheric Pressure Chemical Ionization', abv: '(APCI)'},
                {name: 'Chemical Ionization', abv: '(CI)'},
                {name: 'Electron Impact', abv: '(EI)'},
                {name: 'Electrospray Ionization', abv: '(ESI)'},
                {name: 'Fast Atom Bombardment', abv: '(FAB)'},
                {name: 'Matrix Assisted Laser Desorption Ionization', abv: '(MALDI)'}
            ];

            $scope.msType = [{name: 'MS1'}, {name: 'MS2'}, {name: 'MS3'}, {name: 'MS4'}];
            $scope.ionMode = [{name: 'Positive'}, {name: 'Negative'}];


            /**
             * Process given query parameters and execute query
             */
            $scope.submitQuery = function () {
                SpectraQueryBuilderService.prepareQuery();

                // Query Name/InChIKey search
                if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test($scope.query.name)) {
                    SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', $scope.query.name);
                } else if (/^[A-Z]{14}$/.test($scope.query.name)) {
                    SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', $scope.query.name, true);
                } else if (angular.isDefined($scope.query.name)) {
                    SpectraQueryBuilderService.addNameToQuery($scope.query.name);
                }

                // Query compound classification
                if (angular.isDefined($scope.query.classification)) {
                    SpectraQueryBuilderService.addGeneralClassificationToQuery($scope.query.classification);
                }

                // Query molecular formula
                if (angular.isDefined($scope.query.formula)) {
                    SpectraQueryBuilderService.addCompoundMetaDataToQuery('molecular formula', $scope.query.formula, true);
                }

                // Query exact mass
                if (angular.isDefined($scope.query.exactMass)) {
                    SpectraQueryBuilderService.addNumericalCompoundMetaDataToQuery('total exact mass', $scope.query.exactMass, $scope.query.exactMassTolerance);
                }

                // Handle chromatography
                var chromatography = $scope.sourceIntroduction.reduce(function (result, element) {
                    if (element.selected)
                        result.push(element.abv + '-MS');
                    return result;
                }, []);

                if (chromatography.length > 0) {
                    SpectraQueryBuilderService.addTagToQuery(chromatography);
                }

                // Handle ionization mode
                var ionMode = $scope.ionMode.reduce(function (result, element) {
                    if (element.selected)
                        result.push(element.name.toLowerCase());
                    return result;
                }, []);

                if (ionMode.length > 0) {
                    SpectraQueryBuilderService.addMetaDataToQuery('ionization mode', ionMode);
                }


                // Handle MS type
                var msType = $scope.msType.reduce(function (result, element) {
                    if (element.selected)
                        result.push(element.name);
                    return result;
                }, []);

                if (msType.length > 0) {
                    SpectraQueryBuilderService.addMetaDataToQuery('ms level', msType);
                }


                // Redirect to the spectra browser
                SpectraQueryBuilderService.executeQuery();
            };
        }

        return {
            restrict: 'E',
            templateUrl: 'views/spectra/query/keywordSearchForm.html',
            controller: KeywordSearchController
        };
    }
})();
