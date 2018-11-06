# jose

Extensible JOSE library for Scala.

> Not yet implemented:  
> * JWE
> * RSA
> * Less common key sizes for ECDSA
> * Custom parameters

## Usage

Pretty simple: make a key, make something to sign, sign it.

```scala
val claims = Claims(sub = Some("my user"), iss = Some("me"), exp = Some(Instant.now.plus(1, ChronoUnit.DAYS)))
val key = JavaP256KeyPair.generate

val compactToken = Jwt.sign(key, claims)

val errorOrJwt = Jwt.validateSync(compactToken, key, Check.iss("me").orElse(JwtValidator.defaultValidator))
errorOrJwt.right.get.claims.sub // Some(my user)
```

### Selecting a JSON implementation

Work in progress, currently just `import pkg.Json._`  
Later on the `Mapper` implicits for each json library will be in separate projects so that you can select one without 
pulling in dependencies for the others.

### Async key resolution and validation checks

Frequently you will need to dynamically look up a new key from a keyserver based on a JWS header, 
or check a centralized cache to see if a token has been revoked.   
This is easy to do asynchronously by implementing `KeyResolver` or `JwtValidator.`  
`JwtValidator` is a partial function so you can easily chain both sync and async validations.  
`KeyResolver` allows you to return an error in the event that there was a specific reason a key could not be found 
(perhaps a key does exist, but it's only for encryption and this token is using it for signing).