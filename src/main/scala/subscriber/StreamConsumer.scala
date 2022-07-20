package subscriber

import org.apache.kafka.streams.scala.StreamsBuilder

trait StreamConsumer {
  def consume(builder: StreamsBuilder): Unit
}
