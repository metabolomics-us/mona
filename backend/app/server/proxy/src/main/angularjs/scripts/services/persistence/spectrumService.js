/**
 * Created by wohlgemuth on 6/11/14.
 */

(function () {
	'use strict';
	spectrum.$inject = ['$resource', 'REST_BACKEND_SERVER'];
	angular.module('moaClientApp')
		.factory('Spectrum', spectrum);

	/* @ngInject */
	function spectrum($resource, REST_BACKEND_SERVER) {

		/**
		 * creates a new resources, we can work with
		 */

		return $resource(
			REST_BACKEND_SERVER + '/rest/spectra/:id',
			{id: "@id", offset: "@offset"},
			{
				'update': {
					method: 'PUT'
				},
				'searchSpectra': {
					url: REST_BACKEND_SERVER + '/rest/spectra/:endpoint',
					method: 'GET',
					isArray: true
				},
				'searchSpectraCount': {
					url: REST_BACKEND_SERVER + '/rest/spectra/search/:endpoint',
					method: 'GET',
					isArray: false,
					transformResponse: function (data) {
						return {count: data};
					}
				},
				'searchSimilarSpectra': {
					url: REST_BACKEND_SERVER + '/rest/similarity/search',
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					isArray: true,
					transformResponse: function (data) {
					    data = angular.fromJson(data).map(function (spectrum) {
                            spectrum.hit.similarity = spectrum.score;
                            return spectrum.hit;
                        });
						return data;
					}
				},
				'batchSave': function (token) {
					return {
						url: REST_BACKEND_SERVER + '/rest/spectra',
						method: 'POST',
						isArray: false,
						headers: {
							'Content-Type': 'application/json',
							'Authorization': 'bearer ' + token
						}
					}
				},
				'score': {
					url: REST_BACKEND_SERVER + '/rest/spectra/score/:id/explain',
					method: 'GET',
					headers: {
						'Content-Type': 'application/json'
					},
					isArray: false
				},
				'curate': {
					url: REST_BACKEND_SERVER + '/rest/spectra/curate/:id',
					method: 'GET',
					headers: {
						'Content-Type': 'application/json'
					},
					isArray: false
				},
				'curateSpectraByQuery': {
					url: REST_BACKEND_SERVER + '/rest/curate/spectra/curateAllByQuery',
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					isArray: false
				},
				'associateSpectraByQuery': {
					url: REST_BACKEND_SERVER + '/rest/spectra/associate/allByQuery',
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					isArray: false
				},
				'getPredefinedQueries': {
					url: REST_BACKEND_SERVER + '/rest/downloads/predefined',
					method: 'GET',
					headers: {
						'Content-Type': 'application/json'
					},
					isArray: true
				}
			}
		);
	}
})();
