#!/usr/bin/env bash

FILE=.settings.xml
if [ -f "$FILE" ]; then
    printf "\nUsing settings.xml\n"; mvn -s .settings.xml clean deploy;
else
    printf "\nNot using provided settings.xml\n"; mvn clean deploy;
fi
