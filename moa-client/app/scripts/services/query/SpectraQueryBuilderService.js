/**
 * Created by wohlgemuth on 7/10/14.
 */

/**
 * a service to build our specific query object to be executed against the Spectrum service, mostly required for the modal query dialog and so kinda special
 *
 */
app.service('SpectraQueryBuilderService', function (QueryCache, $log) {
    /**
     * provides us with the current query
     * @returns {*|QueryCache.spectraQuery}
     */
    this.getQuery = function () {
        var query =  QueryCache.getSpectraQuery();

        if(query == null){
            query = this.prepareQuery();
        }

        return query;
    };

    /**
     * prepares an empty query to avoid null pointer exceptions
     */
    this.prepareQuery = function () {
        var query = {
            compound: {},
            metadata: [],
            tags: []
        };

        QueryCache.setSpectraQuery(query);

        return query;
    };


    /**
     * updates a pre-compiled query with the given
     */
    this.updateQuery = function (query, metadata, tags, compiled) {

        //no query assigned, use the one from the cache
        if(compiled == null){
            compiled = this.getQuery();
        }

        // Get all metadata in a single dictionary
        var meta = {};
        Object.keys(metadata).forEach(function (element) {
            for (var i = 0; i < metadata[element].length; i++) {
                meta[metadata[element][i].name] = metadata[element][i];
            }
        });


        // Handle all query components
        Object.keys(query).forEach(function (element) {
            if (element === "nameFilter" && query[element]) {
                compiled.compound.name = {like: query[element]};
            }

            else if (element === "inchiFilter" && query[element]) {
                if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(query[element])) {
                    compiled.compound.inchiKey = {eq: query[element]};
                } else {
                    compiled.compound.inchiKey = {like: '%' + query[element] + '%'};
                }
            }

            // Ignore tolerance values
            else if (element.indexOf("_tolerance", element.length - 10) !== -1) {
                //nothing to see here
            }

            else {
                if (query[element]) {
                    if (meta[element].type === "double") {
                        if ((element + "_tolerance") in query && query[element + "_tolerance"]) {
                            var min = parseFloat(query[element]) - parseFloat(query[element + "_tolerance"]);
                            var max = parseFloat(query[element]) + parseFloat(query[element + "_tolerance"]);
                            compiled.metadata.push({name: element, value: {between: [min, max]}});
                        } else
                            compiled.metadata.push({name: element, value: {eq: parseFloat(query[element])}});
                    } else {
                        compiled.metadata.push({name: element, value: {eq: query[element]}});
                    }
                }
            }
        });


        // Add all tags to query
        for (var i = 0; i < tags.length; i++) {
            compiled.tags.push(tags[i]);
        }

        QueryCache.setSpectraQuery(compiled);

        return compiled;
    };

    /**
     * compiles our dedicated query to execute it against another service
     * @param query
     * @param metadata
     * @param tags
     */
    this.compileQuery = function (query, metadata, tags) {
        return this.updateQuery(query, metadata, tags, this.prepareQuery());
    };

    /**
     * removes a tag from a query
     * @param tag
     */
    this.removeTagFromQuery = function (tag) {
        var query = this.getQuery();
        var index = query.tags.indexOf(tag);

        if (index > -1) {
            query.tags.splice(query.tags.indexOf(tag), 1);
        }

        QueryCache.setSpectraQuery(query);
    };


    /**
     * adds a tag to the query
     * @param tag
     */
    this.addTagToQuery = function (tag) {
        this.removeTagFromQuery(tag);
        var query = this.getQuery();

        query.tags.push(tag);

        QueryCache.setSpectraQuery(query);
    };

    /**
     * removes metadata from teh query
     * @param metadata
     */
    this.removeMetaDataFromQuery = function (metadata) {
        var query = this.getQuery();

        if (query.metadata == null) {
            return
        }

        //create a metadata query object

        for (var i = 0; i < query.metadata.length; i++) {
            if (query.metadata[i].name == metadata.name) {
                query.metadata.splice(i, 1);
                QueryCache.setSpectraQuery(query);
                return;
            }
        }
    };

    /**
     * adds further metadata to the query
     * @param metadata
     */
    this.addMetaDataToQuery = function (metadata) {
        this.removeMetaDataFromQuery(metadata);

        var query = this.getQuery();

        if (query.metadata == null) {
            query.metadata = [];
        }
        //add a metadata query object

        var meta = {'name': metadata.name, 'value': {'eq': metadata.value}};

        if (metadata.unit != null) {
            meta.unit = {'eq': metadata.unit};
        }

        query.metadata.push(meta);

        QueryCache.setSpectraQuery(query);
    };
});