package moa.persistence

import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import moa.MetaDataValue
import moa.Spectrum
import org.junit.Ignore
import moa.SupportsMetaData
import moa.Tag
import org.apache.log4j.Logger

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class SpectrumControllerSpec extends IntegrationSpec {
    Logger log = Logger.getLogger(this.class)
    SpectrumController spectrumController = new SpectrumController()


    def setup() {}

    def cleanup() {
        Spectrum.list().each {
            it.delete()
        }
    }

    void "save a simple spectrum"() {
        when:
        spectrumController.request.json = """
{
    "biologicalCompound": {
        "names": [ "Alanine", "DL-Alanine" ],
        "inchi": "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)",
        "inchiKey": "QNAYBMKLOCPYGJ-UHFFFAOYSA-N",
        "metaData": [],

    },
    "chemicalCompound": {
        "names": [ "Alanine", "DL-Alanine" ],
        "inchi": "InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)",
        "inchiKey": "QNAYBMKLOCPYGJ-UHFFFAOYSA-N",
        "metaData": []
    },
    "tags": [
        { "text": "Massbank" },
        { "text": "experimental" }
    ],
    "metaData": [
        { "name": "accession", "value":" JP010310" },
        { "name": "authors", "value": "Tajima S, Gunma College of Technology" },
        { "name": "license", "value": "CC BY-SA" },
        { "name": "exact mass", "value": "89.04768" },
        { "name": "instrument", "value": "Hitachi M-80A" },
        { "name": "instrument type", "value": "EI-B" },
        { "name": "ms type", "value": "MS", "category":"mass spectrometry" },
        { "name": "ion mode", "value": "POSITIVE", "category":"mass spectrometry" },
        { "name": "ionization energy", "value": "70 eV", "category":"mass spectrometry" },
        { "name": "precursor type", "value": "[M]+", "category":"focused ion" },
        { "name": "origin", "value": "JP010310.txt" }
    ],
    "spectrum": "14:10 15:58 18:255 26:16 27:54 28:149 29:22 30:11 40:20 41:34 42:121 43:36 44:999 45:49 46:21 74:14 90:47",
    "comments": [],
    "submitter": {
        "firstName": "Gert",
        "lastName": "Wohlgemuth",
        "institution": "University of California, Davis",
        "emailAddress": "wohlgemuth@ucdavis.edu"
    }
}
"""
        spectrumController.save()

        then:
        Spectrum.count() == 1
        MetaDataValue.count() == 11
        Tag.count() == 2


        Spectrum s = Spectrum.findById(Spectrum.first().id)
        println s as JSON

        s.chemicalCompound.names.contains('Alanine')
        s.chemicalCompound.names.contains('DL-Alanine')
        s.chemicalCompound.inchi == 'InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)'
        s.chemicalCompound.inchiKey == 'QNAYBMKLOCPYGJ-UHFFFAOYSA-N'
        s.chemicalCompound.metaData.size() == 0

        s.biologicalCompound.names.contains('Alanine')
        s.biologicalCompound.names.contains('DL-Alanine')
        s.biologicalCompound.inchi == 'InChI=1S/C3H7NO2/c1-2(4)3(5)6/h2H,4H2,1H3,(H,5,6)'
        s.biologicalCompound.inchiKey == 'QNAYBMKLOCPYGJ-UHFFFAOYSA-N'
        s.biologicalCompound.metaData.size() == 0

        s.submitter.firstName == "Gert"
        s.submitter.lastName == "Wohlgemuth"
        s.submitter.institution == "University of California, Davis"
        s.submitter.emailAddress == "wohlgemuth@ucdavis.edu"

        s.ions.size() == 2

        s.metaData.size() == 11
        s.tags.size() == 2
    }

//    void "save a more complex spectrum"() {
//        when:
//        spectrumController.request.json = """
//{
//    "biologicalCompound":{
//        "names":[
//            "Cyclopamine",
//            "11-deoxojervine",
//            "(3beta,22S,23R)-17,23-Epoxyveratraman-3-ol"
//        ],
//        "inchi":"InChI=1S/C27H41NO2/c1-15-11-24-25(28-14-15)17(3)27(30-24)10-8-20-21-6-5-18-12-19(29)7-9-26(18,4)23(21)13-22(20)16(27)2/h5,15,17,19-21,23-25,28-29H,6-14H2,1-4H3/t15-,17+,19-,20-,21-,23-,24+,25-,26-,27-/m0/s1",
//        "metaData":[
//
//        ]
//    },
//    "chemicalCompound":{
//        "names":[
//            "Cyclopamine",
//            "11-deoxojervine",
//            "(3beta,22S,23R)-17,23-Epoxyveratraman-3-ol"
//        ],
//        "inchi":"InChI=1S/C27H41NO2/c1-15-11-24-25(28-14-15)17(3)27(30-24)10-8-20-21-6-5-18-12-19(29)7-9-26(18,4)23(21)13-22(20)16(27)2/h5,15,17,19-21,23-25,28-29H,6-14H2,1-4H3/t15-,17+,19-,20-,21-,23-,24+,25-,26-,27-/m0/s1",
//        "metaData":[
//
//        ]
//    },
//    "tags":[
//        {
//            "text":"Massbank"
//        },
//        {
//            "text":"experimental"
//        }
//    ],
//    "metaData":[
//        {
//            "name":"accession",
//            "value":"BSU00002"
//        },
//        {
//            "name":"authors",
//            "value":"Chandler, C. and Habig, J. Boise State University"
//        },
//        {
//            "name":"license",
//            "value":"CC BY-SA"
//        },
//        {
//            "name":"copyright",
//            "value":"Chandler, C., Habig, J. and McDougal O. Boise State University"
//        },
//        {
//            "name":"compound class",
//            "value":"Natural Product; Alkaloid"
//        },
//        {
//            "name":"exact mass",
//            "value":"411.31372"
//        },
//        {
//            "name":"cas",
//            "value":"4449-51-8",
//            "category":"link"
//        },
//        {
//            "name":"chemspider",
//            "value":"391275",
//            "category":"link"
//        },
//        {
//            "name":"kegg",
//            "value":"C10798",
//            "category":"link"
//        },
//        {
//            "name":"knapsack",
//            "value":"C00002245",
//            "category":"link"
//        },
//        {
//            "name":"nikkaji",
//            "value":"J15.137H",
//            "category":"link"
//        },
//        {
//            "name":"pubchem",
//            "value":"12981",
//            "category":"link"
//        },
//        {
//            "name":"instrument",
//            "value":"Bruker maXis ESI-QTOF"
//        },
//        {
//            "name":"instrument type",
//            "value":"LC-ESI-QTOF"
//        },
//        {
//            "name":"ms type",
//            "value":"MS2",
//            "category":"mass spectrometry"
//        },
//        {
//            "name":"ion mode",
//            "value":"POSITIVE",
//            "category":"mass spectrometry"
//        },
//        {
//            "name":"precursor m/z",
//            "value":"412.3",
//            "category":"focused ion"
//        },
//        {
//            "name":"precursor type",
//            "value":"[M+H]+",
//            "category":"focused ion"
//        },
//        {
//            "name":"origin",
//            "value":"BSU00002.txt"
//        }
//    ],
//    "spectrum": "67.1:996 81.1:1626 84.1:3012 85.1:2434 93.1:1034 96.1:1039 102.1:2334 105.1:1791 107.1:2824 109.1:25019 110.1:2155 112.1:1817 114.1:23944 115.1:1409 119.1:3802 121.1:3241 124.1:1738 125.1:925 126.1:10185 127.1:879 129.1:1117 131.1:5209 133.1:20975 134.1:2073 135.1:5153 140.1:6402 143.1:9554 144.1:1549 145.1:30697 146.1:3414 147.1:9348 148.1:1074 149.1:1660 150.1:1599 151.1:2633 155.1:3688 156.1:1907 157.1:34548 158.1:4548 159.1:29706 160.1:3578 161.1:11529 162.1:1281 163.1:2758 167.1:838 169.1:5590 170.1:1295 171.1:19552 172.1:2831 173.1:11400 174.1:1483 175.1:1981 177.1:903 181.1:2000 182.1:1328 183.1:7538 184.1:2062 185.1:8414 186.1:1277 187.1:2641 195.1:4841 196.1:1936 197.1:8763 198.1:1615 199.1:4771 200.2:955 201.2:887 207.1:1009 209.1:3897 210.1:1194 211.1:6018 212.2:974 213.2:12177 214.2:1848 221.1:1083 222.1:980 223.1:2726 224.2:878 225.2:2094 227.2:918 235.1:1483 237.2:2445 239.2:2158 249.2:1048 251.2:2010 253.2:2741 264.2:949 269.2:976 279.2:3458 280.2:957 281.2:1202 287.2:1457 295.2:2005 297.2:1490 321.2:4482 322.2:1298 377.3:1510 394.3:1899 412.3:3904 413.3:1136",
//    "comments": [
//        {
//            "comment": "Data obtained from a cyclopamine standard purchased from Logan Natural Products, Logan, Utah USA."
//        }
//    ],
//    "submitter": {
//        "firstName": "Gert",
//        "lastName": "Wohlgemuth",
//        "institution": "University of California, Davis",
//        "emailAddress": "wohlgemuth@ucdavis.edu"
//    }
//}
//"""
//        spectrumController.save()
//
//        then:
//        Spectrum.count() == 1
//        MetaDataValue.count() == 13
//
//        Spectrum s = Spectrum.list().get(0)
//        println s as JSON
//
//        s.chemicalCompound.inchi == 'InChI=1S/C27H41NO2/c1-15-11-24-25(28-14-15)17(3)27(30-24)10-8-20-21-6-5-18-12-19(29)7-9-26(18,4)23(21)13-22(20)16(27)2/h5,15,17,19-21,23-25,28-29H,6-14H2,1-4H3/t15-,17+,19-,20-,21-,23-,24+,25-,26-,27-/m0/s1'
//        s.biologicalCompound.inchi == 'InChI=1S/C27H41NO2/c1-15-11-24-25(28-14-15)17(3)27(30-24)10-8-20-21-6-5-18-12-19(29)7-9-26(18,4)23(21)13-22(20)16(27)2/h5,15,17,19-21,23-25,28-29H,6-14H2,1-4H3/t15-,17+,19-,20-,21-,23-,24+,25-,26-,27-/m0/s1'
//
//        //s.metaData.size() == 19
//    }
}
