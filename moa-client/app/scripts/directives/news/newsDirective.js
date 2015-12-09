/**
 * Created by wohlgemuth on 2/27/15.
 * disables automatic form submission when you press enter in an input element
 * TODO: implement NEWS feature once app is working
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
    .directive('gwNews', function(News, $interval, $timeout) {
        return {
            //must be an attribute
            restrict: 'A',

            //we want to replace the element
            replace: true,

            //replace the fields text and add it in the internal span
            //transclude: true,

            //our template to use
            templateUrl: '/views/templates/news.html',

            //scope definition
            scope: {
                limit: '@',
                type: '@'
            },
            priority: 1001,


            controller: function($scope, $interval, $timeout) {
                $scope.recentNews = [];

            },

            //decorate our elements based on there properties
            link: function($scope, element, attrs, ctrl) {

                var running;

                if (angular.isDefined(running)) return;

                $scope.loadData = function() {
                    if ($scope.type === 'announcements') {
                        News.listAnnouncements(function(data) {
                            $scope.recentNews.length = 0;
                            $scope.recentNews = data;
                        });
                    }
                    else if ($scope.type === 'upload') {
                        News.listUpdates(function(data) {
                            $scope.recentNews.length = 0;
                            $scope.recentNews = data;
                        });
                    }
                    else if ($scope.type === 'notification') {
                        News.listNotifications(function(data) {
                            $scope.recentNews.length = 0;
                            $scope.recentNews = data;
                        });
                    }
                    else {
                        News.query(function(data) {
                            $scope.recentNews.length = 0;
                            $scope.recentNews = data;
                        });
                    }

                    $timeout(function() {
                        $scope.$apply();
                    });
                };

                $scope.loadData();

                //let it run in the background
                running = $interval($scope.loadData, 5 * 1000);

                $scope.$on('$destroy', function() {
                    // Make sure that the interval is destroyed too
                    if (angular.isDefined(running)) {
                        $interval.cancel(running);
                    }
                });

            }
        }
    });
})();