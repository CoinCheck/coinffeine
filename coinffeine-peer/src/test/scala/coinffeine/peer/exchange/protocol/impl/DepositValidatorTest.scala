package coinffeine.peer.exchange.protocol.impl

import scala.collection.JavaConversions._

import com.google.bitcoin.core.Transaction.SigHash
import org.scalatest.Inside

import coinffeine.model.bitcoin.{ImmutableTransaction, MutableTransaction}
import coinffeine.model.currency.Implicits._
import coinffeine.model.exchange.Both

class DepositValidatorTest extends ExchangeTest with Inside {

  "Valid deposits" should "not be built from an invalid buyer commitment transaction" in
    new Fixture {
      val deposits = Both(
        buyer = ImmutableTransaction(invalidFundsCommitment),
        seller = sellerHandshake.myDeposit
      )
      inside(validator.validate(deposits)) {
        case Both(buyerResult, sellerResult) =>
          buyerResult should be ('failure)
          sellerResult should be ('success)
      }
  }

  it should "not be built from an invalid seller commitment transaction" in new Fixture {
    private val deposits = Both(
      buyer = buyerHandshake.myDeposit,
      seller = ImmutableTransaction(invalidFundsCommitment)
    )
    inside(validator.validate(deposits)) {
      case Both(buyerResult, sellerResult) =>
        buyerResult should be ('success)
        sellerResult should be ('failure)
    }
  }

  trait Fixture extends BuyerHandshake with SellerHandshake {
    sendMoneyToWallet(sellerWallet.delegate, 10.BTC)
    val invalidFundsCommitment = new MutableTransaction(parameters.network)
    invalidFundsCommitment.addInput(sellerWallet.delegate.calculateAllSpendCandidates(true).head)
    invalidFundsCommitment.addOutput(5.BTC.asSatoshi, sellerWallet.delegate.getKeys.head)
    invalidFundsCommitment.signInputs(SigHash.ALL, sellerWallet.delegate)
    val validator = new DepositValidator(amounts, buyerHandshakingExchange.requiredSignatures)
  }
}
