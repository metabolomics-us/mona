package moa.server

import grails.transaction.Transactional

@Transactional
class MetaDataDictionaryService {

    /**
     * attempts to convert the provided name to a better name for a metadata object internally
     * @param name
     * @return
     */
    String convertNameToBestMatch(String name) {

        return name;
    }
}
