# Separation of concerns

## Validation

### Ensuring JOSE header is well formed JSON

ByteDeserializer[JwsHeader] implementation

### Ensure all required fields are understood (`crit` header value)

Library user

### Providing a JWK which is valid for signature verification (`key_ops` and `use` params) 
    and for the current JWS (`kid` and `alg` match between the JWK and JWS header)
    
The `KeyResolver` implementation must be responsible for providing a key which is correct and usable 
for the current JWS, but the library will double check that it has done so (although it will throw exceptions if not).

### Applying the correct algorithm for verification with the given JWK.

The `SignatureValidator` implementation in the `SignatureAlgorithm` must ensure that it can handle the given JWS header.
It must also ensure the given key is valid for the algorithm either by being undefined for the key and header or by 
throwing an exception (only if it determines that no other algorithm could be valid for that header).

## Failure cases

The library differentiates between (external) errors and exceptions. Errors are failures caused by external inputs where
there is nothing the library or library user could have done to enable success. Exceptions are failures caused by 
improper code or configuration where there is nothing an external input could have done to enable success.

Errors should result in a `Left` value on verification so that the library user can decide what action to take based on 
the error.  
Exceptions should throw a runtime exception. Library users should not try to catch these exceptions in logic code since 
the presence of the exception means that the logic is incorrect (rather clients should go fix the logic when they notice 
an exception).