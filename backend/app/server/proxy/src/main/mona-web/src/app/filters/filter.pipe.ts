import { Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'filterPipe',
    pure: true
})
export class FilterPipe implements PipeTransform {
    transform(value: any, args: any): any {
        if (!value || !args) {return value; }
        if (typeof args === 'string'){
            if (args.charAt(0) === '!') {
                return value.filter(item => item.toString().indexOf(args.toString()) === -1);
            }
            return value.filter(item => item.toString().indexOf(args.toString()) !== -1);
        } else {
            // Remove duplicates and only allow computed to display, i.e. 2 InChI codes, 1 computed, 1 not
            // so the computed InChI will display instead of the none-computed
             const lookup = value.reduce((a, e) => {
               a[e.name] = ++a[e.name] || 0;
               return a;
             }, {});
             let finalResult = value.filter((x) => {
               return !lookup[x.name] || (lookup[x.name] && x.computed);
             });
             Object.keys(args).forEach((key) => {
                finalResult = finalResult.filter(item => {
                    if (args[key].charAt(0) === '!'){
                        const arg = args[key].slice(1);
                        if (typeof item[key] !== 'undefined') {
                            return item[key].toString().indexOf(arg.toString()) === -1;
                        }
                        return true;
                    } else {
                        if (typeof item[key] !== 'undefined') {
                            return item[key].toString().indexOf(args[key].toString()) !== -1;
                        }
                        return true;
                    }
                });
              });
             return finalResult;
        }
    }
}
