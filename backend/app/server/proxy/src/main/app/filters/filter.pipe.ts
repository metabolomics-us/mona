import { Pipe, PipeTransform} from "@angular/core";

@Pipe({
    name: 'filterPipe',
    pure: false
})
export class FilterPipe implements PipeTransform {
    transform(value: any, args: any): any {
        if (!value || !args) return value;
        if (typeof args == "string"){
            if(args.charAt(0) === '!') {
                return value.filter(item => item.toString().indexOf(args.toString()) === -1)
            }
            return value.filter(item => item.toString().indexOf(args.toString()) !== -1);
        } else {
            Object.keys(args).forEach((key) => {
                value.filter(item => {
                    if(args[key].charAt(0) === '!'){
                        let arg = args[key].slice(0);
                        if(typeof item[key] !== 'undefined') {
                            return item[key].toString().indexOf(arg.toString()) === -1
                        }
                    } else {
                        if(typeof item[key] !== 'undefined') {
                            return item[key].toString().indexOf(args[key].toString()) !== -1
                        }
                    }

                });
            });
            return value;

        }
    }
}
