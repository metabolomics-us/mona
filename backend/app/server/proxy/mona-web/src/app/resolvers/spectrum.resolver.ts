import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Resolve, Router} from '@angular/router';
import {Observable, of} from 'rxjs';
import {SpectrumCacheService} from '../services/cache/spectrum-cache.service';
import {Spectrum} from '../services/persistence/spectrum.resource';
import {retry} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class SpectrumResolver implements Resolve<Observable<any>> {
  constructor(private spectrumCache: SpectrumCacheService, private spectrumService: Spectrum) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    const id = route.params.id;
    if (!this.spectrumCache.hasSpectrum() || this.spectrumCache.getSpectrum().id !== id) {
      console.log('Attempting to fetch spectrum');
      return this.spectrumService.get(id)
        .pipe(retry(2));
    }
    else {
      this.spectrumCache.removeSpectrum();
      return of(this.spectrumCache.getSpectrum());
    }
  }
}
