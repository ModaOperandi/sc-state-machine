package com.modaoperandi.sc

import cats.{Functor, Monad}
import cats.data.State
import cats.effect.Sync
import cats.effect.concurrent.Ref
import io.chrisdavenport.log4cats.Logger
import cats.implicits._

package object statemachine {
  def nextState[F[_]: Functor, CMD, STATE, EVT, MSG](cmd: CMD, state: STATE)(
    persist: (CMD, STATE) => F[List[EVT]],
    updateState: EVT => State[STATE, List[MSG]]
  ): F[State[STATE, List[MSG]]] =
    persist(cmd, state).map(evts => replayEvents[EVT, STATE, MSG](evts)(updateState))

  def calcNewState[F[_]: Monad: Sync, CMD, STATE, EVT, MSG](cmd: CMD)(
    persist: (CMD, STATE) => F[List[EVT]],
    historyFetcher: CMD => F[List[EVT]],
    initialState: () => STATE,
    updateState: EVT => State[STATE, List[MSG]]
  )(implicit logger: Logger[F]): F[(STATE, List[MSG])] =
    for {
      hist       <- historyFetcher(cmd)
      _          <- logger.debug(s"fetched history $hist for $cmd")
      stateM     = replayEvents[EVT, STATE, MSG](hist)(updateState)
      stateRef   <- Ref.of[F, STATE](initialState())
      _          <- stateRef.modifyState(stateM)
      curState   <- stateRef.get
      _          <- logger.debug(s"replayed history to current state $curState")
      nextStateM <- nextState[F, CMD, STATE, EVT, MSG](cmd, curState)(persist, updateState)
      event      <- stateRef.modifyState(nextStateM)
      newState   <- stateRef.get
      _          <- logger.debug(s"new state $newState after applying command $cmd and event $event emitted")
    } yield newState -> event

  def replayEvents[EVT, STATE, MSG](
    evts: Seq[EVT]
  )(updateState: EVT => State[STATE, List[MSG]]): State[STATE, List[MSG]] =
    evts.foldLeft(State.empty[STATE, List[MSG]]) {
      case (st, evt) => st.flatMap(evts => updateState(evt).map(evts ::: _))
    }
}
