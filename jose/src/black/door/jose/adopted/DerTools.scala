package black.door.jose.adopted

import java.security.SignatureException

/** Adapted from
  * https://github.com/martintreurnicht/jjwt/blob/master/src/main/java/io/jsonwebtoken/impl/crypto/EllipticCurveProvider.java
  * under http://www.apache.org/licenses/LICENSE-2.0
  */
object DerTools {

  /** Transcodes the JCA ASN.1/DER-encoded signature into the concatenated R + S format
    * expected by ECDSA JWS.
    *
    * @param derSignature
    *   The ASN1./DER-encoded. Must not be { @code null}.
    * @param outputLength
    *   The expected length of the ECDSA JWS signature.
    * @return
    *   The ECDSA JWS encoded signature.
    * @throws SignatureException
    *   If the ASN.1/DER signature format is invalid.
    */
  @throws[SignatureException]
  def transcodeSignatureToConcat(derSignature: Array[Byte], outputLength: Int): Array[Byte] = {
    if (derSignature.length < 8 || derSignature(0) != 48)
      throw new SignatureException("Invalid ECDSA signature format")

    var offset = 0
    if (derSignature(1) > 0) offset = 2
    else if (derSignature(1) == 0x81.toByte) offset = 3
    else throw new SignatureException("Invalid ECDSA signature format")

    val rLength = derSignature(offset + 1)

    var i: Int = rLength
    while ((i > 0) && (derSignature((offset + 2 + rLength) - i) == 0))
      i -= 1

    val sLength = derSignature(offset + 2 + rLength + 1)
    var j: Int  = sLength
    while ((j > 0) && (derSignature((offset + 2 + rLength + 2 + sLength) - j) == 0))
      j -= 1

    var rawLen = Math.max(i, j)
    rawLen = Math.max(rawLen, outputLength / 2)

    if (
      (derSignature(offset - 1) & 0xff) != derSignature.length - offset
      || (derSignature(offset - 1) & 0xff) != 2 + rLength + 2 + sLength
      || derSignature(offset) != 2
      || derSignature(offset + 2 + rLength) != 2
    ) throw new SignatureException("Invalid ECDSA signature format")

    val concatSignature = new Array[Byte](2 * rawLen)

    System.arraycopy(derSignature, (offset + 2 + rLength) - i, concatSignature, rawLen - i, i)
    System.arraycopy(
      derSignature,
      (offset + 2 + rLength + 2 + sLength) - j,
      concatSignature,
      2 * rawLen - j,
      j
    )

    concatSignature
  }

  /** Transcodes the ECDSA JWS signature into ASN.1/DER format for use by the JCA verifier.
    *
    * @param jwsSignature
    *   The JWS signature, consisting of the concatenated R and S values. Must not be { @code
    *   null}.
    * @return
    *   The ASN.1/DER encoded signature.
    * @throws SignatureException
    *   If the ECDSA JWS signature format is invalid.
    */
  @throws[SignatureException]
  def transcodeSignatureToDER(jwsSignature: Array[Byte]): Array[Byte] = {
    val rawLen = jwsSignature.length / 2

    var i = rawLen

    while ((i > 0) && (jwsSignature(rawLen - i) == 0))
      i -= 1

    var j = i

    if (jwsSignature(rawLen - i) < 0) j += 1

    var k = rawLen

    while ((k > 0) && (jwsSignature(2 * rawLen - k) == 0))
      k -= 1

    var l = k

    if (jwsSignature(2 * rawLen - k) < 0) l += 1

    val len = 2 + j + 2 + l

    if (len > 255) throw new SignatureException("Invalid ECDSA signature format")

    var offset                    = 0
    var derSignature: Array[Byte] = null

    if (len < 128) {
      derSignature = new Array[Byte](2 + 2 + j + 2 + l)
      offset = 1
    } else {
      derSignature = new Array[Byte](3 + 2 + j + 2 + l)
      derSignature(1) = 0x81.toByte
      offset = 2
    }
    derSignature(0) = 48
    derSignature { offset += 1; offset - 1 } = len.toByte
    derSignature { offset += 1; offset - 1 } = 2
    derSignature { offset += 1; offset - 1 } = j.toByte

    System.arraycopy(jwsSignature, rawLen - i, derSignature, (offset + j) - i, i)

    offset += j

    derSignature { offset += 1; offset - 1 } = 2
    derSignature { offset += 1; offset - 1 } = l.toByte

    System.arraycopy(jwsSignature, 2 * rawLen - k, derSignature, (offset + l) - k, k)

    derSignature
  }
}
