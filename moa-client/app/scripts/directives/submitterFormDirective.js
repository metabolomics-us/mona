/**
 * Created by Gert on 5/28/2014.
 */
app.directive('submitterForm', function() {
	return {
		restrict: "A",
		replace: true,
		templateUrl: '/views/submitters/template/createUpdateForm.html'

	};
});
