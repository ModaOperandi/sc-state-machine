package com.modaoperandi.sc.statemachine.fsm

sealed trait Event
case class Added(v: Int)      extends Event
case class Subtracted(v: Int) extends Event
