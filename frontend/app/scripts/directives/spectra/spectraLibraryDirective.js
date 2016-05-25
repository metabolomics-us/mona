/**
 * Created by wohlgemuth on 10/16/14.
 */
(function() {
    'use strict';

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
    function displayLibraryReferenceController($scope) {

        // Empty string if no library object exists
        if ($scope.spectrum.library === null) {
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

        // Handle a provided library link

        if (angular.isDefined($scope.spectrum.library.link)) {
            // Link to library but no identifier
            if ($scope.spectrum.libraryIdentifier === null) {
                $scope.libraryString += '<a href="'+ $scope.spectrum.library.link +'" target="_blank">'+
                    $scope.spectrum.library.description +'</a>';
            }

            // Link to library and identifier and link placeholder for identifier
            else if (angular.isDefined($scope.spectrum.libraryIdentifier) && $scope.spectrum.library.link.indexOf('%s') > -1) {
                var link = $scope.spectrum.library.link.replace('%s', $scope.spectrum.libraryIdentifier);

                $scope.libraryString += $scope.spectrum.library.description + ' as <a href="'+ link +'" target="_blank">'+
                    $scope.spectrum.libraryIdentifier +'</a>';
            }

            // Link to library and identifier but no link placeholder for identifier
            else {
                $scope.libraryString += '<a href="'+ $scope.spectrum.library.link +'" target="_blank">'+ $scope.spectrum.library.description +
                    '</a> as '+ $scope.spectrum.libraryIdentifier;
            }
        }

        // With no library link
        else {
            $scope.libraryString += $scope.spectrum.library.description;
        }
    }
})();

