import {Pipe, PipeTransform} from '@angular/core';
@Pipe({
  name: 'orderBy',
  pure: true
})
export class OrderbyPipe implements PipeTransform {
  transform(value: any, sortBy: any): any {
    if (!value || !sortBy) {
      return value;
    }
    if (typeof sortBy === 'string') {
      // Ascending Order Sort
      if (sortBy.charAt(0) === '+') {
        sortBy = sortBy.substring(1);
        return value.sort((a, b) => {
          if (a[sortBy] < b[sortBy]) { return 1; }
          else if (a[sortBy] > b[sortBy] ) { return -1; }
          else { return 0; }
        });
      }
      else if (sortBy.charAt(0) === '-') {
        // Descending Order Sort
        sortBy = sortBy.substring(1);
        return value.sort((a, b) => {
          if (a[sortBy] > b[sortBy]) { return 1; }
          else if (a[sortBy] < b[sortBy]) { return -1; }
          else { return 0; }
        });
      }
    } else { return value; }
  }
}

