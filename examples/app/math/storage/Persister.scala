package math.storage

import cats.data.State
import cats.effect.IO
import cats.effect.concurrent.Ref
import math.fsm.{ Add, Added, Command, Event, MathState, Subtract, Subtracted }

case class Persister(state: Ref[IO, List[Event]])
    extends ((Command, MathState) => IO[List[Event]]) {
  override def apply(v1: Command, s1: MathState): IO[List[Event]] =
    state.modifyState {
      State { st =>
        val e: Event = v1 match {
          case Add(v)      => Added(v)
          case Subtract(v) => Subtracted(v)
        }
        (e :: st, e :: Nil)
      }
    }
}
