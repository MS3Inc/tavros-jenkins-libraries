#!/usr/bin/env sh

echo $REG_HOST
echo $REG_CREDS_USR
mvn deploy -Dhost=$REG_HOST -Duser=$REG_CREDS_USR -Dpass=$REG_CREDS_PSW