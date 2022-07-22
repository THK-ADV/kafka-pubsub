package publisher

import config.{AppLifecycle, KafkaConfig}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}

import java.util.Properties
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

final class KafkaPublisher[A](
    private val config: KafkaConfig,
    private val topic: String,
    private val appLifecycle: AppLifecycle,
    private val serializer: Class[_ <: Serializer[A]]
) {

  type OnComplete = (Record[A], Try[RecordMetadata]) => Unit

  private val props = buildProperties(config.server)
  private val producer = new KafkaProducer[String, A](props)

  private def callback(record: Record[A], f: OnComplete): Callback =
    (metadata: RecordMetadata, exception: Exception) =>
      f(
        record,
        if (exception == null)
          Success(metadata)
        else
          Failure(exception)
      )

  def publishComplete(records: Seq[Record[A]])(onComplete: OnComplete): Unit = {
    records map { record =>
      producer.send(
        new ProducerRecord(topic, record.key, record.value),
        callback(record, onComplete)
      )
    }
    producer.flush()
  }

  def publish(records: Seq[Record[A]]): Unit =
    publishComplete(records)((_, _) => ())

  def close(): Unit = producer.close()

  private def buildProperties(serverUrl: String): Properties = {
    val properties = new Properties
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverUrl)
    properties.put(
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
      classOf[StringSerializer]
    )
    properties.put(
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
      serializer
    )
    properties
  }

  appLifecycle.onStop { () =>
    Future.fromTry(Try(close()))
  }
}
