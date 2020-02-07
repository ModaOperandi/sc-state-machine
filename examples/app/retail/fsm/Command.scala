package retail.fsm

import retail.model.{ Guid, InventoryItem, OrderItem, WmsLocation }

sealed trait Command

case class NewInventoryItem(
  uuid: Guid[InventoryItem],
  sku: String,
  level: Int,
  location: WmsLocation
) extends Command

case class UpdateInventoryItemLevel(uuid: Guid[InventoryItem], sku: String, level: Int)
    extends Command

case class UpdateInventoryItemLocation(
  uuid: Guid[InventoryItem],
  sku: String,
  location: WmsLocation
) extends Command

case class DeleteInventoryItem(uuid: Guid[InventoryItem], sku: String) extends Command

case class NewOrderItem(uuid: Guid[OrderItem], sku: String) extends Command
