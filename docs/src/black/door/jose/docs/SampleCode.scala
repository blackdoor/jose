package black.door.jose.docs

import java.time.Instant
import java.time.temporal.ChronoUnit
import black.door.jose.json.ninny.JsonSupport._
import io.github.kag0.ninny.Json

object SampleCode extends App {

  implicit def autoSome[A](a: A) = Some(a)

// format: off
{

import black.door.jose.jwk._
import black.door.jose.jwt._

val key = P256KeyPair.generate

val claims = Claims(
  sub = "my user",
  iss = "me",
  exp = Instant.now.plus(1, ChronoUnit.DAYS)
)

val token = Jwt.sign(
  claims,
  key.withAlg("ES256")
)

val errorOrJwt = Jwt
  .validate(token)
  .using(key, Check.iss(_ == "me"))
  .now

errorOrJwt.right.get.claims.sub // Some(my user)
  
} // format: on

  {
    import black.door.jose.jwk._
    import io.github.kag0.ninny.Auto._
    case class MyCustomClaimsClass(isAdmin: Boolean)
    val es256Key = Json
      .parse(
        """
        |{
        |    "kty": "EC",
        |    "d": "L5qVktaHtMwgo9RrDGw7pAsUlUpGsaLnIXwvpJNba_I",
        |    "use": "sig",
        |    "crv": "P-256",
        |    "x": "ZalefywgnJq0iuzt8HFIX4hK6LHqkOX6_IV7idd-8c0",
        |    "y": "8cAfsN9JW7X9ukqystF198wdv6JovCG99qfB-LUjblo",
        |    "alg": "ES256"
        |}""".stripMargin
      )
      .to[Jwk]
      .get

    val compact =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJ0aGlzVG9rZW5Jc0ZvckFuQWRtaW4iOmZhbHNlfQ.OEpXwMiFjOIk-vSjBekB67m2cwwIAd6vyVkIP5FgRS8L4MBc6q7qdpSZwkTkkg7oSv5G6YI5UIJUIgnjq_5Jdg"

    // format: off
    
import black.door.jose.jwt._

val customValidator =
  JwtValidator.fromSync[MyCustomClaimsClass] {
    case jwt if !jwt.claims.unregistered.isAdmin =>
      "Token needs to be for an admin"
  }

Jwt
  .validate(compact)[MyCustomClaimsClass]
  .using(es256Key, customValidator)
  .now
// Left(Token needs to be for an admin)

    // format: on
  }
}
