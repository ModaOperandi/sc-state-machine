package retail.fsm

import retail.model.{ InventoryItem, OrderItem }

case class AllocationResult(orderItem: OrderItem, inventoryItem: InventoryItem)
