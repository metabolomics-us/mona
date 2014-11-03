/**
 * Created by wohlgemuth on 10/17/14.
 */


app.directive('gwValue', function ($compile) {
    return {

        restrict: 'A',
        scope: {
            value: '=value'
        },
        link: function ($scope, element, attrs, ngModel) {


            if ($scope.value.suspect == true) {
                element.append("<i class='fa fa-exclamation-triangle'></i>");
            }

            if ($scope.value.computed == true) {
                element.append("<i class='fa fa-flast'></i>");
            }
        }
    }
});


app.directive('gwMetaQuery', function ($compile) {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/metaQuery.html',
        restrict: 'A',
        scope: {
            value: '=value'
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building new queries
        controller: function ($scope, $element,SpectraQueryBuilderService, $location) {

            //receive a click
            $scope.newQuery = function () {


                //build a mona query based on this label
                var query = SpectraQueryBuilderService.prepareQuery();

                //assing to the rootscope
                $rootScope.setSpectraQuery(query);

                //add it to query
                SpectraQueryBuilderService.addMetaDataToQuery($scope.value);

                //run the query and show it's result in the spectra browser

                $location.path("/spectra/browse/");

            };

            //receive a click
            $scope.addToQuery = function () {
                SpectraQueryBuilderService.addMetaDataToQuery($scope.value);
                $location.path("/spectra/browse/");

            };


            //receive a click
            $scope.removeFromQuery = function () {

                //build a mona query based on this label
                SpectraQueryBuilderService.removeMetaDataFromQuery($scope.value);

                //run the query and show it's result in the spectra browser

                $location.path("/spectra/browse/");

            };


        }
    }
});

