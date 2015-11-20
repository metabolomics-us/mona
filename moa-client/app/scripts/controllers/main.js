//TODO: find out if this file is used, then remove if no longer needed.
'use strict';

angular.module('moaClientApp')
    .controller('MainCtrl', function ($scope) {
        $scope.slides = [
            {image: "images/s1.png", id: '252'},
            {image: "images/s2.png", id: '931'}
        ];
    });
