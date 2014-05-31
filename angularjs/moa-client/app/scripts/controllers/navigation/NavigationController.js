/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

app.controller('NavigationController', function ($scope, $location) {
	$scope.navClass = function (page) {
		var currentRoute = $location.path().substring(1) || 'home';
		return page === currentRoute ? 'active' : '';
	};
});
