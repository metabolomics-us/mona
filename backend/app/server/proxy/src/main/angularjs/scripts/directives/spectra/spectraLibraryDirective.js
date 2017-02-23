/**
 * Created by wohlgemuth on 10/16/14.
 */
(function() {
    'use strict';

    displayLibraryReferenceController.$inject = ['$scope', '$log'];
    angular.module('moaClientApp')
        .directive('displayLibraryReference', displayLibraryReference);

    function displayLibraryReference() {
        return {
            require: 'ngModel',
            restrict: 'A',
            template: '<span data-ng-bind-html="libraryString"></span>',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: displayLibraryReferenceController
        };
    }

    /* @ngInject */
    function displayLibraryReferenceController($scope, $log) {

        console.log($scope.spectrum.library)
        console.log(!$scope.spectrum.library)
        console.log(!$scope.spectrum.library.description)
        console.log($scope.spectrum.library.description == '')

        // Empty string if no library object exists
        if (!$scope.spectrum.library || !$scope.spectrum.library.description || $scope.spectrum.library.description == '') {
            $scope.libraryString = '';
            return;
        }

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

