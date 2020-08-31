#!/bin/bash

BUILD_GRADLE="../build.gradle"
VERSION=$(grep "version = '.*'" $BUILD_GRADLE | cut -d "'" -f2)

cd ..
./gradlew shadowjar
cd docker
rm -f listenability-tools-*-all.jar
cp ../build/libs/listenability-tools-$VERSION-all.jar .
cp ../src/main/resources/conf/service.conf .

if groups | grep -q '\bdocker\b';then
  docker build -t webis/listenability-tools:$VERSION .
else
  sudo docker build -t webis/listenability-tools:$VERSION .
fi

