import {Compound} from "./compound.model";
import {Metadata} from "./metadata.model";
import {Score} from "./score.model";
import {Tag} from "./tag.model";
import {Submitter} from "./submitter.model";
import {Splash} from "./splash.model";
import {Library} from "./library.model";

export class Spectrum {
  compound: Compound[];
  id: String;
  metaData: Metadata[];
  annotations: Metadata[];
  score: Score;
  spectrum: string;
  lastUpdated: string;
  dateCreated: string;
  lastCurated: string;
  splash: Splash;
  submitter: Submitter;
  tags: Tag[];
  library: Library;
  similarity: number;
}
