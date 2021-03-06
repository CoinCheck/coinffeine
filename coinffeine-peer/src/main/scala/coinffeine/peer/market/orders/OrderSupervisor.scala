package coinffeine.peer.market.orders

import akka.actor._

import coinffeine.model.currency.FiatCurrency
import coinffeine.model.market.{Order, OrderId}
import coinffeine.peer.CoinffeinePeerActor._

/** Manages orders */
private class OrderSupervisor(orderActorProps: OrderSupervisor.OrderActorProps,
                              submissionSupervisorProps: Props)
  extends Actor with ActorLogging {

  private val submission = context.actorOf(submissionSupervisorProps, "submission")
  private var orders = Map.empty[OrderId, ActorRef]

  override def receive: Receive = {

    case OpenOrder(order) =>
      val ref = context.actorOf(orderActorProps(order, submission), s"order-${order.id.value}")
      orders += order.id -> ref

    case CancelOrder(orderId, reason) =>
      orders.get(orderId).foreach(_ ! OrderActor.CancelOrder(reason))
      orders = orders.filterNot(_._1 == orderId)
  }
}

object OrderSupervisor {

  /** Props of a submission supervisor given a message gateway */
  type SubmissionSupervisorProps = ActorRef => Props

  type OrderActorProps = (Order[_ <: FiatCurrency], ActorRef) => Props

  def props(orderActorProps: OrderActorProps, submissionSupervisorProps: Props) =
    Props(new OrderSupervisor(orderActorProps, submissionSupervisorProps))
}
