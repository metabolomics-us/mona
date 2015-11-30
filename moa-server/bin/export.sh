#!/bin/bash

# Get list of spectrum ids and download spectra
psql -U compound -h venus -d monaproduction -c 'select id from spectrum' | sed -n '1,2d;N;$!P;D' | xargs -P 8 -I % curl -o %.json http://mona.fiehnlab.ucdavis.edu/rest/spectra/%

# Redownload failed downloads
find . -type f -size '-2k' | xargs -I % curl -o % "http://mona.fiehnlab.ucdavis.edu/rest/spectra/$(basename % .json)"

# Re-upload spectra from json
find . -type f -name "*.json" | xargs -P 4 -I % curl -XPOST -H 'Content-Type:application/json' -H 'Accept:application/json' --data-binary @% http://127.0.0.1:8080/rest/spectra/batch/save



# MoNA credentials
export MONA_USERNAME=""
export MONA_PASSWORD=""

# Log in and capture authentication token
export ACCESS_TOKEN=$(curl -s -X POST -H "Content-Type: application/json" -d '{"email": "'"$MONA_USERNAME"'", "password": "'"$MONA_PASSWORD"'"}' http://mona.fiehnlab.ucdavis.edu/rest/login | grep -Eow "[a-z0-9]*([a-z]+[0-9]+|[0-9]+[a-z]+)")

# Example of a call that requires an authentication token, e.g logging out
curl -v -X POST -H "Content-Type: application/json" -H "X-Auth-Token: $ACCESS_TOKEN" http://mona.fiehnlab.ucdavis.edu/rest/logout

