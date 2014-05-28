/**
 * all our services are defined in this section
 */

/**
 * storing and retrieving submitters from the backend
 */
massspecsOfAmerica.factory("Submitter", function ($resource) {
    return $resource(
        "/rest/submitters/:id",
        {id: "@id"},
        {
            'update': {
                method: 'PUT'

            }
        }
    )
});

/**
 * does an http ajax call to the server and tries to provide us with a valid inchi key for the given mol file
 */
massspecsOfAmerica.service("MolConverter", function ($http) {

    /**
     * converts the molecule to an inchi code
     * @param molecule
     * @returns {IHttpPromise<T>}
     */
    this.convertToInchiKey = function (molecule) {
        return $http({
                method: 'POST',
                url: '/rest/util/converter/molToInchi',
                data: {mol: molecule}
            }
        ).success(function (data, status, headers, config) {
                alert(data);
            }).
            error(function (data, status, headers, config) {
            });
    }
});