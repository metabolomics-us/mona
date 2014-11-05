/**
 * Created by wohlgemuth on 6/12/14.
 */

/**
 * disables automatic form submission when you press enter in an input element
 */
app.directive('gwTag', function () {
    return {
        //must be an attribute
        restrict: 'A',

        //we want to replace the element
        replace: true,

        //replace the fields text and add it in the internal span
        //transclude: true,

        //our template to use
        templateUrl: '/views/templates/tags.html',

        //scope definition
        scope: {
            ruleBased: '=ruleBased',
            tag: '=value',
            options: '=options',
            size: '@size'
        },

        //decorate our elements based on there properties
        link: function ($scope, element, attrs, ngModel) {

            var elem = angular.element(element[0].querySelector('.btn'));

            //append an image
            if ($scope.ruleBased == true) {
                elem.addClass("btn-info");
                elem.append("<span class='left15'><i class='fa fa-flask'></i></span>");
            }
            //make it the class which shows it's not computed
            else {
                elem.addClass("btn-primary");
            }

            //set button size
            if (typeof $scope.size == 'undefined' && ($scope.size == 'lg' || $scope.size == 'sm' || $scope.size == 'xs')) {
                elem.addClass("btn-"+ $scope.size);
            }

            //set the carret for us
            elem.append('<span class="caret left30"></span>');

        }
    }
});
