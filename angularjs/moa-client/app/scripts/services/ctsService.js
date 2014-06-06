/**
 * Created by Gert on 5/28/2014.
 */

'use strict';

/**
 * provides us with access to the CTS system
 */
app.service('CTSService', function ($http, $q) {

		/**
		 * converts the given molecule to an inchi key
		 * @param molecule
		 * @returns {*}
		 */
		this.getNamesForInChIKey = function (inchiKey) {

			var deferred = $q.defer();

			$http.post('http://cts.fiehnlab.ucdavis.edu/service/convert/InChIKey/Chemical%20Name/' + inchiKey
			).success(function (result) {
					deferred.resolve(result);
				});

			return deferred.promise;
		}
	}
);
