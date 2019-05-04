package black.door.jose.jws.circe

import black.door.jose.jwk.{OctJwk, RsaPublicKey}
import com.nimbusds.jose.crypto.{MACSigner, MACVerifier, RSASSASigner}
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader, JWSObject, Payload}
import org.scalatest.{EitherValues, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import black.door.jose.json.circe.JsonSupport._
import black.door.jose.json.common._
import black.door.jose.jws.{Jws, JwsHeader}
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator

class JwsCirceSpec extends WordSpec with Matchers with EitherValues {
  "HS signatures" should {

    val hsKey = OctJwk.generate(256)

    "sign correctly" in {
      val jws     = Jws(JwsHeader("HS256"), "test data".getBytes)
      val compact = jws.sign(hsKey)

      val verifier = new MACVerifier(hsKey.k)
      JWSObject.parse(compact).verify(verifier) shouldBe true
    }

    "validate correctly" in {
      val signer  = new MACSigner(hsKey.k)
      val payload = "test data"
      val jwsObj  = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(payload))
      jwsObj.sign(signer)
      val compact = jwsObj.serialize

      val jws = Await.result(Jws.validate[String](compact, hsKey), Duration.Inf)
      jws.right.value.payload shouldBe payload
    }

    "reject invalid signatures" in {
      val jws     = Jws(JwsHeader("HS256"), "test data".getBytes)
      val compact = jws.sign(hsKey)

      val parsedJws =
        Await.result(Jws.validate[String](compact, OctJwk.generate(256)), Duration.Inf)
      parsedJws shouldBe 'left
    }
  }

  "RS signatures" should {
    val rsKey = new RSAKeyGenerator(2048).generate

    "validate correctly" in {
      val signer = new RSASSASigner(rsKey)

      val payload   = "test data"
      val jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256), new Payload(payload))

      jwsObject.sign(signer)
      val compact = jwsObject.serialize

      val blackdoorKey = RsaPublicKey(
        rsKey.getModulus.decodeToBigInteger,
        rsKey.getPublicExponent.decodeToBigInteger
      )

      val jws = Await.result(Jws.validate[String](compact, blackdoorKey), Duration.Inf)
      jws.right.value.payload shouldBe payload
    }

    "reject invalid signatures" in {
      val signer = new RSASSASigner(rsKey)

      val payload   = "test data"
      val jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256), new Payload(payload))

      jwsObject.sign(signer)
      val compact = jwsObject.serialize

      val otherKey = new RSAKeyGenerator(2048).generate
      val blackdoorKey = RsaPublicKey(
        otherKey.getModulus.decodeToBigInteger,
        otherKey.getPublicExponent.decodeToBigInteger
      )

      val jws = Await.result(Jws.validate[String](compact, blackdoorKey), Duration.Inf)
      jws shouldBe 'left
    }
  }
}
