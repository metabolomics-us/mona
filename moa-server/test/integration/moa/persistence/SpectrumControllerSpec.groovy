package moa.persistence

import grails.test.spock.IntegrationSpec
import moa.Spectrum
import spock.lang.Ignore

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@Ignore
class SpectrumControllerSpec extends IntegrationSpec {

	SpectrumController spectrumController = new SpectrumController()
	/**
	 * a simple spectrum
	 */

	def setup() {
	}

	def cleanup() {
		Spectrum.list().each {
			it.delete()
		}
	}

	@Ignore
	void "define a new spectrum"() {

		when:
		spectrumController.request.json = """
{
  "biologicalCompound": {
    "names": [
      {
        "name": "N-acetyl-L-glutamic acid major TMS3"
      }
    ],
    "metaData": [],
    "molFile": "\\n  CDK     0717141641\\n\\n  1  0  0  0  0  0  0  0  0  0999 V2000\\n    0.0000    0.0000    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\nM  END\\n"
  },
  "chemicalCompound": {
    "names": [
      {
        "name": "N-acetyl-L-glutamic acid major TMS3"
      }
    ],
    "molFile": "\\n  CDK     0717141641\\n\\n  1  0  0  0  0  0  0  0  0  0999 V2000\\n    0.0000    0.0000    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\\nM  END\\n"
  },
  "tags": [
    {
      "text": "imported"
    },
    {
      "text": "library"
    },
    {
      "text": "msp"
    }
  ],
  "metaData": [
    {
      "name": "Retention Index",
      "value": "592501",
      "category": "none"
    },
    {
      "name": "MW",
      "value": " 405",
      "category": "none"
    },
    {
      "name": "CASNO",
      "value": " 1188370",
      "category": "none"
    },
    {
      "name": "Num Peaks",
      "value": " 189",
      "category": "spectral properties"
    },
    {
      "name": "origin",
      "value": "small.msp"
    }
  ],
  "spectrum": "85:78 86:26 87:20 88:17 89:18 90:4 91:4 92:2 93:6 94:3 95:26 96:6 97:15 98:37 99:32 100:97 101:112 102:26 103:47 104:6 105:9 106:1 107:1 108:27 109:4 110:7 111:17 112:29 113:19 114:46 115:41 116:305 117:83 118:19 119:17 120:2 121:1 124:3 125:4 126:35 127:14 128:81 129:173 130:44 131:66 132:25 133:136 134:20 135:16 136:3 137:1 138:5 139:3 140:53 141:11 142:82 143:34 144:21 145:6 146:3 147:400 148:67 149:102 150:14 151:8 152:3 153:13 154:24 155:20 156:999 157:203 158:74 159:20 160:5 161:4 162:4 163:15 164:3 165:1 166:2 168:4 169:8 170:65 171:56 172:17 173:6 174:7 175:5 176:3 177:3 178:1 179:1 180:1 181:1 182:24 183:9 184:232 185:40 186:39 187:7 188:5 189:11 190:6 191:19 192:3 193:2 196:1 197:20 198:235 199:42 200:20 201:3 202:5 203:17 204:93 205:21 206:10 207:3 208:1 210:3 214:12 215:4 216:10 217:4 218:25 219:6 220:2 221:5 222:1 223:1 226:7 227:2 228:11 229:3 230:51 231:19 232:10 233:3 234:1 242:1 243:2 244:292 245:69 246:44 247:8 248:3 249:1 256:2 257:1 258:7 259:4 260:5 261:28 262:6 263:3 272:8 273:4 274:137 275:34 276:15 277:3 287:32 288:121 289:31 290:12 291:2 300:16 301:4 302:2 315:33 316:11 317:4 318:2 348:27 349:16 350:8 351:2 362:17 363:10 364:4 365:1 389:2 390:35 391:15 392:7 393:1 405:7 406:3 407:1",
  "comments": "this spectra was generated using the MSP Service, with an existing uploaded files",
  "submitter": {
    "emailAddress": "wohlgemuth@ucdavis.edu",
    "firstName": "Gert",
    "id": 1,
    "lastName": "Wohlgemuth"
  }
}
"""
		spectrumController.save()

		then:
		Spectrum.list().size() == 1

		Spectrum spec = Spectrum.list().get(0)

		spec.chemicalCompound.inchiKey == "OTMSDBZUPAUEDD-UHFFFAOYSA-N"
		spec.biologicalCompound.inchiKey == "OTMSDBZUPAUEDD-UHFFFAOYSA-N"

		spec.chemicalCompound.names.contains("N-acetyl-L-glutamic acid major TMS3")
		spec.biologicalCompound.names.contains("N-acetyl-L-glutamic acid major TMS3")


	}
}
