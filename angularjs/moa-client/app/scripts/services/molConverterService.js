/**
 * Created by Gert on 5/28/2014.
 */

'use strict';

app.service('MolConverter', function ($http, REST_BACKEND_SERVER, $q) {

		/**
		 * converts the molecule to an inchi code, which needs to be used in your promise
		 * @param molecule
		 * @returns {IHttpPromise<T>}
		 */
		this.convertToInchiKey = function (molecule) {

			var deferred = $q.defer();

			$http.post(REST_BACKEND_SERVER + '/rest/util/converter/molToInchi', {
					params: {
						data: {mol: molecule}
					}
				}
			).success(function(result){
				deferred.resolve(result);
			});

			return deferred.promise;
		}
	}
);
