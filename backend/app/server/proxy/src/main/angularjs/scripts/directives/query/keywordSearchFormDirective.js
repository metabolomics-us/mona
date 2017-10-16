(function () {
    'use strict';

    KeywordSearchController.$inject = ['$scope', 'SpectraQueryBuilderService', 'TagService', '$log'];
    angular.module('moaClientApp')
        .directive('keywordSearchForm', keywordSearchForm);

    function keywordSearchForm() {
        return {
            restrict: 'E',
            templateUrl: 'views/spectra/query/keywordSearchForm.html',
            controller: KeywordSearchController
        };
    }

    /* @ngInject */
    function KeywordSearchController($scope, SpectraQueryBuilderService, TagService, $log) {

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

            // Handle library tags
            var libraryTags = $scope.libraryTags.reduce(function (result, element) {
                if (element.selected)
                    result.push(element.text);
                return result;
            }, []);
            
            if (libraryTags.length > 0) {
                SpectraQueryBuilderService.addTagToQuery(libraryTags);
            }

            // Handle all other tags
            $scope.queryTags.forEach(function(tag) {
                if (tag.selected == '+') {
                    SpectraQueryBuilderService.addTagToQuery(tag.text);
                } else if (tag.selected == '-') {
                    SpectraQueryBuilderService.addTagToQuery(tag.text, 'ne');
                }
            });

            // Redirect to the spectra browser
            SpectraQueryBuilderService.executeQuery();
        };

        (function() {
            TagService.query(
                function (tags) {
                    $scope.queryTags = tags.filter(function(x) {
                        return x.category != 'library' && !x.ruleBased;
                    });

                    $scope.libraryTags = tags.filter(function(x) {
                        return x.category == 'library';
                    });
                },
                function (error) {
                    $log.error('Tag pull failed: '+ error);
                }
            );
        })();
    }
})();
