package math.fsm

sealed trait Command

case class Add(v: Int)      extends Command
case class Subtract(v: Int) extends Command
