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