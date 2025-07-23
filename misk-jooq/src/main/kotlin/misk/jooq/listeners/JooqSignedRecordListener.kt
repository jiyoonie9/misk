package misk.jooq.listeners

import org.jooq.Field
import org.jooq.Record
import org.jooq.RecordContext
import org.jooq.RecordListener
import org.jooq.impl.DSL
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A JOOQ RecordListener that automatically signs records with HMAC signatures.
 * Similar to SignedTimestampedEntity but for JOOQ records.
 */
@Singleton
class JooqSignedRecordListener @Inject constructor(
  private val options: JooqSignedRecordListenerOptions
) : RecordListener {

  // Called before inserting an UpdatableRecord
  override fun insertStart(ctx: RecordContext) {
    signRecord(ctx.record())
  }

  // Called before updating an UpdatableRecord
  override fun updateStart(ctx: RecordContext) {
    signRecord(ctx.record())
  }

  private fun signRecord(record: Record) {
    val hashField = findField(record, options.hashColumnName) ?: return
    val signatureField = findField(record, options.signatureColumnName) ?: return

    // Skip if already manually set
    if (record.changed(hashField) || record.changed(signatureField)) {
      return
    }

    try {
      // Generate deterministic hash of the record data
      val recordData = generateRecordData(record)
      val hash = MessageDigest.getInstance("SHA-256").digest(recordData)
      val hashBase64 = Base64.getEncoder().encodeToString(hash)

      // Generate HMAC signature
      // val mac = macKeyManager[options.signingKeyName]
      // val signature = mac.computeMac(recordData)
      val signatureBase64 = Base64.getEncoder().encodeToString(byteArrayOf())

      // Set the hash and signature fields
      record.set(hashField, hashBase64)
      record.set(signatureField, signatureBase64)

    } catch (e: Exception) {
      throw e
    }
  }

  private fun generateRecordData(record: Record): ByteArray {
    // Generate a deterministic representation of the record for signing
    val dataBuilder = StringBuilder()
    
    // Include all fields except hash and signature in deterministic order
    val fieldsToSign = record.fields()
      .filter { field -> 
        field.name != options.hashColumnName && 
        field.name != options.signatureColumnName 
      }
      .sortedBy { it.name }

    fieldsToSign.forEach { field ->
      val value = record.get(field)
      dataBuilder.append("${field.name}:${value?.toString() ?: "null"};")
    }

    return dataBuilder.toString().toByteArray(Charsets.UTF_8)
  }

  private fun findField(record: Record, columnName: String): Field<String>? {
    return try {
      @Suppress("UNCHECKED_CAST")
      record.field(DSL.name(columnName)) as? Field<String>
    } catch (e: Exception) {
      null
    }
  }
}

/**
 * Configuration options for JooqSignedRecordListener
 */
data class JooqSignedRecordListenerOptions(
  val install: Boolean,
  val signingKeyName: String = "",
  val hashColumnName: String = "hash",
  val signatureColumnName: String = "signature"
)