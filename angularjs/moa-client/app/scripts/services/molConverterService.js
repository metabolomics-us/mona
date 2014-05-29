
/**
 * Created by Gert on 5/28/2014.
 */

'use strict';

app.service('MolConverter', function ($http,REST_BACKEND_SERVER) {

    /**
     * converts the molecule to an inchi code
     * @param molecule
     * @returns {IHttpPromise<T>}
     */
    this.convertToInchiKey = function (molecule) {
        return $http({
                method: 'POST',
                url: REST_BACKEND_SERVER +'/rest/util/converter/molToInchi',
                data: {mol: molecule}
            }
        ).success(function (data, status, headers, config) {
                alert(data);
            }).
            error(function (data, status, headers, config) {
            });
    }
});
