package subscriber

import config.{AppLifecycle, KafkaConfig}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import java.util.Properties
import scala.concurrent.Future
import scala.util.Try

final class KafkaSubscriber[A](
    private val config: KafkaConfig,
    private val appLifecycle: AppLifecycle,
    private val consumer: StreamConsumer
) {

  private val props = buildProperties(config.server, config.applicationId)
  private val builder = new StreamsBuilder()
  consumer.consume(builder)
  private val topology = builder.build()
  private val app = new KafkaStreams(topology, props)
  app.start()

  private def buildProperties(
      serverUrl: String,
      applicationId: String
  ): Properties = {
    val props = new Properties
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, serverUrl)
    props.put(
      StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
      Serdes.stringSerde.getClass
    )
    props
  }

  appLifecycle.onStop { () =>
    Future.fromTry(Try(close()))
  }

  def close(): Unit = app.close()
}
