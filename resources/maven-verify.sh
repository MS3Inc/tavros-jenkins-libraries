#!/usr/bin/env bash

FILE=.settings.xml
if [ -f "$FILE" ]; then
    printf "\nUsing settings.xml\n"; mvn -s .settings.xml clean verify;
else
    printf "\nNo settings.xml provided\n"; mvn clean verify;
fi
