package coinffeine.peer.api

import scala.concurrent.Future

import coinffeine.model.bitcoin.{WalletProperties, Address, Hash, KeyPair}
import coinffeine.model.currency.BitcoinAmount

trait CoinffeineWallet extends WalletProperties {

  def importPrivateKey(address: Address, key: KeyPair): Unit

  /** Transfer a given amount of BTC to an address if possible.
    *
    * @param amount   Amount to transfer
    * @param address  Destination address
    * @return         TX id if transfer is possible, TransferException otherwise
    */
  def transfer(amount: BitcoinAmount, address: Address): Future[Hash]
}

object CoinffeineWallet {

  case class TransferException(amount: BitcoinAmount, address: Address, cause: Throwable)
    extends Exception(s"Cannot transfer $amount to $address", cause)
}
