package retail.fsm

import retail.model.{ Guid, InventoryItem, OrderItem, WmsLocation }

sealed trait Event

case class InventoryItemAdded(
  uuid: Guid[InventoryItem],
  sku: String,
  level: Int,
  location: WmsLocation
) extends Event

case class OrderItemAdded(uuid: Guid[OrderItem], sku: String) extends Event
