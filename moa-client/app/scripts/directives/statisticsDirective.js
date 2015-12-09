/**
 * Created by wohlgemuth on 5/14/15.
 */


(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('statistics', statistics);

    statistics.$inject = ['StatisticsService'];

    function statistics($compile, $filter, StatisticsService, $log) {
        var directive = {
            restrict: 'AE',
            replace: false,
            template: '<span>{{executionTime | number:0}} {{unit}} over the last {{timeframe}}</span>',
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
            controller: statisticsController,

            //decorate our elements based on there properties
            link: function($scope, element, attrs, ngModel) {

                StatisticsService.executionTime({time: $scope.timeframe, method: $scope.query, max: 1},
                  function(data) {
                      if (data.length) {
                          $scope.executionTime = data[0].avg;

                          if ($scope.executionTime > 1000) {
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
                      } else {
                          $scope.executionTime = 0;
                      }
                  }
                );
            }
        };

        return directive;
    }

    statisticsController.$inject = ['$scope'];

    function statisticsController($scope) {
        $scope.executionTime = "loading...";
        $scope.unit = "ms";
    }

})();