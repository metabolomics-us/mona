import { Pipe, PipeTransform} from "@angular/core";

@Pipe({
    name: 'filterPipe',
    pure: false
})
export class FilterPipe implements PipeTransform {
    transform(items: any, filter: any, orderBy: boolean, unique: string): any {
        if(!items || !filter) {
            return items;
        }
        if(orderBy) {
            return items.filter(item => item.title.indexOf(filter.title) !== -1)
                .sort((a,b) => {
                    if(a.name < b.name) { return -1; }
                    if(a.name > b.name) { return 1; }
                    return 0;
                });
        } else{
            return items.filter(item => item.title.indexOf(filter.title) !== -1);
        }

    }
}
