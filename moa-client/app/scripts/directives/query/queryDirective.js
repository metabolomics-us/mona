/**
 * Created by wohlgemuth on 11/3/14.
 */

app.directive('showQuery', function ($compile) {
    return {
        //must be an attribute
        restrict: 'A',
        replace: true,
        templateUrl: '/views/templates/showQuery.html',

        /**
         * watches for changes and is used to modify the query terms on the fly
         * @param $scope
         * @param $log
         * @param $rootScope
         */
        controller: function ($scope, $log, $rootScope,SpectraQueryBuilderService,Spectrum) {
            $scope.status = {
                isOpen : false
            };

            $scope.query = SpectraQueryBuilderService.getQuery();
            $scope.result = [];

            $scope.$on('spectra:query', function (event, data) {
                $scope.query = data;
            });

            $scope.$on('spectra:loaded', function (event, data) {
                $scope.result = data;
            });


            $scope.$on('spectra:query:show', function(event, data) {
                $scope.status.isOpen = !$scope.status.isOpen;
            });

            $scope.curateSpectra = function(){

                Spectrum.curateSpectraByQuery($scope.query, function (data) {
                });
            };

            $scope.associateSpectra = function(){

                Spectrum.associateSpectraByQuery($scope.query, function (data) {
                });
            };

        },

        //decorate our elements based on there properties
        link: function ($scope, element, attrs, ngModel) {

        }
    }
});
