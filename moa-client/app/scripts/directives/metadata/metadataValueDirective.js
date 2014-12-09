/**
 * Created by wohlgemuth on 10/17/14.
 */

/**
 * used to render a metadata value field
 */
app.directive('gwValue', function ($compile) {
    return {
        templateUrl: '/views/templates/metaValue.html',

        restrict: 'A',
        scope: {
            value: '=value'
        },
        link: function ($scope, element, attrs, ngModel) {


            if ($scope.value.computed == true) {
                element.append("<i class='fa fa-flask'></i>");
            }
        }
    }
});

/**
 * links a metadata field to a query builder and executes the spectra query for us
 */
app.directive('gwMetaQuery', function ($compile) {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/metaQuery.html',
        restrict: 'A',
        scope: {
            value: '=value',
            compound: '=compound'
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building new queries
        controller: function ($scope, $element, SpectraQueryBuilderService, QueryCache, $location) {

            //receive a click
            $scope.newQuery = function () {
                //build a mona query based on this label
                SpectraQueryBuilderService.prepareQuery();

                //add it to query
                SpectraQueryBuilderService.addMetaDataToQuery($scope.value, $scope.compound);

                //assign to the cache

                //run the query and show it's result in the spectra browser
                $location.path("/spectra/browse/");
            };

            //receive a click
            $scope.addToQuery = function () {
                SpectraQueryBuilderService.addMetaDataToQuery($scope.value, $scope.compound);
                $location.path("/spectra/browse/");
            };


            //receive a click
            $scope.removeFromQuery = function () {
                //build a mona query based on this label
                SpectraQueryBuilderService.removeMetaDataFromQuery($scope.value, $scope.compound);

                //run the query and show it's result in the spectra browser
                $location.path("/spectra/browse/");
            };
        }
    }
});

/**
 * defines a metadata text field combo with autocomplete and typeahead functionality
 */
app.directive('gwMetaQueryInput', function ($compile) {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/metaQueryInput.html',
        restrict: 'A',
        scope: {
            query: '=',
            editable: '=?'
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building of the queires
        controller: function ($scope, $element, SpectraQueryBuilderService, QueryCache, $location, REST_BACKEND_SERVER, $http, AppCache) {

            $scope.metadata = {};
            $scope.metadataNames = [];

            /**
             * queries our values
             * @param name
             * @param value
             */
            $scope.queryMetadataValues = function (name, value) {
                return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search?max=10', {
                    query: {
                        name: name,
                        value: {ilike: '%' + value + '%'},
                        property: 'stringValue'
                    }
                }).then(function (data) {
                    return data.data;
                });
            };

            /**
             * adds a metadata query
             */
            $scope.addMetadataQuery = function () {
                $scope.query.push({name: '', value: ''});
            };

            /**
             * initializations
             */
            (function () {
                // Set query if undefined
                if(!angular.isDefined($scope.query)) {
                    $scope.query = [];
                }

                // Set blank entry if query list is empty
                if($scope.query.length == 0) {
                    $scope.addMetadataQuery();
                }

                // Set editable option if not set
                if(!angular.isDefined($scope.editable)) {
                    $scope.editable = false;
                }

                // Get metadata
                AppCache.getMetadata(function (data) {
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
                });
            })();
        }
    }
});
