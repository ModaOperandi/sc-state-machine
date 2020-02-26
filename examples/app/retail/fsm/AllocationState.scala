package retail.fsm

import retail.model.{ InventoryItem, OrderItem }

case class AllocationState(
  sku: String,
  supply: List[InventoryItem] = Nil,
  demand: List[OrderItem] = Nil
)
