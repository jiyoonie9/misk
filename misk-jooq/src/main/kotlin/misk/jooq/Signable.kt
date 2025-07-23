package misk.jooq

import java.util.Base64

/**
 * Interface for records that can be signed with HMAC signatures.
 * Similar to the Hibernate Signable interface but for JOOQ records.
 */
interface Signable {
  fun getHash(): String?
  fun getSignature(): String?
  fun setHash(hash: String?)
  fun setSignature(signature: String?)

  /**
   * Get the signature as bytes, or null if no signature is set.
   */
  fun maybeGetSignature(): ByteArray? {
    return getSignature()?.let { Base64.getDecoder().decode(it) }
  }

  /**
   * Sign the record with the given hash and signature bytes.
   */
  fun sign(hash: ByteArray, signature: ByteArray) {
    setHash(Base64.getEncoder().encodeToString(hash))
    setSignature(Base64.getEncoder().encodeToString(signature))
  }

  /**
   * Calculate the signing hash for this record.
   * Implement this method to define what data should be included in the signature.
   */
  fun calculateSigningHash(): ByteArray
}