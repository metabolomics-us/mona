/**
 * Created by wohlgemuth on 11/6/14.
 */

import {Pipe, PipeTransform} from '@angular/core';
import {environment} from '../../environments/environment';
import {Spectrum} from '../services/persistence/spectrum.resource';

@Pipe({
    name: 'curlPipe',
    pure: false
})
export class CurlPipe implements PipeTransform {
    constructor(private spectrum: Spectrum) {
    }

    transform(input: any): any {
        const host = environment.REST_BACKEND_SERVER === '' ? location.origin : environment.REST_BACKEND_SERVER;

        const query = input && typeof(input.query) === 'string' ? input.query.replace(/"/g, '\\"') : '';

        if (query !== '') {
            let cmd = 'curl "' + host + '/rest/spectra/search?';

            if (query !== '') {
              const cleaned = this.spectrum.cleanParameters({query});
              cmd += cleaned;
            }

            // Add default size and page parameters, as well as closing quote "
            cmd += '&size=1000&page=0"';

            return cmd;
        } else {
            return 'curl "' + host + '/rest/spectra?size=1000&page=0"';
        }
    }
}
