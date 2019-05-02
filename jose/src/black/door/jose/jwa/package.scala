package black.door.jose

import black.door.jose.jws.{InputSigner, SignatureValidator}

import collection.immutable.Seq

package object jwa {

  sealed trait Jwa {
    def value: String
  }

  // used with JWS
  trait SignatureAlgorithm extends Jwa {
    def alg: String
    def value = alg

    def validate: SignatureValidator
    def sign: InputSigner
  }

  object SignatureAlgorithms {
    val all: Seq[SignatureAlgorithm] = HSAlgs.all ++ RSAlgs.all :+ ES256
  }

  // used with JWE alg
  trait KeyManagementAlgorithm extends Jwa

  // used with JWE enc
  trait EncryptionAlgorithm extends Jwa
}
