package math.main

import cats.data.State
import cats.effect.concurrent.Ref
import cats.effect.{ ExitCode, IO, IOApp }
import com.modaoperandi.sc.statemachine
import io.chrisdavenport.log4cats.SelfAwareStructuredLogger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import math.fsm.{ Add, Added, Command, Event, MathState, Subtract, Subtracted }
import math.storage.Persister

object Math extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      implicit0(logger: SelfAwareStructuredLogger[IO]) <- Slf4jLogger.fromName[IO]("example")
      persister                                        <- Ref.of[IO, List[Event]](Nil).map(Persister.apply)
      machine = statemachine.calcNewState[IO, Command, MathState, Event, Event](_: Command)(
        persist = persister,
        historyFetcher = _ => persister.state.get,
        initialState = () => MathState(0),
        updateState = updateState
      )
      (newState, msgs) <- streamOfCommands()
                           .evalMap(machine)
                           .compile
                           .toList
                           .map { l =>
                             l.last._1 -> l.map(_._2)
                           }
      storedEvts <- persister.state.get
      _          <- logger.info(s"persisted events $storedEvts")
      _          <- logger.info(s"latest state $newState")
      _          <- logger.info(s"generated messages ${msgs.flatten.toList}")
    } yield ()).as(ExitCode.Success)

  def streamOfCommands(): fs2.Stream[IO, Command] =
    fs2.Stream
      .emits(
        Seq(Add(1), Subtract(4), Add(5), Add(10))
      )
      .covary[IO]

  def updateState(evt: Event): State[MathState, List[Event]] =
    State[MathState, List[Event]] { state =>
      evt match {
        case Added(v)      => state.copy(state.result + v) -> (evt :: Nil)
        case Subtracted(v) => state.copy(state.result - v) -> (evt :: Nil)
      }
    }
}
