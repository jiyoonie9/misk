package misk.jooq.listeners

import misk.jooq.HmacSha256
import misk.jooq.JooqEntitySigner
import misk.jooq.JooqSignable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jooq.Record
import org.jooq.RecordContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.google.inject.Provider

class JooqSignedRecordListenerTest {

  private lateinit var listener: JooqSignedRecordListener
  private lateinit var testRecord: TestSignableRecord
  private val testSigningKey = "test-key".toByteArray()

  @BeforeEach
  fun setUp() {
    val hmacProvider = Provider { HmacSha256(testSigningKey) }
    val entitySigner = JooqEntitySigner(hmacProvider)
    listener = JooqSignedRecordListener(entitySigner)

    testRecord = TestSignableRecord(id = 1L, data = "test-data")
  }

  @Test
  fun `should sign record on insert`() {
    val mockRecord = mock<Record>()
    whenever(mockRecord.toString()).thenReturn("MockRecord")
    
    val signable = TestSignableRecord(1L, "test-data")
    val signableRecord = SignableRecordWrapper(mockRecord, signable)
    
    val context = mock<RecordContext>()
    whenever(context.record()).thenReturn(signableRecord)

    listener.insertStart(context)

    assertThat(signable.getHash()).isNotNull()
    assertThat(signable.getSignature()).isNotNull()
    assertThat(signable.getHash()).isNotEmpty()
    assertThat(signable.getSignature()).isNotEmpty()
  }

  @Test
  fun `should verify valid signature on load`() {
    val mockRecord = mock<Record>()
    whenever(mockRecord.toString()).thenReturn("MockRecord")
    
    val signable = TestSignableRecord(1L, "test-data")
    val signableRecord = SignableRecordWrapper(mockRecord, signable)
    
    val context = mock<RecordContext>()
    whenever(context.record()).thenReturn(signableRecord)

    // Sign first
    listener.insertStart(context)
    
    // Verify should pass
    listener.loadEnd(context)
  }

  @Test
  fun `should throw on invalid signature`() {
    val mockRecord = mock<Record>()
    whenever(mockRecord.toString()).thenReturn("MockRecord")
    
    val signable = TestSignableRecord(1L, "test-data")
    // Set invalid signature
    signable.setHash("invalid")
    signable.setSignature("invalid")
    
    val signableRecord = SignableRecordWrapper(mockRecord, signable)
    
    val context = mock<RecordContext>()
    whenever(context.record()).thenReturn(signableRecord)

    assertThatThrownBy {
      listener.loadEnd(context)
    }.isInstanceOf(InvalidSignatureException::class.java)
  }

  // Wrapper that implements both Record and JooqSignable
  private class SignableRecordWrapper(
    private val record: Record,
    private val signable: JooqSignable
  ) : Record by record, JooqSignable by signable

  // Test record that implements JooqSignable
  private class TestSignableRecord(
    private val id: Long,
    private val data: String
  ) : JooqSignable {
    private var hash: String? = null
    private var signature: String? = null

    override fun calculateSigningHash(): ByteArray {
      return "id:$id;data:$data".toByteArray()
    }

    override fun getName(): String = "TestRecord:$id"
    override fun getHash(): String? = hash
    override fun getSignature(): String? = signature
    override fun setHash(hash: String?) { this.hash = hash }
    override fun setSignature(signature: String?) { this.signature = signature }
  }

}