package retail.main

import java.util.UUID

import cats.data.State
import cats.effect.concurrent.Ref
import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import com.modaoperandi.sc.statemachine
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import retail.fsm.{
  AllocationResult,
  AllocationState,
  Command,
  Event,
  InventoryItemAdded,
  NewInventoryItem,
  NewOrderItem,
  OrderItemAdded
}
import retail.model.{ Guid, InventoryItem, OrderItem, Radial }
import retail.storage.Persister

object OrderAllocation extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      implicit0(logger: SelfAwareStructuredLogger[IO]) <- Slf4jLogger.fromName[IO]("example")
      persister                                        <- Ref.of[IO, List[Event]](Nil).map(Persister.apply)
      inventoryItemGuid                                <- IO.delay(UUID.randomUUID()).map(uuid => Guid[InventoryItem](uuid))
      (newState, msgs) <- streamOfCommands(inventoryItemGuid)
                           .evalMap(
                             command =>
                               statemachine
                                 .calcNewState[
                                   IO,
                                   Command,
                                   AllocationState,
                                   Event,
                                   AllocationResult
                                 ](command)(
                                   persist = persister,
                                   historyFetcher = _ => persister.state.get,
                                   initialState = () => AllocationState(sku = "123"),
                                   updateState = updateState
                               )
                           )
                           .compile
                           .toList
                           .map { l =>
                             l.last._1 -> l.map(_._2)
                           }
      storedEvts <- persister.state.get
      _          <- logger.info(s"persisted events $storedEvts")
      _          <- logger.info(s"latest state $newState")
      _          <- logger.info(s"generated messages ${msgs.flatten}")
    } yield ()).as(ExitCode.Success)

  def streamOfCommands(inventoryItemGuid: Guid[InventoryItem]): fs2.Stream[IO, Command] =
    fs2.Stream
      .emits(
        Seq(
          NewInventoryItem(
            inventoryItemGuid,
            level = 10,
            sku = "123",
            location = Radial
          ),
          NewOrderItem(Guid[OrderItem](UUID.randomUUID()), "123"),
          NewInventoryItem(inventoryItemGuid, level = 10, sku = "123", location = Radial),
          NewOrderItem(Guid[OrderItem](UUID.randomUUID()), sku = "123")
        )
      )
      .covary[IO]

  def updateState(evt: Event): State[AllocationState, List[AllocationResult]] =
    State[AllocationState, List[AllocationResult]] { state =>
      evt match {
        case OrderItemAdded(uuid, sku) =>
          allocate(state.copy(demand = OrderItem(uuid, sku) :: state.demand))
        case InventoryItemAdded(uuid, sku, level, location) =>
          allocate(state.copy(supply = InventoryItem(uuid, sku, level, location) :: state.supply))
        case _ => state -> Nil
      }
    }

  def allocate(state: AllocationState): (AllocationState, List[AllocationResult]) =
    state match {
      //No demand
      case s @ AllocationState(_, _, Nil) => s -> Nil
      //No Supply
      case s @ AllocationState(_, Nil, _) => s -> Nil
      //Both supply and demand are present, supply has more then 1 level
      case AllocationState(_, inventoryItem :: restSupply, orderItem :: restDemand)
          if inventoryItem.level > 1 =>
        val (st, res) = allocate(
          state.copy(
            supply = inventoryItem.copy(level = inventoryItem.level - 1) :: restSupply,
            demand = restDemand
          )
        )
        (st, AllocationResult(orderItem, inventoryItem) :: res)

      //we have exactly 1 supply item, allocate it and remove from the supply list
      case AllocationState(_, inventoryItem :: restSupply, orderItem :: restDemand)
          if inventoryItem.level == 1 =>
        val (st, res) = allocate(state.copy(supply = restSupply, demand = restDemand))
        (st, AllocationResult(orderItem, inventoryItem) :: res)
    }
}
