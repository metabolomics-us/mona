/**
 * Created by wohlgemuth on 6/11/14.
 */

import * as angular from 'angular';

class SpectrumService {
	private static $inject = ['REST_BACKEND_SERVER', '$http'];
	private REST_BACKEND_SERVER;
	private $http;

	constructor(REST_BACKEND_SERVER, $http) {
		this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
		this.$http = $http;
	}

	get = (id) => {
		return this.$http.get(this.REST_BACKEND_SERVER + '/rest/spectra/' + id);
	}

	update = (data: any) => {
		return this.$http.put(this.REST_BACKEND_SERVER + '/rest/spectra/' + data.id, data);
	}

	searchSpectra = (data) => {
		const config = {
			params: data
		};
		if(data.endpoint === undefined) {
			return this.$http.get(this.REST_BACKEND_SERVER + '/rest/spectra', config);
		}
		else{
			return this.$http.get(this.REST_BACKEND_SERVER + '/rest/spectra/' + data.endpoint, config);
		}

	}

	searchSpectraCount = (data: any) => {
		const config = {
			isArray: true,
			transformResponse:  (d: any) => {
				return {count: d};
			}
		}
		return this.$http.get(this.REST_BACKEND_SERVER + '/rest/spectra/search/' + data.endpoint, config)
	}

	searchSimilarSpectra = (data: any) => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			},
			transformResponse:  (d) => {
				let data: any;
				data = angular.fromJson(d).map((spectrum) => {
					spectrum.hit.similarity = spectrum.score;
					return spectrum.hit;
				});
				return data;
			}

		};
		return this.$http.post(this.REST_BACKEND_SERVER + '/rest/similarity/search', data, config);
	}

	batchSave = (token) => {
		const config = {
			headers: {
				'Content-Type' : 'application/json',
				'Authorization': 'bearer ' + token
			}
		};
		return this.$http.post(this.REST_BACKEND_SERVER + '/rest/spectra', token, config);
	}

	score = (data) => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		this.$http.get(this.REST_BACKEND_SERVER + '/rest/spectra/score/' + data.id + '/explain', config);
	}

	curate = (id: any) => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.$http.get(this.REST_BACKEND_SERVER + '/rest/spectra/curate' + id, config);
	}

	curateSpectraByQuery = (data: any) => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.$http.post(this.REST_BACKEND_SERVER + '/rest/curate/spectra/curateAllByQuery', data, config);
	}

	associateSpectraByQuery = (data: any) => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.$http.post(this.REST_BACKEND_SERVER + '/rest/spectra/associate/allByQuery', data, config);
	}
}

angular.module('moaClientApp')
	.service('Spectrum', SpectrumService);

