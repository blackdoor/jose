#!/usr/bin/env bash

pushd `pwd`

if [ -z $TRAVIS_PULL_REQUEST_BRANCH ]
then

    echo "Publishing scaladocs"

    if [ $TRAVIS_TAG ]
    then
        echo "Publishing docs under $TRAVIS_TAG"
        export RELEASE_TAG=$TRAVIS_TAG
    fi

    sbt clean makeSite
    cd target/site

    git init
    git add .
    git commit -m "Update scaladoc"
    git push --force "https://kag0:$GITHUB_TOKEN@github.com/blackdoor/jose.git" master:gh-pages

fi

popd