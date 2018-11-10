# jose

![Code Coverage](https://codecov.io/gh/blackdoor-specialteams/jose/branch/master/graph/badge.svg)
[![Build Status](https://travis-ci.com/blackdoor-specialteams/jose.svg?branch=master)](https://travis-ci.com/blackdoor-specialteams/jose)

Extensible JOSE library for Scala.

> Not yet implemented:  
> * JWK serialization
> * JWE
> * RSA
> * Less common key sizes for ECDSA
> * Custom parameters

## Installation
Add the below to your `build.sbt` (replace the value after the pound with the desired version)
```scala
dependsOn(RootProject(uri("git://github.com/blackdoor/jose.git#0.1.0")))
```

## Usage

Pretty simple: make a key, make something to sign, sign it.

```scala
val claims = Claims(sub = Some("my user"), iss = Some("me"), exp = Some(Instant.now.plus(1, ChronoUnit.DAYS)))
val key = JavaP256KeyPair.generate

val compactToken = Jwt.sign(claims, key)

val errorOrJwt = Jwt.validateSync(compactToken, key, Check.iss("me").orElse(JwtValidator.defaultValidator))
errorOrJwt.right.get.claims.sub // Some(my user)
```

### Selecting a JSON implementation

Work in progress, currently just `import black.door.jose.Json._` for Play JSON support.  
Implement `Mapper` implicits to add support for new libraries.

### Async key resolution and validation checks

Frequently you will need to dynamically look up a new key from a keyserver based on a JWS header, 
or check a centralized cache to see if a token has been revoked.   
This is easy to do asynchronously by implementing `KeyResolver` or `JwtValidator.`  
`JwtValidator` is a partial function so you can easily chain both sync and async validations.  
`KeyResolver` allows you to return an error in the event that there was a specific reason a key could not be found 
(perhaps a key does exist, but it's only for encryption and this token is using it for signing).