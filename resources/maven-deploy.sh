#!/usr/bin/env bash

FILE=.settings.xml
if [ -f "$FILE" ]; then
    printf "\nUsing settings.xml"; mvn -s .settings.xml clean package k8s:build k8s:push;
else
    printf "\nNot using provided settings.xml"; mvn clean clean package k8s:build k8s:push;
fi
