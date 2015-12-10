/**
 * Created by wohlgemuth on 5/14/15.
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('statistics', statistics);

    function statistics() {
        var directive = {
            restrict: 'AE',
            replace: false,
            template: '<span>{{ctrl.executionTime | number:0}} {{ctrl.unit}} over the last {{ctrl.timeframe}}</span>',
            scope: {
                timeframe: '@',
                query: '@'
            },
            controller: statisticsController,
            controllerAs: 'ctrl',
            // bind scope to variable, we no longer need $compile or link function, and we can input data into template
            bindToController: true
        };

        return directive;
    }

    statisticsController.$inject = ['StatisticsService'];

    function statisticsController(StatisticsService) {
        var ctrl = this;
        ctrl.executionTime = "loading...";
        ctrl.unit = "ms";

        getExecutionTime();


        function getExecutionTime() {
            StatisticsService.executionTime({time: ctrl.timeframe, method: ctrl.query, max: 1}, function(data) {
                if (data.length) {
                    ctrl.executionTime = data[0].avg;
                    calculateTime();
                } else {
                    ctrl.executionTime = 0;
                }
            })
        }

        function calculateTime() {
            console.log(ctrl.executionTime);
            if (ctrl.executionTime > 1000) {
                ctrl.executionTime = ctrl.executionTime / 1000;
                ctrl.unit = "s";

                if (ctrl.executionTime > 90) {
                    ctrl.executionTime = ctrl.executionTime / 60;
                    ctrl.unit = "min";


                    if (ctrl.executionTime > 90) {
                        ctrl.executionTime = ctrl.executionTime / 60;
                        ctrl.unit = "h";


                        if (ctrl.executionTime > 24) {
                            ctrl.executionTime = ctrl.executionTime / 24;
                            ctrl.unit = "d";
                        }
                    }
                }
            }
        }
    }
})();


/*
 (function() {
 'use strict';

 angular.module('moaClientApp')
 .directive('statistics', statistics);

 statistics.$inject = ['$compile', '$filter', 'StatisticsService'];

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
 */


