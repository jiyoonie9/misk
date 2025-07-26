package misk.jooq.listeners

import misk.jooq.JooqEntitySigner
import misk.jooq.JooqSignable
import org.jooq.RecordContext
import org.jooq.RecordListener
import wisp.logging.getLogger
import jakarta.inject.Inject

/**
 * Exception thrown when a record's signature is invalid.
 */
class InvalidSignatureException @JvmOverloads constructor(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * A JOOQ RecordListener that automatically signs records with HMAC signatures.
 * Equivalent to Hibernate's SignedEntityEventListener, using EntitySigner pattern.
 */
class JooqSignedRecordListener @Inject constructor(
  private val jooqEntitySigner: JooqEntitySigner
) : RecordListener {

  companion object {
    private val logger = getLogger<JooqSignedRecordListener>()
  }

  override fun insertStart(ctx: RecordContext) = maybeSign(ctx)
  override fun updateStart(ctx: RecordContext) = maybeSign(ctx)
  override fun loadEnd(ctx: RecordContext) = maybeVerify(ctx)

  private fun maybeSign(ctx: RecordContext) {
    val record = ctx.record()
    if (record is JooqSignable) {
      jooqEntitySigner.sign(record)
    }
  }

  private fun maybeVerify(ctx: RecordContext) {
    val record = ctx.record()
    if (record is JooqSignable && record.maybeGetSignature() != null) {
      if (!jooqEntitySigner.verify(record)) {
        throw InvalidSignatureException("Signature did not verify. [entity=${record.getName()}]")
      }
    }
  }
}

/**
 * Configuration options for JooqSignedRecordListener
 */
data class JooqSignedRecordListenerOptions @JvmOverloads constructor(
  val install: Boolean,
  val enableVerification: Boolean = true
)