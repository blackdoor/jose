#!/usr/bin/env bash

#sudo mv /usr/bin/gpg /usr/bin/gpg1
#sudo update-alternatives --install /usr/bin/gpg gnupg /usr/bin/gpg2 100

echo $SIGNING_KEY | base64 --decode > signing.key
gpg --import signing.key
#rm signing.key

which gpg gpg1 gpg2

#gpg --list-secret-keys
#gpg1 --list-secret-keys
gpg --list-secret-keys
ls -la $HOME/.gnupg
#cat $HOME/.gnupg/gpg.conf

# echo "default-key $GPG_NAME" >> $HOME/.gnupg/gpg.conf

PUBLISH_VERSION=$TRAVIS_TAG mill -i --disable-ticker mill.scalalib.PublishModule/publishAll \
	$SONATYPE_NAME:$SONATYPE_PW \
	$GPG_PW \
	__.publishArtifacts \
	--release \
	true