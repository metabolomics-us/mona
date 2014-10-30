/**
 * Created by wohlgemuth on 6/12/14.
 */

/**
 * disables automatic form submission when you press enter in an input element
 */
app.directive('gwTag', function ($compile, $location,$rootScope,SpectraQueryBuilderService) {
    return {
        //must be an attribute
        restrict: 'A',

        //we want to replace the element
        replace: true,

        //replace the fields text and add it in the internal span
        transclude: true,

        //our template to use
        template: '<div class="label" ng-click="click()"><span ng-transclude></span></div>',

        //scope definition
        scope: {
            ruleBased: '=ruleBased'
        },
        //controller to handle linking
        controller: function ($scope, $element) {

            //receive a click
            $scope.click = function () {


                //grab the label
                var label = $element.text();

                //build a mona query based on this label
                var query = SpectraQueryBuilderService.prepareQuery();
                query.tags = [label];

                //assing to the rootscope
                $rootScope.spectraQuery = query;


                //run the query and show it's result in the spectra browser

                $location.path("/spectra/browse/");

            }
        },
        link: function ($scope, element, attrs, ngModel) {

            //append an image
            if ($scope.ruleBased == true) {
                element.addClass("label-info");
                element.append("<i class='fa fa-flask'></i>");
            }
            //make it the class which shows it's not computed
            else {
                element.addClass("label-primary");
            }


        }
    }
});