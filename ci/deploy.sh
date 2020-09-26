#!/usr/bin/env bash

echo $SIGNING_KEY | base64 --decode > signing.key
gpg --import signing.key
rm signing.key

PUBLISH_VERSION=$TRAVIS_TAG ./mill -i --disable-ticker mill.scalalib.PublishModule/publishAll \
	--sonatypeCreds $SONATYPE_NAME:$SONATYPE_PW \
	--gpgPassphrase $GPG_PW \
	--publishArtifacts __.publishArtifacts \
	--release true