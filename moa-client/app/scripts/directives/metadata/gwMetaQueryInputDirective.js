/**
 * defines a metadata text field combo with autocomplete and typeahead functionality
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('gwMetaQueryInput', gwMetaQueryInput)

    function gwMetaQueryInput() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/metaQueryInput.html',
            replace: true,
            transclude: true,
            scope: {
                query: '=',
                editable: '=?',
                fullText: '=?'
            },
            link: linkFunc,
            controller: gwMetaQueryInputController
        };

        return directive;
    }


    function linkFunc($scope, element, attrs, ngModel) {

    }

    //controller to handle building of the queries
    /* @ngInject */
    function gwMetaQueryInputController($scope, $element, SpectraQueryBuilderService, $location,
                                        REST_BACKEND_SERVER, $http, $filter, $log, limitToFilter) {

        $scope.metadata = [];
        //$scope.metadataNames = [];

        //our select options, should be based on metadata value
        //should be based on received data type for metadata fields
        $scope.select = [
            {name: "equals", value: "eq"},
            {name: "does not equal", value: "ne"}
        ];

        $scope.metadata.selected = $scope.select[0];

        /**
         * tries to find meta data names for us
         * @param value
         */
        $scope.queryMetadataNames = function(value) {
            if (angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') === '') {
                return $http.get(REST_BACKEND_SERVER + '/rest/meta/searchNames/', {}).then(function(data) {
                    return limitToFilter(data.data, 50);
                });

            }
            else {
                return $http.get(REST_BACKEND_SERVER + '/rest/meta/searchNames/' + value + "?max=10", {}).then(function(data) {
                    return limitToFilter(data.data, 25);
                });
            }
        };

        /**
         * queries our values
         * @param name
         * @param value
         */
        $scope.queryMetadataValues = function(name, value) {

            if (angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') === '') {
                return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search', {
                    query: {
                        name: name,
                        value: {isNotNull: ''},
                        property: 'stringValue',
                        deleted: false
                    }
                }).then(function(data) {
                    return limitToFilter(data.data, 25);
                });

            }
            else if (angular.isDefined($scope.fullText)) {
                return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search?max=10', {
                    query: {
                        name: name,
                        value: {ilike: '%' + value + '%'},
                        property: 'stringValue',
                        deleted: false
                    }
                }).then(function(data) {
                    return limitToFilter(data.data, 25);
                });
            }
            else {
                return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search?max=10', {
                    query: {
                        name: name,
                        value: {ilike: value + '%'},
                        property: 'stringValue',
                        deleted: false

                    }
                }).then(function(data) {
                    return limitToFilter(data.data, 25);
                });
            }

        };

        /**
         * adds a metadata query
         */
        $scope.addMetadataQuery = function() {
            $scope.query.push({name: '', value: ''});
        };

        /**
         * initializations
         */
        (function() {
            // Set query if undefined
            if (!angular.isDefined($scope.query)) {
                $scope.query = [];
            }

            // Set blank entry if query list is empty
            if ($scope.query.length === 0) {
                $scope.addMetadataQuery();
            }

            // Set editable option if not set
            if (!angular.isDefined($scope.editable)) {
                $scope.editable = false;
            }

            /*
             // Get metadata
             MetadataService.metadata(
             function (data) {
             var metadataNames = {};

             for (var i = 0; i < data.length; i++) {
             if (data[i].category.visible) {
             var name = data[i].category.name;

             metadataNames[data[i].name] = true;

             if (!$scope.metadata.hasOwnProperty(name)) {
             $scope.metadata[name] = [];
             }

             $scope.metadata[name].push(data[i]);
             }
             }

             $scope.metadataNames = Object.keys(metadataNames);
             },
             function (error) {
             $log.error('metadata failed: ' + error);
             }
             );

             */
        })();
    }
})();