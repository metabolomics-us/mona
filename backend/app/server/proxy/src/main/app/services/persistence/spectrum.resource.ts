/**
 * Created by wohlgemuth on 6/11/14.
 */

import {HttpClient, HttpParams} from "@angular/common/http";
import {Injectable, Inject} from "@angular/core";
import { map } from 'rxjs/operators';
import {downgradeInjectable} from "@angular/upgrade/static";
import { environment } from "../../environments/environment";
import * as  angular from 'angular';
import {Observable} from "rxjs";


export class Spectrum {
	constructor(@Inject(HttpClient) private http: HttpClient) {
	}

	private cleanParameters = (data: Object) : HttpParams => {
		//Filter our undefined values and place the others in HttpParams value
		let params = new HttpParams();
		//Ternary case, we use a truthy check on data[k] to see if the value is not undefined, if so add to HttpParams
		Object.keys(data).forEach(k => {data[k] ? params = params.set(k, data[k]): {}});

		return params;
	}

	get = (id: String): Promise<Object> => {
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${id}`).toPromise();
	}

	update = (data: Object): Promise<Object> => {
		return this.http.put(`${environment.REST_BACKEND_SERVER}/rest/spectra/${data["id"]}`, data).toPromise();
	}

	searchSpectra = (data: Object): Promise<Object> => {
		let params = this.cleanParameters(data);
		console.log(data);
		if(data["endpoint"] === undefined) {
			console.log("Should enter here");
			return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra`, { params: params}).toPromise();
		}
		else{
			return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/${params.get("endpoint")}`, { params: params}).toPromise();
		}
	}

	searchSpectraCount = (data: Object): Promise<Object> => {
		let params = this.cleanParameters(data);
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/search/${params.get("endpoint")}`, { params: params })
			//TransformResponse no longer available, so pipe the map function to our observable and change the data before shipping to a promise
			.pipe(map((res) => {
				return {count: res}
			}))
			.toPromise();
	}

	searchSimilarSpectra = (data: Object): Observable<any> => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/similarity/search`, data, config)
			.pipe(map((res) => {
				let data: any;
				data = angular.fromJson(res).map((spectrum) => {
					spectrum.hit.similarity = spectrum.score;
					return spectrum.hit;
				});
				return data;
			}));
	}

	batchSave = (token: String): Promise<Object> => {
		const config = {
			headers: {
				'Content-Type' : 'application/json',
				'Authorization': 'bearer ' + token
			}
		};
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra`, token, config).toPromise();
	}

	score = (data: Object): Promise<Object> => {
		const config = {
			headers: {
				'Content-Type': 'application/json'
			}
		};
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/score/${data["id"]}/explain`, config).toPromise();
	}

	curate = (id: String): Promise<Object> => {
		return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/spectra/curate${id}`).toPromise();
	}

	curateSpectraByQuery = (data: Object): Promise<Object> => {
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/curate/spectra/curateAllByQuery`, data).toPromise();
	}

	associateSpectraByQuery = (data: Object): Promise<Object> => {
		return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/spectra/associate/allByQuery`, data).toPromise();
	}
}

angular.module('moaClientApp')
	.factory('Spectrum', downgradeInjectable(Spectrum));

