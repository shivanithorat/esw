package esw.ocs.framework.api.models.codecs

import csw.command.client.cbor.MessageCodecs
import csw.location.api.codec.DoneCodec
import esw.ocs.framework.api.models.StepStatus.Finished.{Failure, Success}
import esw.ocs.framework.api.models.StepStatus._
import esw.ocs.framework.api.models.messages.ProcessSequenceError.{DuplicateIdsFound, ExistingSequenceIsInProcess}
import esw.ocs.framework.api.models.messages.SequencerMsg._
import esw.ocs.framework.api.models.messages.StepListError._
import esw.ocs.framework.api.models.messages.{ProcessSequenceError, StepListError}
import esw.ocs.framework.api.models.{Sequence, Step, StepList, StepStatus}
import io.bullet.borer.Codec
import io.bullet.borer.derivation.MapBasedCodecs.deriveCodec

trait SequencerCodecs extends MessageCodecs with DoneCodec {

  //SequencerMsgCodecs
  implicit lazy val processSequenceCodec: Codec[ProcessSequence]         = deriveCodec[ProcessSequence]
  implicit lazy val shutdownSequencerCodec: Codec[Shutdown]              = deriveCodec[Shutdown]
  implicit lazy val abortCodec: Codec[Abort]                             = deriveCodec[Abort]
  implicit lazy val availableCodec: Codec[Available]                     = deriveCodec[Available]
  implicit lazy val getSequenceCodec: Codec[GetSequence]                 = deriveCodec[GetSequence]
  implicit lazy val getPreviousSequenceCodec: Codec[GetPreviousSequence] = deriveCodec[GetPreviousSequence]
  implicit lazy val addCodec: Codec[Add]                                 = deriveCodec[Add]
  implicit lazy val prependCodec: Codec[Prepend]                         = deriveCodec[Prepend]
  implicit lazy val replaceCodec: Codec[Replace]                         = deriveCodec[Replace]
  implicit lazy val insertAfterCodec: Codec[InsertAfter]                 = deriveCodec[InsertAfter]
  implicit lazy val deleteCodec: Codec[Delete]                           = deriveCodec[Delete]
  implicit lazy val addBreakpointCodec: Codec[AddBreakpoint]             = deriveCodec[AddBreakpoint]
  implicit lazy val removeBreakpointCodec: Codec[RemoveBreakpoint]       = deriveCodec[RemoveBreakpoint]
  implicit lazy val pauseCodec: Codec[Pause]                             = deriveCodec[Pause]
  implicit lazy val resumeCodec: Codec[Resume]                           = deriveCodec[Resume]
  implicit lazy val resetCodec: Codec[Reset]                             = deriveCodec[Reset]

  implicit lazy val externalSequencerMsgCodec: Codec[ExternalSequencerMsg] = deriveCodec[ExternalSequencerMsg]

  implicit lazy val stepCodec: Codec[Step]         = deriveCodec[Step]
  implicit lazy val stepListCodec: Codec[StepList] = deriveCodec[StepList]

  // StepCodecs
  implicit lazy val successStatusCodec: Codec[Success] = deriveCodec[Success]
  implicit lazy val failureStatusCodec: Codec[Failure] = deriveCodec[Failure]

  implicit lazy val pendingStatusCodec: Codec[Pending.type]   = singletonCodec(Pending)
  implicit lazy val inflightStatusCodec: Codec[InFlight.type] = singletonCodec(InFlight)
  implicit lazy val finishedStatusCodec: Codec[Finished]      = deriveCodec[Finished]

  implicit lazy val stepStatusCodec: Codec[StepStatus] = deriveCodec[StepStatus]

  //SequenceCodec
  implicit lazy val sequenceCodec: Codec[Sequence] = deriveCodec[Sequence]

  //ProcessSequenceErrorCodecs
  implicit lazy val duplicateIdsFoundCodec: Codec[DuplicateIdsFound.type] = singletonCodec(DuplicateIdsFound)
  implicit lazy val existingSequenceIsInProcessCodec: Codec[ExistingSequenceIsInProcess.type] =
    singletonCodec(ExistingSequenceIsInProcess)
  implicit lazy val processSequenceErrorCodec: Codec[ProcessSequenceError] = deriveCodec[ProcessSequenceError]

  //StepListErrorCodecs

  implicit lazy val notSupportedCodec: Codec[NotSupported] = deriveCodec[NotSupported]
  implicit lazy val notAllowedOnFinishedSeqCodec: Codec[NotAllowedOnFinishedSeq.type] =
    singletonCodec(NotAllowedOnFinishedSeq)

  implicit lazy val idDoesNotExistCodec: Codec[IdDoesNotExist] = deriveCodec[IdDoesNotExist]
  implicit lazy val pauseFailedCodec: Codec[PauseFailed.type]  = singletonCodec(PauseFailed)
  implicit lazy val addingBreakpointNotSupportedCodec: Codec[AddingBreakpointNotSupported] =
    deriveCodec[AddingBreakpointNotSupported]
  implicit lazy val updateNotSupportedCodec: Codec[UpdateNotSupported] = deriveCodec[UpdateNotSupported]
  implicit lazy val addFailedCodec: Codec[AddFailed.type]              = singletonCodec(AddFailed)

  implicit lazy val stepListErrorCodec: Codec[StepListError] = deriveCodec[StepListError]

}
