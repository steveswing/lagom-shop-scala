package be.yannickdeturck.lagomshopscala.order.impl

import be.yannickdeturck.lagomshopscala.item.api.ItemService
import be.yannickdeturck.lagomshopscala.order.api.OrderService
import com.lightbend.lagom.internal.client.CircuitBreakerMetricsProviderImpl
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.rp.servicediscovery.lagom.scaladsl.LagomServiceLocatorComponents
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{Environment, LoggerConfigurator}

import scala.concurrent.ExecutionContext

/**
  * @author Yannick De Turck
  */
trait OrderComponents extends LagomServerComponents with CassandraPersistenceComponents {
  implicit def executionContext: ExecutionContext

  def environment: Environment

  override lazy val lagomServer: LagomServer = serverFor[OrderService](wire[OrderServiceImpl])
  lazy val orderRepository: OrderRepository = wire[OrderRepository]
  override lazy val jsonSerializerRegistry: OrderSerializerRegistry.type = OrderSerializerRegistry

  persistentEntityRegistry.register(wire[OrderEntity])
  readSide.register(wire[OrderEventProcessor])

  val itemService: ItemService
}

abstract class OrderApplication(context: LagomApplicationContext) extends LagomApplication(context)
  with OrderComponents
  with AhcWSComponents
  with CassandraPersistenceComponents
  with LagomKafkaComponents {
  override lazy val itemService: ItemService = serviceClient.implement[ItemService]
}

class OrderApplicationLoader extends LagomApplicationLoader {
  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    // Workaround for logback.xml not being detected, see https://github.com/lagom/lagom/issues/534
    val environment = context.playContext.environment
    LoggerConfigurator(environment.classLoader).foreach {
      _.configure(environment)
    }
    // end workaround
    new OrderApplication(context) with LagomDevModeComponents
  }

  override def load(context: LagomApplicationContext): LagomApplication =
    new OrderApplication(context) with LagomServiceLocatorComponents {
      override lazy val circuitBreakerMetricsProvider = new CircuitBreakerMetricsProviderImpl(actorSystem)
    }

  override def describeService = Some(readDescriptor[OrderService])
}
