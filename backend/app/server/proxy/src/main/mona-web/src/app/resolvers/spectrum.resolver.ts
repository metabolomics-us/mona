import {Injectable} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {ActivatedRouteSnapshot, Resolve} from '@angular/router';
import {Observable, of} from 'rxjs';
import {SpectrumCacheService} from '../services/cache/spectrum-cache.service';
import {Spectrum} from '../services/persistence/spectrum.resource';
import {retry} from 'rxjs/operators';
import {SpectrumResult} from "../mocks/spectrum-result.model";

@Injectable({
  providedIn: 'root'
})
export class SpectrumResolver implements Resolve<Observable<SpectrumResult[]>> {
  constructor(private spectrumCache: SpectrumCacheService, private spectrumService: Spectrum, private logger: NGXLogger) {}

  resolve(route: ActivatedRouteSnapshot): Observable<SpectrumResult[]> {
    const id = route.params.id;
    if (!this.spectrumCache.hasSpectrum() || this.spectrumCache.getSpectrum().id !== id) {
      this.logger.debug('Attempting to fetch spectrum');
      const foundSpectrum= this.spectrumService.get(id)
        .pipe(retry(2));
      this.logger.debug('Found spectrum: ' + foundSpectrum);
      return foundSpectrum;
    }
    else {
      const cachedSpectrum = of(this.spectrumCache.getSpectrum());
      this.logger.debug('Retrieved cache spectrum: ' + cachedSpectrum);
      this.spectrumCache.removeSpectrum();
      return cachedSpectrum;
    }
  }
}
