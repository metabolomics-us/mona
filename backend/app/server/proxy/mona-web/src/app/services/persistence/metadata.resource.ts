/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * simple service to help with available tags
 */
import {HttpClient} from "@angular/common/http";
import { environment } from "../../../environments/environment";
import {Injectable} from "@angular/core";

@Injectable()
export class Metadata {
    constructor(public http: HttpClient) {
    }

    metadata = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/data?max=100`).toPromise();
    }

    categories = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/category?max=100`).toPromise();
    }

    categoryData = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/category/data?max=100`).toPromise();
    }

    dataValues = (): Promise<Object> => {
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/data/value?max=100`).toPromise();
    }

    queryValues = (data: Object): Promise<Object> => {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            },
            params: {query: '@query'}
        };
        return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/meta/data/search?max=10`, data).toPromise();
    }

    metaDataNames = (): Promise<Object> => {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            },
            cache: true
        };
        return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/metaData/names`, config).toPromise();
    }
}
