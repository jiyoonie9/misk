package misk.jooq

/**
 * Example base implementation of JooqSignable for JOOQ records.
 * Similar to SignedTimestampedEntity in Hibernate but as an interface.
 * 
 * Records implementing this interface should have 'hash' and 'signature' columns
 * that will be automatically populated by JooqSignedRecordListener.
 * 
 * Example usage:
 * ```
 * class MyTransactionRecord : TransactionRecord(), JooqSignable {
 *   override fun calculateSigningHash(): ByteArray {
 *     val hasher = SigningHasher()
 *     hasher.put(getState())
 *     hasher.put(getAmount()) 
 *     return hasher.getHash()
 *   }
 *   
 *   override fun getName(): String = "Transaction:${getId()}"
 *   
 *   // Hash and signature fields are handled by generated JOOQ code
 * }
 * ```
 */
interface DbSignedEntity : JooqSignable {
  // These would typically be implemented by JOOQ generated code
  override fun getHash(): String?
  override fun getSignature(): String?
  override fun setHash(hash: String?)
  override fun setSignature(signature: String?)
}