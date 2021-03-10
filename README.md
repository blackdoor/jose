<script type='module' src="https://kag0.github.io/sauce/sauce.js">
<h1>If you're reading this, click <a href="https://blackdoor.github.io/jose">HERE</a></h1>
</script>

# jose
[![](https://img.shields.io/codacy/grade/177db012dc7548be9143a7562cd1d4bd.svg?style=flat-square)](https://app.codacy.com/project/blackdoor/jose/dashboard)
[![Travis (.com)](https://img.shields.io/travis/com/blackdoor/jose.svg?style=flat-square)](https://travis-ci.com/blackdoor/jose)
[![Scaladoc](https://img.shields.io/badge/scaladoc-latest-blue.svg?style=flat-square)](https://blackdoor.github.io/jose/api/latest/black/door/jose/index.html)
[![Maven Central](https://img.shields.io/maven-central/v/black.door/jose_2.12.svg?style=flat-square)](https://mvnrepository.com/artifact/black.door/jose)
[![Gitter](https://img.shields.io/gitter/room/blackdoor/jose?style=flat-square)](https://gitter.im/blackdoor/jose?utm_source=share-link&utm_medium=link&utm_campaign=share-link)
[![Matrix](https://img.shields.io/matrix/blackdoor_jose:gitter.im?label=chat%20on%20matrix&logoColor=0dbd8b&style=flat-square)](https://matrix.to/#/#blackdoor_jose:gitter.im?via=gitter.im&via=matrix.org)

Extensible JOSE library for Scala.

## Installation

The dependency is available on [Maven Central](https://mvnrepository.com/artifact/black.door/jose).

## Usage

Pretty simple: make a key, make something to sign, sign it.

<sauce-code 
    repo='blackdoor/jose' 
    file='docs/src/black/door/jose/docs/SampleCode.scala'
    lines='15:36'
></sauce-code>

### Selecting a JSON implementation

Currently supported JSON libraries:

* [x] [ninny JSON](https://mvnrepository.com/artifact/black.door/jose-json-ninny)
* [x] [Play JSON](https://mvnrepository.com/artifact/black.door/jose-json-play)
* [ ] [Json4s](http://json4s.org/)
* [x] [Circe](https://mvnrepository.com/artifact/black.door/jose-json-circe)

To add a JSON support, just import or mix in an implementation like `import black.door.jose.json.playjson.JsonSupport._`.

If your preferred library isn't supported, just implement `Mapper` implicits (or open an issue to request they be added).

### Async key resolution and validation checks

Frequently you will need to dynamically look up a new key from a keyserver based on a JWS header, 
or check a centralized cache to see if a token has been revoked.   
This is easy to do asynchronously by implementing `KeyResolver` or `JwtValidator.`  
`JwtValidator` is a partial function so you can easily chain both sync and async validations.  
`KeyResolver` allows you to return an error in the event that there was a specific reason a key could not be found 
(perhaps a key does exist, but it's only for encryption and this token is using it for signing).

### JWT validation DSL

There is a handy compile-safe DSL for JWT validation that allows you to indicate if you want to use unregistered claims, 
and if you want to evaluate synchronously or asynchronously. Its structure looks like this

```
Jwt
|__ .validate(compactJwt)
    |__ .using(keyResolver, etc...)
    |   |__ .now   // validates the JWT synchronously
    |   |__ .async // returns the validation result in a Future
    |__ .apply[UnregisteredClaims]
        |__ .using(keyResolver, etc...)
            |__ .now   // validates the JWT synchronously
            |__ .async // returns the validation result in a Future
```

So for example you could synchronously validate a JWT with some custom claims with

<sauce-code
repo='blackdoor/jose'
file='docs/src/black/door/jose/docs/SampleCode.scala'
lines='64:76'
></sauce-code>

> Not yet implemented:  
> * JWK serialization partly implemented
> * JWE
> * RSA signing (RSA signature verification is supported)
> * Less common key sizes for ECDSA
> * Custom JOSE header parameters (custom JWT claims are supported)
