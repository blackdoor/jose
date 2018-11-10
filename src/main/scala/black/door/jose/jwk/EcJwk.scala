package black.door.jose.jwk

import java.math.BigInteger
import java.security.interfaces.{ECPrivateKey => jECPrivateKey, ECPublicKey => jECPublicKey}
import java.security.spec._
import java.security.{KeyFactory, KeyPairGenerator, SecureRandom, Signature}


trait EcPublicKey extends Jwk {
  val kty = "EC"
  def crv: String
  def x: BigInt
  def y: BigInt
}

trait EcPrivateKey extends Jwk {
  val kty = "EC"
  def d: BigInt
}

trait EcKeyPair extends EcPublicKey with EcPrivateKey {
  override val kty = "EC"
}

trait JavaEcPublicKey extends EcPublicKey {
  protected def spec: ECParameterSpec
  def javaSignature: Signature
  lazy val toJavaPublicKey: jECPublicKey = JavaP256KeyPair.ecToJavaPublicKey(this, spec)
}

trait JavaEcPrivateKey extends EcPrivateKey {
  protected def spec: ECParameterSpec
  def javaSignature: Signature
  lazy val toJavaPrivateKey: jECPrivateKey = {
    val privateKeySpec = new ECPrivateKeySpec(d.bigInteger, spec)
    val keyFactory = KeyFactory.getInstance("EC")
    keyFactory.generatePrivate(privateKeySpec).asInstanceOf[jECPrivateKey]
  }
}

case class JavaP256KeyPair(
                            d: BigInt,
                            x: BigInt,
                            y: BigInt,
                            use: Option[String] = None,
                            key_ops: Option[Seq[String]] = None,
                            kid: Option[String] = None
                          ) extends EcKeyPair with JavaEcPrivateKey with JavaEcPublicKey {
  val crv = "P-256"
  val alg = Some("ES256")

  def isValidFor(alg: String) = alg == "ES256"

  def spec = JavaP256KeyPair.P256ParameterSpec
  def javaSignature = Signature.getInstance("SHA256withECDSA")
}

object JavaP256KeyPair {
  def generate = {
    val keyGen = KeyPairGenerator.getInstance("EC")
    keyGen.initialize(256, new SecureRandom())
    val pair = keyGen.generateKeyPair()
    JavaP256KeyPair(
      pair.getPrivate.asInstanceOf[java.security.interfaces.ECPrivateKey].getS,
      pair.getPublic.asInstanceOf[java.security.interfaces.ECPublicKey].getW.getAffineX,
      pair.getPublic.asInstanceOf[java.security.interfaces.ECPublicKey].getW.getAffineY
    )
  }

  val P256ParameterSpec = new ECParameterSpec(
    new EllipticCurve(
      new ECFieldFp(new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951")),
      new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948"),
      new BigInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291")
    ),
    new ECPoint(
      new BigInteger("48439561293906451759052585252797914202762949526041747995844080717082404635286"),
      new BigInteger("36134250956749795798585127919587881956611106672985015071877198253568414405109")
    ),
    new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369"),
    1
  )

  def ecToJavaPublicKey(jwk: EcPublicKey, spec: ECParameterSpec) = {
    val publicKeySpec = new ECPublicKeySpec(new ECPoint(jwk.x.bigInteger, jwk.y.bigInteger), spec)
    val keyFactory = KeyFactory.getInstance("EC")
    keyFactory.generatePublic(publicKeySpec).asInstanceOf[jECPublicKey]
  }
}

