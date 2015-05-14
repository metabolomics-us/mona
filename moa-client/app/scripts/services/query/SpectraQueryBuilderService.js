/**
 * Created by wohlgemuth on 7/10/14.
 */

/**
 * a service to build our specific query object to be executed against the Spectrum service, mostly required for the modal query dialog and so kinda special
 *
 */
app.service('SpectraQueryBuilderService', function (QueryCache, MetadataService) {
    /**
     * provides us with the current query
     * @returns {*|QueryCache.spectraQuery}
     */
    this.getQuery = function () {
        var query = QueryCache.getSpectraQuery();

        if (query == null) {
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
    this.updateQuery = function (query, tags, compiled) {

        //no query assigned, use the one from the cache

        if (compiled == null) {
            compiled = this.getQuery();
        }

        if (tags == null) {
            tags = [];
        }


        // Get all metadata in a single dictionary
        var meta = {};

        MetadataService.metadata(
            function (data) {
                for (var i = 0; i < data.length; i++) {
                    meta[data[i].name] = data[i];
                }
            },
            function (error) {
                $log.error('metadata failed: ' + error);
            }
        );


        // Handle all query components
        Object.keys(query).forEach(function (element) {
            if (element === "submitter" && query[element]) {
                compiled.submitter = query[element];
            }

            if (element === "nameFilter" && query[element]) {
                compiled.compound.name = {like: query[element]};
            }

            else if (element === "inchiFilter" && query[element]) {
                if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(query[element])) {
                    compiled.compound.inchiKey = {eq: query[element]};
                } else {
                    compiled.compound.inchiKey = {like: query[element]};
                }
            }

            // Ignore tolerance values
            else if (element.indexOf("_tolerance", element.length - 10) !== -1) {
                //nothing to see here
            }

            else {
                if (query[element]) {
                    if (meta.hasOwnProperty(element) && meta[element].type === "double") {
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
    this.compileQuery = function (query, tags) {
        return this.updateQuery(query, tags, this.prepareQuery());
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

        if (query.compound.tags) {
            index = query.compound.tags.indexOf(tag);

            if (index > -1) {
                query.tags.splice(query.compound.tags.indexOf(tag), 1);
            }
        }

        QueryCache.setSpectraQuery(query);
    };

    /**
     * adds the given id || hash to the query
     * @param id
     */
    this.addSpectraIdToQuery = function (id) {

        var query = this.getQuery();

        if (!query.id) {
            query.id = [];
        }

        query.id.push(id);

        QueryCache.setSpectraQuery(query);

    };
    /**
     * removes this spectra id from the query
     * @param id
     */
    this.removeSpectraIdFromQuery = function (id) {

        var query = this.getQuery();

        if (query.id) {

            //create a metadata query object

            for (var i = 0; i < query.id.length; i++) {
                if (query.id[i] == id) {
                    query.id.splice(i, 1);
                }
            }
        }

        QueryCache.setSpectraQuery(query);

    };

    /**
     * adds a tag to the query
     * tag: {
     *          name :  {
     *              "eq" : "tada"
     *          }
     *      }
     *
     * @param tag
     * @param isCompound is this a tag of a compound
     */
    this.addTagToQuery = function (tag, isCompound, includeExclude) {
        if (tag) {
            this.removeTagFromQuery(tag);
            var query = this.getQuery();

            if (isCompound) {
                if (!query.compounds.tags) {
                    query.compounds.tags = [];
                }
                query.compounds.tags.push(tag);
            }
            else {

                if(includeExclude == '+'){

                    query.tags.push(
                        {
                            name:{
                                eq:tag
                            }
                        }
                    );
                }
                else if(includeExclude == '-'){

                    query.tags.push(
                        {
                            name:{
                                ne:tag
                            }
                        }
                    );
                }

            }
            QueryCache.setSpectraQuery(query);
        }
    };

    /**
     * resets all tags
     */
    this.clearTagsFromQuery = function () {
        var query = this.getQuery();

        query.tags = [];

        QueryCache.setSpectraQuery(query);
    };

    /**
     * removes metadata from teh query
     * @param metadata
     */
    this.removeMetaDataFromQuery = function (metadata) {
        var query = this.getQuery();

        if (query.metadata) {

            //create a metadata query object

            for (var i = 0; i < query.metadata.length; i++) {
                if (query.metadata[i].name == metadata.name) {
                    query.metadata.splice(i, 1);
                }
            }
        }

        if (query.compound.metadata) {

            for (var i = 0; i < query.compound.metadata.length; i++) {
                if (query.compound.metadata[i].name == metadata.name) {
                    query.compound.metadata.splice(i, 1);
                }
            }
        }
        QueryCache.setSpectraQuery(query);

    };

    /**
     * adds further metadata to the query
     * @param metadata
     * @param compound
     */
    this.addMetaDataToQuery = function (metadata, compound) {
        if (metadata) {
            if (metadata.name && metadata.name != '') {
                this.removeMetaDataFromQuery(metadata);


                var query = this.getQuery();

                if (compound == null) {
                    compound = false;
                }

                if (query.metadata == null) {
                    query.metadata = [];
                }

                //build query data object
                var meta = {'name': metadata.name, 'value': {'eq': metadata.value}};

                if (metadata.unit != null) {
                    meta.unit = {'eq': metadata.unit};
                }

                if (compound) {
                    if (query.compound.metadata == null) {
                        query.compound.metadata = [];
                    }
                    query.compound.metadata.push(meta);

                }

                else {
                    //add a metadata query object
                    query.metadata.push(meta);
                }

                QueryCache.setSpectraQuery(query);
            }
        }
    };
});