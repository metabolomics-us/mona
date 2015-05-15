/**
 * Created by wohlgemuth on 5/14/15.
 */

app.directive('statistics', function ($compile, $filter,StatisticsService, $log) {
    return {
        //must be an attribute
        replace: false,
        scope: {
            timeframe: '@',
            query: '@'
        },

        /**
         * watches for changes and is used to modify the query terms on the fly
         * @param $scope
         * @param QueryCache
         * @param $log
         * @param $rootScope
         */
        controller: function ($scope) {

            $scope.executionTime = "loading...";
            $scope.unit = "ms";

        },

        //decorate our elements based on there properties
        link: function ($scope, element, attrs, ngModel) {

            StatisticsService.executionTime({time:$scope.timeframe,method:$scope.query,max:1},
                function(data){
                    $scope.executionTime = data[0].avg;

                    if($scope.executionTime > 1000) {
                        $scope.executionTime = $scope.executionTime / 1000;
                        $scope.unit = "s";


                        if ($scope.executionTime > 90) {
                            $scope.executionTime = $scope.executionTime / 60;
                            $scope.unit = "min";


                            if ($scope.executionTime > 90) {
                                $scope.executionTime = $scope.executionTime / 60;
                                $scope.unit = "h";


                                if ($scope.executionTime > 24) {
                                    $scope.executionTime = $scope.executionTime / 24;
                                    $scope.unit = "d";
                                }
                            }
                        }
                    }

                }
            );
        },

        template: '<span>{{executionTime | number:0}} {{unit}} over the last {{timeframe}}</span>'
    }
});