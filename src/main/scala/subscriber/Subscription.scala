package subscriber

import org.apache.kafka.common.serialization.Serde

case class Subscription[A](
    topic: String,
    serde: Serde[A],
    f: (String, A) => Unit
)
