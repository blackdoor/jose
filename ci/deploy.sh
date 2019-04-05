#!/usr/bin/env bash

echo $SIGNING_KEY | base64 --decode > ci/signing.key
gpg --fast-import ci/signing.key

PUBLISH_VERSION=$TRAVIS_TAG mill mill.scalalib.PublishModule/publishAll --sonatypeCreds $SONATYPE_NAME:$SONATYPE_PW --release true --gpgPassphrase $GPG_PW --publishArtifacts __.publishArtifacts