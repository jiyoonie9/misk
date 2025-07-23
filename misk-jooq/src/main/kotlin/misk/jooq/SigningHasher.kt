package misk.jooq

import java.security.MessageDigest

/**
 * Helper class for calculating consistent signing hashes.
 * Similar to SigningHasher from the Hibernate example.
 */
class SigningHasher {
  private val digest = MessageDigest.getInstance("SHA-256")

  fun put(value: Any?) {
    when (value) {
      null -> digest.update("null".toByteArray())
      is String -> digest.update(value.toByteArray())
      is Number -> digest.update(value.toString().toByteArray())
      is Boolean -> digest.update(value.toString().toByteArray())
      is Enum<*> -> digest.update(value.name.toByteArray())
      else -> digest.update(value.toString().toByteArray())
    }
    digest.update("|".toByteArray()) // separator
  }

  fun getHash(): ByteArray = digest.digest()
}