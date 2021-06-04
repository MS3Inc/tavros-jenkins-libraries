#!/usr/bin/env bash
echo This is a sample kong plugin pipeline

touch test.txt
HTTP_CODE=$(curl -v -w '%{http_code}' --user $KC_BASIC_AUTH --upload-file ./test.txt https://artifacts.tavros.ms3-inc.com/nosso/repository/lua-rocks/kong-plugins/test.txt)

if [ $HTTP_CODE = 201 ] 
    then
        echo Succesfully pushed PLUGIN_NAME VERSION to Nexus
        exit 0
    else 
        echo Failed to push PLUGIN_NAME VERSION to Nexus
        exit 1
fi