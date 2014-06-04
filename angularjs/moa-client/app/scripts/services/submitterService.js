/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

app.factory('Submitter', function ($resource, REST_BACKEND_SERVER, $http) {

	$http.defaults.useXDomain = true;

	return $resource(
			REST_BACKEND_SERVER + '/rest/submitters/:id',
		{id: "@id"},
		{
			'update': {
				method: 'PUT'

			}
		}
	);
});