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
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        }
    )
});
