/*
workaround for carousel fix
compatibility between ui.bootstrap and ngAnimate
 https://github.com/angular-ui/bootstrap/issues/1350
 */

'use strict';

app.directive('disableAnimation', function($animate){
    return {
        restrict: 'A',
        link: function($scope, $element, $attrs){
            $attrs.$observe('disableAnimation', function(value){
                $animate.enabled(!value, $element);
            });
        }
    }
});