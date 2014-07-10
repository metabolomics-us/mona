package moa.query

import grails.converters.JSON
import moa.MetaDataValue
import moa.Spectrum
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.StringMetaDataValue
import org.hibernate.QueryException
import util.MetaDataValueHelper

class SpectraQueryController {

    static responseFormats = ['json']


    def beforeInterceptor = {
        log.info("$params - $request.JSON")
    }

    /**
     * search function for the query controller
     */
    def search() {

        def json = request.JSON


        def result = Spectrum.createCriteria().list() {

            //we have compound information available
            if (json.compound) {
                or {
                    biologicalCompound {

                        if (json.compound.name) {
                            names {

                                if (json.compound.name.like) {
                                    like('name', json.compound.name.like)
                                } else if (json.compound.name.eq) {
                                    eq('name', json.compound.name.eq)
                                } else {
                                    throw new QueryException("invalid query term: ${json.compound.name.eq}")
                                }
                            }
                        }
                    }
                    chemicalCompound {

                        if (json.compound.name) {
                            names {

                                if (json.compound.name.like) {
                                    like('name', json.compound.name.like)
                                } else if (json.compound.name.eq) {
                                    eq('name', json.compound.name.eq)
                                } else {
                                    throw new QueryException("invalid query term: ${json.compound.name.eq}")
                                }
                            }
                        }
                    }
                }
            }

            //we have metaData queries
            if (json.metaData) {

                //go over each metadata definition
                json.metaData.eachWithIndex { current, index ->

                    metaData {

                            //our metadata values

                            //our meta data parent object which has to have a specific name
                            metaData {
                                eq("name", current.name)
                            }

                            //equals condition and calculate the actual implementation
                            if (current.value.eq) {
                                def impl = estimateMetaDataValueImpl(current.value.eq.toString())
                                //our actual value
                                eq(impl.name, impl.value)
                            } else {
                                throw new QueryException("invalid query term: ${current.value.eq}")
                            }
                        }
                    }
                }
            }
        }


        log.info("received results from query: ${result.size()}")
        render(result as JSON)
    }

    /**
     * returns a map with exactly two keys
     * @param content
     * @return
     */
    private Map estimateMetaDataValueImpl(String content) {

        def result = [:];

        MetaDataValue value = MetaDataValueHelper.getValueObject(content)

        if (value instanceof BooleanMetaDataValue) {

            result.name = "booleanValue"
            result.value = value.booleanValue
        } else if (value instanceof DoubleMetaDataValue) {

            result.name = "doubleValue"
            result.value = value.doubleValue
        } else if (value instanceof StringMetaDataValue) {
            result.name = "stringValue"
            result.value = value.getStringValue()
        }

        return result;
    }
}
