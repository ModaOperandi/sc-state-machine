package retail.model

case class InventoryItem(uuid: Guid[InventoryItem], sku: String, level: Int, location: WmsLocation)
