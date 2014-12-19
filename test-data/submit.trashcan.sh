#!/bin/bash

for i in `seq $2`; do curl -XPOST -H 'Content-Type:application/json' -H 'Accept:application/json' --data-binary @$1 http://cream.fiehnlab.ucdavis.edu:9292/trashcan.fiehnlab.ucdavis.edu:8080/rest/spectra/batch/save; done
