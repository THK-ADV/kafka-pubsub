package subscriber

import config.{AppLifecycle, KafkaConfig}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.kstream.Consumed
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde

final class KafkaSubscriber[A](
    val config: KafkaConfig,
    val appLifecycle: AppLifecycle,
    val subscriptions: List[Subscription[A]]
) extends KafkaStreamSubscriber[A] {

  override protected def consumer = builder =>
    subscriptions.foreach { s =>
      implicit val serde: Serde[A] = s.serde
      builder
        .stream[String, A](s.topic)(
          Consumed.`with`(stringSerde, s.serde)
        )
        .foreach(s.f)
    }

}
