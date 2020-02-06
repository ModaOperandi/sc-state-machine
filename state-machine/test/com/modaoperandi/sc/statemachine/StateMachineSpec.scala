package com.modaoperandi.sc.statemachine

import cats.data.State
import cats.effect.IO
import cats.effect.concurrent.Ref
import com.modaoperandi.sc.statemachine
import com.modaoperandi.sc.statemachine.fsm.{
  Add,
  Added,
  Command,
  Event,
  MathState,
  Subtract,
  Subtracted
}
import com.modaoperandi.sc.statemachine.storage.Persister
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StateMachineSpec extends AnyWordSpec with Matchers {
  "State Machine" should {

    //This is core of the Math State machine, all state transitions logic is here
    def updateState(evt: Event): State[MathState, List[Event]] =
      State[MathState, List[Event]] { state =>
        evt match {
          case Added(v)      => state.copy(state.result + v) -> (evt :: Nil)
          case Subtracted(v) => state.copy(state.result - v) -> (evt :: Nil)
        }
      }

    implicit val logger: SelfAwareStructuredLogger[IO] =
      Slf4jLogger.getLoggerFromName[IO]("example")

    val persister = Persister(Ref.unsafe[IO, List[Event]](Nil))

    val machine = statemachine.calcNewState[IO, Command, MathState, Event, Event](_: Command)(
      persist = persister,
      historyFetcher = _ => persister.totalOrderEvents.get,
      initialState = () => MathState(0),
      updateState = updateState
    )

    val replayer: Seq[Event] => State[MathState, List[Event]] =
      statemachine.replayEvents[Event, MathState, Event](_)(updateState)

    "persist event" in {
      val eventsIO = for {
        _          <- machine(Add(1))
        storedEvts <- persister.totalOrderEvents.get
      } yield storedEvts

      eventsIO.unsafeRunSync() should be(List(Added(1)))
    }

    "persist total order of events" in {
      val eventsIO = for {
        _          <- machine(Subtract(3))
        storedEvts <- persister.totalOrderEvents.get
      } yield storedEvts

      eventsIO.unsafeRunSync() should be(List(Subtracted(3), Added(1)))
    }

    "calculate final state" in {
      val eventsIO = for {
        (state, _) <- machine(Add(5))
        _          <- persister.totalOrderEvents.get
      } yield state

      eventsIO.unsafeRunSync() should be(MathState(3))
    }

    "replay all messages from the store and restore final state" in {
      val eventsIO = for {
        state    <- persister.totalOrderEvents.get.map(evts => replayer(evts.reverse))
        ref      <- Ref.of[IO, MathState](MathState(0))
        _        <- ref.modifyState(state)
        replayed <- ref.get
      } yield replayed

      eventsIO.unsafeRunSync() should be(MathState(3))
    }
  }
}
