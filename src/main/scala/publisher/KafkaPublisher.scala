package publisher

import config.{AppLifecycle, KafkaConfig}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}

import java.util.Properties
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

final class KafkaPublisher[A](
    private val config: KafkaConfig,
    private val appLifecycle: AppLifecycle,
    private val serializer: Class[_ <: Serializer[A]]
) {

  type OnComplete = (Record, Try[RecordMetadata]) => Unit
  type Record = (String, A)

  private val props = buildProperties(config.server)
  private val producer = new KafkaProducer[String, A](props)

  private def callback(record: Record, f: OnComplete): Callback =
    (metadata: RecordMetadata, exception: Exception) =>
      f(
        record,
        if (exception == null)
          Success(metadata)
        else
          Failure(exception)
      )

  def publishComplete(
      topic: String
  )(records: Seq[Record])(onComplete: OnComplete): Unit = {
    records map { case (k, v) =>
      val record = new ProducerRecord(topic, k, v)
      producer.send(record, callback((k, v), onComplete))
    }
    producer.flush()
  }

  def publish(topic: String)(records: Seq[Record]): Unit =
    publishComplete(topic)(records)((_, _) => ())

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
