/**
 * Created by Gert on 5/28/2014.
 */
app.directive('submitterForm', function() {
	return {
		restrict: "E",
		replace: true,
		templateUrl: '/views/submitters/template/createUpdateForm.html'
	};
});