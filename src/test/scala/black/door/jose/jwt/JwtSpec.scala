package black.door.jose.jwt

import java.time.Instant
import java.util.Base64

import black.door.jose.json.playjson.JsonSupport._
import black.door.jose.jwk.P256KeyPair
import com.nimbusds.jose.crypto.{ECDSASigner, ECDSAVerifier}
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class JwtSpec extends FlatSpec with Matchers {

  val es256Key = P256KeyPair.generate.copy(alg = Some("ES256"))

  def generateToken = {
    val claims = Claims(jti = Some("test token id"))
    Jwt.sign(claims, es256Key)
  }

  "JWT signing" should "trim base64url padding" in {
    val compact = generateToken
    val Array(headerC, payloadC, signatureC, _*) = compact.split('.')
    headerC should not contain '='
    payloadC should not contain '='
    signatureC should not contain '='
  }

  it should "sign with ES256" in {
    val claims = Claims(jti = Some("test token id"))
    val compact = Jwt.sign(claims, es256Key)

    val encoder = Base64.getUrlEncoder
    val nimbusJwk = ECKey.parse(s"""{"kty":"EC","crv":"P-256","x":"${encoder.encodeToString(es256Key.x.toByteArray)}","y":"${encoder.encodeToString(es256Key.y.toByteArray)}"}""")
    val nimbusVerifier = new ECDSAVerifier(nimbusJwk)
    SignedJWT.parse(compact).verify(nimbusVerifier) shouldBe true
  }

  "JWT verification" should "parse and verify with ES256" in {
    val encoder = Base64.getUrlEncoder
    val nimbusJwk = ECKey.parse(s"""{"kty":"EC","crv":"P-256","d":"${encoder.encodeToString(es256Key.d.toByteArray)}","x":"${encoder.encodeToString(es256Key.x.toByteArray)}","y":"${encoder.encodeToString(es256Key.y.toByteArray)}"}""")
    val claimsSet = new JWTClaimsSet.Builder()
      .jwtID("test token id")
      .build
    val signedJWT = new SignedJWT(
      new JWSHeader.Builder(JWSAlgorithm.ES256).build,
      claimsSet)
    val signer = new ECDSASigner(nimbusJwk)
    signedJWT.sign(signer)
    val compact = signedJWT.serialize

    Jwt.validateSync(compact, es256Key.toPublic) shouldBe 'right
  }

  it should "fail for tokens before the nbf value" in {
    val claims = Claims(nbf = Some(Instant.now.plusSeconds(60)))
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key) shouldBe 'left
  }

  it should "fail for tokens after the exp value" in {
    val claims = Claims(exp = Some(Instant.now.minusSeconds(60)))
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key) shouldBe 'left
  }

  it should "fail for the wrong signature" in {
    val key2 = P256KeyPair.generate
    val compact = generateToken
    Jwt.validateSync(compact, key2) shouldBe 'left
  }

  import Check._
  val validations = JwtValidator.combine(Seq(
    aud(_ == "aud"),
    iss(_ == "iss"),
    sub(_ == "sub")
  ))

  it should "fail for the wrong iss value" in {
    val claims = Claims(
      iss = Some("miss"),
      aud = Some("aud"),
      sub = Some("sub")
    )
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key, jwtValidator = validations) shouldBe 'left
  }

  it should "fail for the wrong aud value" in {
    val claims = Claims(
      iss = Some("iss"),
      aud = Some("miss"),
      sub = Some("sub")
    )
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key, jwtValidator = validations) shouldBe 'left
  }

  it should "fail for the wrong sub value" in {
    val claims = Claims(
      iss = Some("iss"),
      aud = Some("aud"),
      sub = Some("miss")
    )
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key, jwtValidator = validations) shouldBe 'left
  }

  it should "fail for missing iss value" in {
    val claims = Claims(
      aud = Some("aud"),
      sub = Some("sub")
    )
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key, jwtValidator = validations) shouldBe 'left
  }

  it should "fail for missing aud value" in {
    val claims = Claims(
      iss = Some("iss"),
      sub = Some("sub")
    )
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key, jwtValidator = validations) shouldBe 'left
  }

  it should "fail for missing sub value" in {
    val claims = Claims(
      iss = Some("iss"),
      aud = Some("aud")
    )
    val compact = Jwt.sign(claims, es256Key)
    Jwt.validateSync(compact, es256Key, jwtValidator = validations) shouldBe 'left
  }
}
