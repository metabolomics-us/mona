/**
 * Created by wohlgemuth on 11/6/14.
 */

import {Pipe, PipeTransform} from '@angular/core';
import {environment} from '../../environments/environment';

@Pipe({
    name: 'curlPipe',
    pure: false
})
export class CurlPipe implements PipeTransform {
    transform(input: any): any {
        const host = environment.REST_BACKEND_SERVER === '' ? location.origin : environment.REST_BACKEND_SERVER;

        const query = input && typeof(input.query) === 'string' ? input.query.replace(/"/g, '\\"') : '';
        const text = input && typeof(input.text) === 'string' ? input.text : '';

        if (query !== '' || text !== '') {
            let cmd = 'curl "' + host + '/rest/spectra/search?';

            if (query !== '') {
                cmd += 'query=' + query;

                if (text !== '') {
                    cmd += '&text=' + text;
                }
            } else {
                cmd += 'text=' + text;
            }

            cmd += '"';

            return cmd;
        } else {
            return 'curl "' + host + '/rest/spectra"';
        }
    }
}
