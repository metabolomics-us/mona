/**
 * Created by wohlgemuth on 11/3/14.
 */

app.directive('showQuery', function ($compile) {
    return {
        //must be an attribute
        restrict: 'A',
        replace: true,
        template: '<div><pre>{{query | json}}</pre></div>',

        /**
         * watches for changes and is used to modify the query terms on the fly
         * @param $scope
         * @param QueryCache
         * @param $log
         * @param $rootScope
         */
        controller: function ($scope, QueryCache, $log, $rootScope) {
            $scope.query = QueryCache.getSpectraQuery();

            $scope.$on('spectra:query', function (event, data) {
                $scope.query = data;
            });
        },

        //decorate our elements based on there properties
        link: function ($scope, element, attrs, ngModel) {

        }
    }
});