#!/usr/bin/env bash

FILE=.settings.xml
if [ -f "$FILE" ]; then
    printf "\nUsing settings.xml"; mvn -s .settings.xml clean deploy;
else
    printf "\nNot using provided settings.xml"; mvn clean deploy;
fi
