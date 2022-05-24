package com.example

import com.wixpress.dst.greyhound.core.Serdes
import com.wixpress.dst.greyhound.core.consumer.domain.ConsumerRecord
import com.wixpress.dst.greyhound.core.metrics.GreyhoundMetrics
import com.wixpress.dst.greyhound.core.producer.ProducerRecord
import com.wixpress.dst.greyhound.future.GreyhoundConsumer.aRecordHandler
import com.wixpress.dst.greyhound.future._
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import com.wixpress.dst.greyhound.testkit._
import zio.ZIO
import zio.blocking.Blocking

class GreyhoundTestkitTest extends AnyFreeSpecLike with LogSupport with Matchers {

  import ExecutionContext.Implicits.global

  val topic    = s"${this.suiteName}_topic"
  val group    = s"${this.suiteName}_group"
  val clientId = s"${this.suiteName}_client"

  "must compile" in {

    // TODO - does not compile:
    // type mismatch;
    // found   : Unit
    // required: zio.ZIO[?,?,?]
    val x: ZIO[Blocking with GreyhoundMetrics, Throwable, Nothing] = ManagedKafka.make(ManagedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2181, Map.empty)).use {
      kafka =>

        val bootstrapServers = kafka.bootstrapServers

        println(s"bootstrap servers: ${bootstrapServers}")

      val config = GreyhoundConfig(bootstrapServers)

      val recordHandler = new RecordHandler[Int, String] {
        override def handle(record: ConsumerRecord[Int, String])(implicit
            ec: ExecutionContext
        ): Future[Any] =
          Future {
            logger.info(s"record: ${record}")
            /* Your handling logic */
          }(ExecutionContext.global)
      }

      val consumer = GreyhoundConsumer(
        initialTopics = Set(topic),
        group = group,
        handle = aRecordHandler {
          recordHandler
        },
        keyDeserializer = Serdes.IntSerde,
        valueDeserializer = Serdes.StringSerde,
        clientId = clientId
      )

      val consumerBuilder = GreyhoundConsumersBuilder(config)
        .withConsumer(consumer)

      val tested: Future[Unit] = for {
        // Start consuming
        consumers <- consumerBuilder.build

        // Create a producer and produce to topic
        producer <- GreyhoundProducerBuilder(config).build
        _ <- producer.produce(
          record = ProducerRecord(topic, "hello world", Some(123)),
          keySerializer = Serdes.IntSerde,
          valueSerializer = Serdes.StringSerde
        )

        // Shutdown all consumers and producers
        _ <- producer.shutdown
        _ <- consumers.shutdown
      } yield ()

      Await.result(tested, 10.seconds)
    }

  }

}
