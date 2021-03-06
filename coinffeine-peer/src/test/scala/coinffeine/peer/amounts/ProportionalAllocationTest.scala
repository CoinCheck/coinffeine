package coinffeine.peer.amounts

import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks

import coinffeine.common.test.UnitTest

class ProportionalAllocationTest extends UnitTest with PropertyChecks {

  val positives = Gen.posNum[Int].map(BigInt.apply)
  val vectorOfPositives = Gen.nonEmptyContainerOf[Vector, BigInt](positives)

  "A proportional distribution" should "have the same amounts as weights" in {
    forAll (positives, vectorOfPositives) { (amount, weights) =>
      ProportionalAllocation.allocate(amount, weights) should have length weights.length
    }
  }

  it should "fully distribute the input amount" in {
    forAll (positives, vectorOfPositives) { (amount, weights) =>
      ProportionalAllocation.allocate(amount, weights).sum shouldBe amount
    }
  }

  it should "have amounts proportional to the corresponding weight" in {
    forAll (positives, vectorOfPositives) { (amount, weights) =>
      val amounts = ProportionalAllocation.allocate(amount, weights)
      val totalWeight = BigDecimal(weights.sum)
      val proportions = weights.map(w => BigDecimal(w) / totalWeight)
      for ((elem, proportion) <- amounts.zip(proportions)) {
        val error = (proportion * BigDecimal(amount) - BigDecimal(elem)).abs
        error should be < BigDecimal(1)
      }
    }
  }

  it should "be fair" in {
    forAll (positives, vectorOfPositives) { (amount, weights) =>
      shouldBeAFairDistribution(weights, ProportionalAllocation.allocate(amount, weights))
    }
  }

  def shouldBeAFairDistribution(weights: Vector[BigInt], amounts: Vector[BigInt]): Unit = {
    for {
      i <- 0 to (weights.size - 1)
      j <- 0 to (weights.size - 1)
      if weights(i) > weights(j)
    } withClue(s"Comparing $i and $j of $amounts generated from weights $weights") {
      amounts(i) should be >= amounts(j)
    }
  }
}
