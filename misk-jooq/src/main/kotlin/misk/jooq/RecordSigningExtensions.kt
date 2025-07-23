package misk.jooq

import org.jooq.Record
import org.jooq.impl.DSL
import java.util.Base64

/**
 * Extension functions to add signing capabilities to any JOOQ Record.
 * These functions work with records that have 'hash' and 'signature' fields.
 */

/**
 * Get the hash value from a record.
 */
fun Record.getRecordHash(): String? {
  return try {
    this.get(DSL.field("hash", String::class.java))
  } catch (e: Exception) {
    null
  }
}

/**
 * Get the signature value from a record.
 */
fun Record.getRecordSignature(): String? {
  return try {
    this.get(DSL.field("signature", String::class.java))
  } catch (e: Exception) {
    null
  }
}

/**
 * Set the hash value on a record.
 */
fun Record.setRecordHash(hash: String?) {
  try {
    this.set(DSL.field("hash", String::class.java), hash)
  } catch (e: Exception) {
    // Field doesn't exist, silently ignore
  }
}

/**
 * Set the signature value on a record.
 */
fun Record.setRecordSignature(signature: String?) {
  try {
    this.set(DSL.field("signature", String::class.java), signature)
  } catch (e: Exception) {
    // Field doesn't exist, silently ignore
  }
}

/**
 * Get the signature as bytes, or null if no signature is set.
 */
fun Record.maybeGetSignatureBytes(): ByteArray? {
  return getRecordSignature()?.let { Base64.getDecoder().decode(it) }
}

/**
 * Sign the record with the given hash and signature bytes.
 */
fun Record.signRecord(hash: ByteArray, signature: ByteArray) {
  setRecordHash(Base64.getEncoder().encodeToString(hash))
  setRecordSignature(Base64.getEncoder().encodeToString(signature))
}

/**
 * Check if a record has signing fields (hash and signature columns).
 */
fun Record.hasSigningFields(): Boolean {
  return try {
    this.field("hash") != null && this.field("signature") != null
  } catch (e: Exception) {
    false
  }
}