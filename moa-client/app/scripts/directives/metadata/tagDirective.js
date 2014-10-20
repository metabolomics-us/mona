/**
 * Created by wohlgemuth on 6/12/14.
 */

/**
 * disables automatic form submission when you press enter in an input element
 */
app.directive('gwTag', function ($compile) {
    return {
        restrict: 'A',
        scope: {
            ruleBased : '=ruleBased'
        },
        link: function ($scope, element, attrs, ngModel) {

            element.addClass("label");

            if($scope.ruleBased == true) {
                element.addClass("label-info");
                element.append("<i class='fa fa-flask'></i>");
            }
            else{
                element.addClass("label-primary");
            }


        }
    }
});