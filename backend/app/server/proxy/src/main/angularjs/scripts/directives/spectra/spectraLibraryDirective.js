/**
 * Created by wohlgemuth on 10/16/14.
 */
(function() {
    'use strict';

    displayLibraryReferenceController.$inject = ['$scope', '$log'];
    angular.module('moaClientApp')
        .directive('displayLibraryReference', displayLibraryReference);

    function displayLibraryReference() {
        var directive = {
            require: 'ngModel',
            restrict: 'A',
            template: '<span data-ng-bind-html="libraryString"></span>',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: displayLibraryReferenceController
        };

        return directive;
    }

    /* @ngInject */
    function displayLibraryReferenceController($scope, $log) {


        // Empty string if no library object exists
        if (!$scope.spectrum.library) {
            $scope.libraryString = '';
            return;
        }

        /**** debug
        console.log($scope.spectrum.libraryIdentifier);
        console.log(angular.isDefined($scope.spectrum.libraryIdentifier));
        console.log($scope.spectrum.libraryIdentifier === null);
        ****/

        // Base library string
        $scope.libraryString = 'Originally submitted to the ';

        var library = $scope.spectrum.library;

        // Handle a provided library link
        if (angular.isDefined(library.link) && library.link != "") {
            // Link to library but no identifier
            if (!angular.isDefined(library.id)) {
                $scope.libraryString += '<a href="'+ library.link +'" target="_blank">'+
                    library.description +'</a>';
            }

            // Link to library and identifier and link placeholder for identifier
            else if (angular.isDefined(library.id) && library.link.indexOf('%s') > -1) {
                var link = library.link.replace('%s', library.id);

                $scope.libraryString += $scope.spectrum.library.description + ' as <a href="'+ link +'" target="_blank">'+
                    library.id +'</a>';
            }

            // Link to library and identifier but no link placeholder for identifier
            else {
                $scope.libraryString += '<a href="'+ library.link +'" target="_blank">'+ library.description +
                    '</a> as '+ library.id;
            }
        }

        // With no library link
        else {
            $scope.libraryString += library.description;
        }
    }
})();

