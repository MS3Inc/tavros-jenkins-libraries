#!/usr/bin/env bash

FILE=.settings.xml
if [ -f "$FILE" ]; then
    printf "\nUsing settings.xml\n"; mvn -s .settings.xml clean verify;
else
    printf "\nNot using provided settings.xml\n"; mvn clean verify;
fi
