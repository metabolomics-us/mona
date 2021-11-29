import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'bytesPipe',
  pure: false
})
export class BytesPipe implements PipeTransform {
  transform(bytes: any): any {
      if (bytes === 0) {
        return '0 bytes';
      }
      if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) {
        return '-';
      }
      const precision = 1;
      const units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];
      const num = Math.floor(Math.log(bytes) / Math.log(1024));

      return (bytes / Math.pow(1024, Math.floor(num))).toFixed(precision) +  ' ' + units[num];
  }
}
