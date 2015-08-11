/**
 * Created by wohlgemuth on 10/17/14.
 */

/**
 * used to render a metadata value field
 */
app.directive('gwValue', function () {
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
app.directive('gwMetaQuery', function () {
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
        controller: function ($scope, $element, SpectraQueryBuilderService, $location) {

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
 * allows us to easily modify meta data on the fly
 */
app.directive('gwMetaEdit', function () {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/meta/editMeta.html',
        restrict: 'A',
        scope: {
            value: '=value'
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building new queries
        controller: function ($scope, $element, MetaData, $filter) {

            //receive a click
            $scope.hide = function () {


                MetaData.get({id: $scope.value.metaDataId}, function (value) {

                    value.hidden = true;

                    console.log($filter('json')(value));

                    value.$update(function () {
                        $scope.value.hidden = true;

                    });
                });
            };

            //receive a click
            $scope.unhide = function () {

                MetaData.get({id: $scope.value.metaDataId}, function (value) {

                    value.hidden = false;

                    console.log($filter('json')(value));

                    value.$update(function () {
                        $scope.value.hidden = false;
                    });
                });
            };

        }
    }
});


/**
 * adds the given id to the query or removes it
 */
app.directive('gwSpectraIdQuery', function () {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/spectra/spectraHashQuery.html',
        restrict: 'A',
        scope: {
            value: '=value'
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building new queries
        controller: function ($scope, $element, SpectraQueryBuilderService, $location) {

            //receive a click
            $scope.newQuery = function () {
                //build a mona query based on this label
                SpectraQueryBuilderService.prepareQuery();

                //add it to query
                SpectraQueryBuilderService.addSpectraIdToQuery($scope.value);

                //assign to the cache

                //run the query and show it's result in the spectra browser
                $location.path("/spectra/browse/");
            };

            //receive a click
            $scope.addToQuery = function () {
                SpectraQueryBuilderService.addSpectraIdToQuery($scope.value);
                $location.path("/spectra/browse/");
            };

            //finds related spectra to this spectra
            $scope.findSimilarSpectra = function(){

                SpectraQueryBuilderService.removeSpectraIdFromQuery($scope.value);

                SpectraQueryBuilderService.addSimilarSpectraToQuery($scope.value.split("-")[3]);
                $location.path("/spectra/browse/");
            };

            //receive a click
            $scope.removeFromQuery = function () {
                //build a mona query based on this label
                SpectraQueryBuilderService.removeSpectraIdFromQuery($scope.value);

                //run the query and show it's result in the spectra browser
                $location.path("/spectra/browse/");
            };
        }
    }
});

/**
 * simple directive to help populating the type ahead views on focis
 */
app.directive('typeaheadFocus', function () {
    return {
        require: 'ngModel',
        link: function (scope, element, attr, ngModel) {

            //trigger the popup on 'click' because 'focus'
            //is also triggered after the item selection
            element.bind('click', function () {

                var viewValue = ngModel.$viewValue;

                //restore to null value so that the typeahead can detect a change
                if (ngModel.$viewValue == ' ') {
                    ngModel.$setViewValue(null);
                }

                //force trigger the popup
                ngModel.$setViewValue(' ');

                //set the actual value in case there was already a value in the input
                ngModel.$setViewValue(viewValue || ' ');
            });

            //compare function that treats the empty space as a match
            scope.emptyOrMatch = function (actual, expected) {
                if (expected == ' ') {
                    return true;
                }
                return actual.indexOf(expected) > -1;
            };
        }
    };
});



/**
 * defines a metadata text field combo with autocomplete and typeahead functionality
 */
app.directive('gwMetaQueryInput', function () {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/metaQueryInput.html',
        restrict: 'A',
        scope: {
            query: '=',
            editable: '=?',
            fullText: '=?'
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building of the queries
        controller: function ($scope, $element, SpectraQueryBuilderService, $location, REST_BACKEND_SERVER, $http, $filter, $log,limitToFilter) {

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
            $scope.queryMetadataNames = function(value){
                if(angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') == '') {
                    return $http.get(REST_BACKEND_SERVER + '/rest/meta/searchNames/', {}).then(function (data) {
                        return limitToFilter(data.data,50);
                    });

                }
                else {
                    return $http.get(REST_BACKEND_SERVER + '/rest/meta/searchNames/' + value + "?max=10", {}).then(function (data) {
                        return limitToFilter(data.data,25);
                    });
                }
            };

            /**
             * queries our values
             * @param name
             * @param value
             */
            $scope.queryMetadataValues = function (name, value) {

                if(angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') == ''){
                    return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search', {
                        query: {
                            name: name,
                            value: {isNotNull: ''},
                            property: 'stringValue',
                            deleted: false
                        }
                    }).then(function (data) {
                        return limitToFilter(data.data,25);
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
                        }).then(function (data) {
                            return limitToFilter(data.data,25);
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
                        }).then(function (data) {
                            return limitToFilter(data.data,25);
                        });
                    }

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
                if (!angular.isDefined($scope.query)) {
                    $scope.query = [];
                }

                // Set blank entry if query list is empty
                if ($scope.query.length == 0) {
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
    }
});
