/**
 * Created by wohlgemuth on 6/11/14.
 */

import {HttpClient, HttpParams} from "@angular/common/http";
import { map } from 'rxjs/operators';
import { environment } from "../../../environments/environment";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";

@Injectable()
export class Spectrum {
	constructor(public http: HttpClient) {
	}

	public cleanParameters = (data: Object) : HttpParams => {
		//Filter our undefined values and place the others in HttpParams value
		let params = new HttpParams();
		//Ternary case, we use a truthy check on data[k] to see if the value is not undefined, if so add to HttpParams
		Object.keys(data).forEach(k => {data[k] ? params = params.set(k, data[k]): {}});

		return params;
	}

	get = (id: String): Observable<any>=> {
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${id}`);
	}

	update = (data: Object): Observable<any> => {
		return this.http.put(`${environment.REST_BACKEND_SERVER}/rest/spectra/${data["id"]}`, data);
	}

	searchSpectra = (data: Object): Observable<any> => {
		let params = this.cleanParameters(data);
		console.log(data);
		if(data["endpoint"] === undefined) {
			console.log("Should enter here");
			return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra`, { params: params});
		}
		else{
			return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${params.get("endpoint")}`, { params: params});
		}
	}

	searchSpectraCount = (data: Object): Observable<any> => {
		let params = this.cleanParameters(data);
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/search/${params.get("endpoint")}`, { params: params })
			//TransformResponse no longer available, so pipe the map function to our observable and change the data before shipping to a promise
			.pipe(map((res) => {
				return {count: res}
			}));
	}

	searchSimilarSpectra = (data: Object): Observable<any> => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/similarity/search`, data, config)
			.pipe(map((res: any) => {
				let data: any;
				data = JSON.parse(res).map((spectrum) => {
					spectrum.hit.similarity = spectrum.score;
					return spectrum.hit;
				});
				return data;
			}));
	}

	batchSave = (token: String): Observable<any> => {
		const config = {
			headers: {
				'Content-Type' : 'application/json',
				'Authorization': 'bearer ' + token
			}
		};
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra`, token, config);
	}

	score = (data: Object): Observable<any> => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/score/${data["id"]}/explain`, config);
	}

	curate = (id: String): Observable<any> => {
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/curate${id}`);
	}

	curateSpectraByQuery = (data: Object): Observable<any> => {
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/curate/spectra/curateAllByQuery`, data);
	}

	associateSpectraByQuery = (data: Object): Observable<any> => {
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra/associate/allByQuery`, data);
	}
}
