package com.modaoperandi.sc.statemachine.storage

import cats.data.State
import cats.effect.IO
import cats.effect.concurrent.Ref
import com.modaoperandi.sc.statemachine.fsm.{
  Add,
  Added,
  Command,
  Event,
  MathState,
  Subtract,
  Subtracted
}

case class Persister(totalOrderEvents: Ref[IO, List[Event]])
    extends ((Command, MathState) => IO[Event]) {
  override def apply(v1: Command, s1: MathState): IO[Event] =
    totalOrderEvents.modifyState {
      State { st =>
        val e: Event = v1 match {
          case Add(v)      => Added(v)
          case Subtract(v) => Subtracted(v)
        }
        (e :: st, e)
      }
    }
}
