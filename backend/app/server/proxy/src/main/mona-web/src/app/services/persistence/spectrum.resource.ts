/**
 * Created by wohlgemuth on 6/11/14.
 */

import {HttpClient, HttpParams} from '@angular/common/http';
import { map, first } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';

@Injectable()
export class Spectrum {
	constructor(public http: HttpClient) {
	}

	private cleanParameters(data: any): HttpParams {
		// Filter our undefined values and place the others in HttpParams value
		let params = new HttpParams();
		// Ternary case, we use a truthy check on data[k] to see if the value is not undefined, if so add to HttpParams
		Object.keys(data).forEach(k => {data[k] ? params = params.set(k, data[k]) : {}; });

		return params;
	}

	get(id: string): Observable<any> {
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${id}`);
	}

	update(data: any): Observable<any> {
		return this.http.put(`${environment.REST_BACKEND_SERVER}/rest/spectra/${data.id}`, data);
	}

	searchSpectra(data: any): Observable<any> {
		const params = this.cleanParameters(data);
		if (data.endpoint === undefined) {
			return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra`, { params});
		}
		else{
			return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${params.get('endpoint')}`, { params});
		}
	}

	searchSpectraCount(data: any): Observable<any> {
		const params = this.cleanParameters(data);
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/search/${params.get('endpoint')}`, { params })
			// TransformResponse no longer available, so pipe the map function to our observable and change the data before shipping to a promise
			.pipe(map((res) => {
				return {count: res};
			}));
	}

	searchSimilarSpectra(data: any): Observable<any> {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/similarity/search`, data, config)
			.pipe(first(), map((res: any) => {
				 let result;
         console.log(res);
				 result = res.map((spectrum) => {
					spectrum.hit.similarity = spectrum.score;
          console.log(spectrum.hit);
					return spectrum.hit;
				  });
         console.log(result);
				 return result;
			}));
	}

	batchSave(token: string): Observable<any> {
		const config = {
			headers: {
				'Content-Type' : 'application/json',
				Authorization: 'bearer ' + token
			}
		};
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra`, token, config);
	}

	score(data: any): Observable<any> {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/score/${data.id}/explain`, config);
	}

	curate(id: string): Observable<any> {
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/curate${id}`);
	}

	curateSpectraByQuery(data: any): Observable<any> {
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/curate/spectra/curateAllByQuery`, data);
	}

	associateSpectraByQuery(data: any): Observable<any> {
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra/associate/allByQuery`, data);
	}

  batchDelete(data: any, token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      params: this.cleanParameters(data),
      responseType: 'text' as 'json'
    };
    return this.http.delete(`${environment.REST_BACKEND_SERVER}/rest/spectra/search`, config);
  }

  batchDeleteByIds(data: any, token: any): Observable<any> {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + token
      },
      body: data,
      responseType: 'text' as 'json'
    };
    return this.http.delete(`${environment.REST_BACKEND_SERVER}/rest/spectra`, config);
  }
}
