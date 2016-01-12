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

    /* @ngInject */
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
