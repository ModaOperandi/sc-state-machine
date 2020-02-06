package retail.model

sealed trait WmsLocation { def name: String }

object WmsLocation {
  def fromNSid(nsId: Int): Option[WmsLocation] =
    nsId match {
      case 1 => Some(Radial)
      case _ => None
    }
}

case object Radial extends WmsLocation {
  override def name: String = "NY Warehouse"
}
