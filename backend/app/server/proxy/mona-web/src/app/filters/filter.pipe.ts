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
            Object.keys(args).forEach((key) => {
                value = value.filter(item => {
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
            return value;

        }
    }
}
