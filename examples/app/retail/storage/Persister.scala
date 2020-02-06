package retail.storage

import cats.data.State
import cats.effect.IO
import cats.effect.concurrent.Ref
import retail.fsm.{
  AllocationState,
  Command,
  Event,
  InventoryItemAdded,
  NewInventoryItem,
  NewOrderItem,
  OrderItemAdded
}

case class Persister(state: Ref[IO, List[Event]])
    extends ((Command, AllocationState) => IO[Event]) {
  override def apply(v1: Command, s1: AllocationState): IO[Event] =
    state.modifyState {
      State { st =>
        val e: Event = v1 match {
          case NewInventoryItem(uuid, sku, level, location) =>
            InventoryItemAdded(uuid = uuid, sku = sku, level = level, location = location)
          case NewOrderItem(uuid, sku) => OrderItemAdded(uuid, sku)
          case _                       => throw new UnsupportedOperationException("not implemented")
        }
        (e :: st, e)
      }
    }
}
