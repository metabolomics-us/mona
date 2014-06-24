/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.SpectraBrowserController = function($scope, Spectrum, TaggingService, $modal) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.spectra = [];


    $scope.tags = [];
    $scope.tagsSelection = '';
    /**
     * list all our submitters in the system
     */
    $scope.listCompounds = list();


    $scope.viewSpectrum = function(id) {
        var modalInstance = $modal.open({
            templateUrl: '/views/compounds/viewCompound.html',
            controller: moaControllers.ViewCompoundModalController,
            size: 'lg',
            backdrop: 'true',
            resolve: {
                compound: function () {
                    return $scope.compounds[id];
                }
            }
        });
    }


    /**
     * helper function
     */
    function list() {
        $scope.spectra = Spectrum.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })


        $scope.tags = TaggingService.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })
    }
}

/*
app.directive('multiselectDropdown', function() {
    return function(scope, element, attributes) {
        // http://stackoverflow.com/questions/16933324
        element = $(element[0]);

        element.multiselect({
            buttonClass: 'btn btn-mini btn-primary',
            buttonWidth: '150px',
            buttonContainer: '<div class="btn-group" />',
            maxHeight: 250,
            enableFiltering: true,
            enableCaseInsensitiveFiltering: true,
            includeSelectAllOption: true,
            selectAllText: true,

            buttonText: function(options) {
                if (options.length == 0) {
                    return element.data()['placeholder'] + ' <b class="caret"></b>';
                } else if (options.length > 1) {
                    return _.first(options).text 
                    + ' + ' + (options.length - 1)
                    + ' more selected <b class="caret"></b>';
                } else {
                    return _.first(options).text
                    + ' <b class="caret"></b>';
                }
            },


            // Replicate the native functionality on the elements so
            // that angular can handle the changes for us.
            onChange: function (optionElement, checked) {
                optionElement.removeAttr('selected');
                if (checked) {
                    optionElement.attr('selected', 'selected');
                }
                element.change();
            }

        });

        element.multiselect('rebuild');

        // Watch for any changes to the length of our select element
        scope.$watch(function () {
            return element[0].length;
        }, function () {
            element.multiselect('rebuild');
        });
        
        // Watch for any changes from outside the directive and refresh
        scope.$watch(attributes.ngModel, function () {
            element.multiselect('refresh');
        });
    }
});
*/

app.filter('spectraFilter', function() {

});