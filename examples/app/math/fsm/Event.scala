package math.fsm

sealed trait Event
case class Added(v: Int)      extends Event
case class Subtracted(v: Int) extends Event
