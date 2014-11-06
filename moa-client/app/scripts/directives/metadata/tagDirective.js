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
            ruleBased: '=',
            type: '@',
            tag: '=value',
            size: '@'
        },

        //controller to handle building new queries
        controller: function ($scope, SpectraQueryBuilderService, $location) {
            $scope.options = [];

            if($scope.type == 'spectrum') {
                $scope.options = [
                    {
                        name: 'Create new query',
                        action: function (tag, status) {
                            //build a mona query based on this label
                            SpectraQueryBuilderService.prepareQuery();
                            SpectraQueryBuilderService.addTagToQuery(tag.text);

                            //run the query and show it's result in the spectra browser
                            $location.path("/spectra/browse/");
                        }
                    },
                    {
                        name: 'Add to query',
                        action: function (tag, status) {
                            SpectraQueryBuilderService.addTagToQuery(tag.text);
                            $location.path("/spectra/browse/");
                        }
                    },
                    {
                        name: 'Remove from query',
                        action: function (tag, status) {
                            SpectraQueryBuilderService.removeTagFromQuery(tag.text);
                            $location.path("/spectra/browse/");
                        }
                    }
                ];
            }
        },

        //decorate our elements based on there properties
        link: function (scope, element, attrs, ctrl) {
            var elem = angular.element(element[0].querySelector('.btn'));

            //append an image
            if (scope.ruleBased == true) {
                elem.addClass("btn-info");
                elem.append("<span class='left15'><i class='fa fa-flask'></i></span>");
            }
            //make it the class which shows it's not computed
            else {
                elem.addClass("btn-primary");
            }

            //set button size
            if (typeof scope.size == 'undefined' && (scope.size == 'lg' || scope.size == 'sm' || scope.size == 'xs')) {
                elem.addClass("btn-"+ $scope.size);
            }

            // Set default tag status
            scope.status = {
                active: false
            };

            //set the caret for us
            elem.append('<span class="caret left30"></span>');
        }
    }
});