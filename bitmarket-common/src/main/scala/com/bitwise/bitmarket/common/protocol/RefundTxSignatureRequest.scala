package com.bitwise.bitmarket.common.protocol

import com.bitwise.bitmarket.common.protorpc.PeerSession

case class RefundTxSignatureRequest(
  exchangeId : String,
  refundTx: Array[Byte]
) {

  override def equals(what: Any) = what match {
    case RefundTxSignatureRequest(id, tx) => (id == exchangeId) && (refundTx.deep == tx.deep)
    case _ => false
  }
}

object RefundTxSignatureRequest {

  implicit val Write = new MessageSend[RefundTxSignatureRequest] {

    def sendAsProto(msg: RefundTxSignatureRequest, session: PeerSession) = ???
  }
}
