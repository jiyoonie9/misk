package misk.jooq

import com.google.common.hash.Hashing
import wisp.logging.getLogger
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * JOOQ equivalent of Hibernate's EntitySigner.
 * Uses dependency injection with HmacSha256 provider, similar to the cash-server pattern.
 */
@Singleton
class JooqEntitySigner @Inject constructor(
  private val hmacFunctionProvider: Provider<HmacSha256>
) {

  companion object {
    private val logger = getLogger<JooqEntitySigner>()
  }

  fun sign(signable: JooqSignable) {
    try {
      val signingHash = signable.calculateSigningHash()
      val hmacFunction = hmacFunctionProvider.get()
      val signature = hmacFunction.hashBytes(signingHash)
      signable.sign(signingHash, signature)
      
      logger.debug { "Signed entity: ${signable.getName()}" }
    } catch (e: Exception) {
      logger.error(e) { "Failed to sign entity: ${signable.getName()}" }
      throw e
    }
  }

  fun verify(signable: JooqSignable): Boolean {
    return try {
      val expectedSigningHash = signable.calculateSigningHash()
      val storedSignature = signable.maybeGetSignature() ?: return false
      
      val hmacFunction = hmacFunctionProvider.get()
      val expectedSignature = hmacFunction.hashBytes(expectedSigningHash)

      expectedSignature.contentEquals(storedSignature)
    } catch (e: Exception) {
      logger.error(e) { "Failed to verify signature for entity: ${signable.getName()}" }
      false
    }
  }
}

/**
 * Simple wrapper around Guava's HMAC-SHA256, equivalent to cash-server's HmacSha256.
 */
class HmacSha256(private val key: ByteArray) {
  fun hashBytes(data: ByteArray): ByteArray {
    return Hashing.hmacSha256(key).hashBytes(data).asBytes()
  }
}