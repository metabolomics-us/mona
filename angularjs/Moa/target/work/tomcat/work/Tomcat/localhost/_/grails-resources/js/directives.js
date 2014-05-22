/**
 * this defines a simple data entry form for our sbumitters to create and update them
 */
massspecsOfAmerica.directive("submitterForm", function() {
    return {
        restrict: "E",
        replace: true,
        templateUrl: "/partial/submitters/template/createUpdateForm.html"
    };
});