/**
 * Created by wohlgemuth on 6/12/14.
 */

/**
 * disables automatic form submission when you press enter in an input element
 */
app.directive('gwTag', function ($compile, $location, $rootScope, $log) {
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
            tag: '=value'
        },
        //controller to handle building new queries
        controller: function ($scope, $element, QueryCache, SpectraQueryBuilderService) {

            //$log.info (angular.element(angular.element(angular.element(angular.element($element.parent()).children()[0]).children()[0]).children()[0]) );

            //receive a click
            $scope.newQuery = function () {
                //build a mona query based on this label
                var query = SpectraQueryBuilderService.prepareQuery();
                query.tags = [$scope.tag.text];

                //assing to the rootscope
                QueryCache.setSpectraQuery(query);


                //run the query and show it's result in the spectra browser
                $location.path("/spectra/browse/");
            };

            //receive a click
            $scope.addToQuery = function () {
                SpectraQueryBuilderService.addTagToQuery($scope.tag.text);
                $location.path("/spectra/browse/");

            };


            //receive a click
            $scope.removeFromQuery = function () {

                //build a mona query based on this label
                SpectraQueryBuilderService.removeTagFromQuery($scope.tag.text);

                //run the query and show it's result in the spectra browser

                $location.path("/spectra/browse/");

            };


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

            //set the carret for us
            elem.append('<span class="caret left30"></span>');

        }
    }
});