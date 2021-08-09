import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, Resolve, Router} from "@angular/router";
import {Observable, of} from "rxjs";
import {SpectrumCacheService} from "../services/cache/spectrum-cache.service";
import {Spectrum} from "../services/persistence/spectrum.resource";

@Injectable({
  providedIn: 'root'
})
export class SpectrumResolver implements Resolve<Observable<any>> {
  constructor(private spectrumCache: SpectrumCacheService, private spectrumService: Spectrum,
              private router: Router) {
  }
  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    let id = route.params.id;
    console.log(id);
    console.log(route);
    if (!this.spectrumCache.hasSpectrum() || this.spectrumCache.getSpectrum().id !== id) {
      console.log('Attempting to fetch spectrum');
      return this.spectrumService.get(id);
    }
    else {
      this.spectrumCache.removeSpectrum();
      return of(this.spectrumCache.getSpectrum());
    }
  }
}
