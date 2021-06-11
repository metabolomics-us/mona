/**
 * Created by sajjan on 3/2/16.
 */
import * as angular from 'angular';

class SpectraLibraryDirective {
    constructor() {
        return {
            require: 'ngModel',
            restrict: 'A',
            template: '<span data-ng-bind-html="$ctrl.libraryString"></span>',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: SpectraLibraryController,
            controllerAs: '$ctrl'
        };
    }
}

class SpectraLibraryController {
    private static $inject = ['$scope', '$log'];
    private $scope;
    private $log;
    private libraryString;

    constructor($scope, $log) {
        this.$scope = $scope;
        this.$log = $log;
    }

    $onInit = () => {
        // Empty string if no library object exists
        if (!this.$scope.spectrum.library || !this.$scope.spectrum.library.description || this.$scope.spectrum.library.description == '') {
            this.libraryString = '';
            return;
        }

        // Base library string
        this.libraryString = 'Originally submitted to the ';

        let library = this.$scope.spectrum.library;

        // Handle a provided library link
        if (angular.isDefined(library.link) && library.link != "") {
            // Link to library but no identifier
            if (!angular.isDefined(library.id)) {
                this.libraryString += '<a href="'+ library.link +'" target="_blank">'+
                    library.description +'</a>';
            }

            // Link to library and identifier and link placeholder for identifier
            else if (angular.isDefined(library.id) && library.link.indexOf('%s') > -1) {
                let link = library.link.replace('%s', library.id);

                this.libraryString += this.$scope.spectrum.library.description + ' as <a href="'+ link +'" target="_blank">'+
                    library.id +'</a>';
            }

            // Link to library and identifier but no link placeholder for identifier
            else {
                this.libraryString += '<a href="'+ library.link +'" target="_blank">'+ library.description +
                    '</a> as '+ library.id;
            }
        }

        // With no library link
        else {
            this.libraryString += library.description;
        }
    }


}

angular.module('moaClientApp')
    .directive('displayLibraryReference', SpectraLibraryDirective);
