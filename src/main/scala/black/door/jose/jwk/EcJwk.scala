package black.door.jose.jwk

import java.math.BigInteger
import java.security.interfaces.{ECPrivateKey => jECPrivateKey, ECPublicKey => jECPublicKey}
import java.security.spec._
import java.security.{KeyFactory, KeyPairGenerator, SecureRandom}


trait EcPublicKey extends PublicJwk {
  val kty = "EC"
  def crv: String
  def x: BigInt
  def y: BigInt

  protected def spec: ECParameterSpec
  lazy val toJcaPublicKey = {
    val publicKeySpec = new ECPublicKeySpec(new ECPoint(x.bigInteger, y.bigInteger), spec)
    val keyFactory = KeyFactory.getInstance("EC")
    keyFactory.generatePublic(publicKeySpec).asInstanceOf[jECPublicKey]
  }
}

trait EcPrivateKey extends PrivateJwk {
  val kty = "EC"
  def d: BigInt

  protected def spec: ECParameterSpec
  lazy val toJcaPrivateKey: jECPrivateKey = {
    val privateKeySpec = new ECPrivateKeySpec(d.bigInteger, spec)
    val keyFactory = KeyFactory.getInstance("EC")
    keyFactory.generatePrivate(privateKeySpec).asInstanceOf[jECPrivateKey]
  }
}

trait EcKeyPair extends EcPublicKey with EcPrivateKey {
  override val kty = "EC"

  // returns only the public key data from this pair
  def toPublic: EcPublicKey
}

case class P256PublicKey(
                          x: BigInt,
                          y: BigInt,
                          use: Option[String] = None,
                          key_ops: Option[Seq[String]] = None,
                          kid: Option[String] = None
                        ) extends EcPublicKey {
  val crv = "P-256"
  val alg = Some("ES256")

  def spec = P256KeyPair.P256ParameterSpec
}

case class P256KeyPair(
                            d: BigInt,
                            x: BigInt,
                            y: BigInt,
                            use: Option[String] = None,
                            key_ops: Option[Seq[String]] = None,
                            kid: Option[String] = None
                          ) extends EcKeyPair with EcPublicKey with EcPrivateKey {
  val crv = "P-256"
  val alg = Some("ES256")

  lazy val toPublic = P256PublicKey(x, y, use, key_ops, kid)
  def spec = P256KeyPair.P256ParameterSpec
}

object P256KeyPair {
  def generate = {
    val keyGen = KeyPairGenerator.getInstance("EC")
    keyGen.initialize(256, new SecureRandom)
    val pair = keyGen.generateKeyPair
    P256KeyPair(
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
}

