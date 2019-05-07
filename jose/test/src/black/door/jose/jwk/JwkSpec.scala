package black.door.jose.jwk

import black.door.jose.Mapper
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.{ECKey, OctetSequenceKey, RSAKey}
import org.scalatest.{EitherValues, FlatSpec, Matchers}

trait JwkSpec extends FlatSpec with Matchers with EitherValues {
  def jwkSerializer: Mapper[Jwk, String]
  implicit def jwkDeserializer: Mapper[String, Jwk]

  "Symmetric JWKs" should "serialize and deserialize" in {
    val initialKey = OctJwk
      .generate(256)
      .copy(use = Some("enc"), alg = Some("HS256"), kid = Some("1"))
    val initialJson = jwkSerializer(initialKey).right.value

    val intermediateKey  = OctetSequenceKey.parse(initialJson)
    val intermediateJson = intermediateKey.toJSONObject.toJSONString

    val finalKey = Jwk.parse(intermediateJson).right.value

    initialKey shouldEqual finalKey
  }

  "ES JWKs" should "serialize and deserialize" in {
    val initialKey = P256KeyPair.generate
      .copy(use = Some("sig"), alg = Some("ES256"), kid = Some("1"))
    val initialJson = jwkSerializer(initialKey).right.value

    val intermediateKey  = ECKey.parse(initialJson)
    val intermediateJson = intermediateKey.toJSONObject.toJSONString

    val finalKey = Jwk.parse(intermediateJson).right.value

    initialKey shouldEqual finalKey
  }

  "RSA JWKs" should "serialize and deserialize" in {
    val public = new RSAKeyGenerator(2048).generate
    val initialKey = RsaPublicKey(
      n = public.getModulus.decodeToBigInteger,
      e = public.getPublicExponent.decodeToBigInteger,
      alg = Some("RS256"),
      kid = Some("1"),
      use = Some("sig")
    )

    val initialJson = jwkSerializer(initialKey).right.value

    val intermediateKey  = RSAKey.parse(initialJson)
    val intermediateJson = intermediateKey.toJSONObject.toJSONString

    val finalKey = Jwk.parse(intermediateJson).right.value

    initialKey shouldEqual finalKey
  }
}
