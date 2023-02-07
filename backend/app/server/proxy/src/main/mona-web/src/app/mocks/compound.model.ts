import {Tag} from "./tag.model";
import {Names} from "./names.model";
import {Metadata} from "./metadata.model";

export class Compound {
  kind: string;
  tags: Tag[];
  inchi: String;
  names: Names[];
  molFile: String;
  computed: boolean;
  inchiKey: string;
  metaData: Metadata[]
  classification: Metadata[]
}
