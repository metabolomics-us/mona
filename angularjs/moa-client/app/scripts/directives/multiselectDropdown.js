app.directive('multiselectDropdown', function() {
    return function(scope, element, attributes) {
        // http://stackoverflow.com/questions/16933324
        //element = $(element[0]);

        element.multiselect({
            buttonClass: 'btn btn-mini btn-primary',
            buttonWidth: '150px',
            buttonContainer: '<div class="btn-group" />',
            maxHeight: 250,
            enableFiltering: true,
            enableCaseInsensitiveFiltering: true,
            includeSelectAllOption: true,

            buttonText: function(options) {
                return element.data()['placeholder'] + ' <b class="caret"></b>';
            },

            // Replicate the native functionality on the elements so
            // that angular can handle the changes for us.
            onChange: function (optionElement, checked) {
                optionElement.removeAttr('selected');
                if (checked)
                    optionElement.prop('selected', 'selected');
                element.change();

                console.log(optionElement)
                console.log(checked)
            }

        });

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