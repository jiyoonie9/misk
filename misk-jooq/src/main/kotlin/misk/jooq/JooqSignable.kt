package misk.jooq

import java.util.Base64

/**
 * JOOQ equivalent of Hibernate's Signable interface.
 * Records implementing this interface will be automatically signed by JooqSignedRecordListener.
 */
interface JooqSignable {
  /**
   * Calculate the signing hash for this entity.
   * This should return a deterministic hash based on the entity's data.
   */
  fun calculateSigningHash(): ByteArray

  /**
   * Sign the entity with the given hash and signature.
   */
  fun sign(hash: ByteArray, signature: ByteArray) {
    setHash(Base64.getEncoder().encodeToString(hash))
    setSignature(Base64.getEncoder().encodeToString(signature))
  }

  /**
   * Get the signature as bytes, or null if no signature is set.
   */
  fun maybeGetSignature(): ByteArray? {
    return getSignature()?.let { Base64.getDecoder().decode(it) }
  }

  /**
   * Get the entity name for logging and error messages.
   */
  fun getName(): String

  // Abstract methods that must be implemented by the record
  fun getHash(): String?
  fun getSignature(): String?
  fun setHash(hash: String?)
  fun setSignature(signature: String?)
}