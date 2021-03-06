/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.moneyservicebusiness

import models.Country
import models.businessmatching._
import models.registrationprogress.{Started, Completed, NotStarted, Section}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import typeclasses.MongoKey
import uk.gov.hmrc.http.cache.client.CacheMap
import org.mockito.Mockito._
import play.api.mvc.Call

class MoneyServiceBusinessSpec extends PlaySpec with MockitoSugar with MoneyServiceBusinessTestData with OneAppPerSuite{

  "MoneyServiceBusiness" should {

    "have an implicit conversion from Option which" when {

      "called with None" should {

        "return a default version of MoneyServiceBusiness" in {

          val res: MoneyServiceBusiness = None
          res must be(emptyModel)
        }
      }

      "called with a concrete value" should {
        "return the value passed in extracted from the option" in {
          val res: MoneyServiceBusiness = Some(completeModel)
          res must be(completeModel)
        }
      }
    }

    "have a section function that" when {
      implicit val cacheMap = mock[CacheMap]

      "model is empty" should {
        "return a NotStarted Section" in {
          when(cacheMap.getEntry[MoneyServiceBusiness](MoneyServiceBusiness.key)) thenReturn None
          when(cacheMap.getEntry[MoneyServiceBusiness](BusinessMatching.key)) thenReturn None
          MoneyServiceBusiness.section must be(Section(MoneyServiceBusiness.key, NotStarted, false,  controllers.msb.routes.WhatYouNeedController.get()))
        }
      }

      "model is incomplete" should {
        "return a NotStarted Section" in {
          when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key)) thenReturn Some(BusinessMatching(msbServices = Some(MsbServices(
            Set(ChequeCashingScrapMetal)))))
          when(cacheMap.getEntry[MoneyServiceBusiness](MoneyServiceBusiness.key)) thenReturn
            Some(MoneyServiceBusiness( throughput = Some(ExpectedThroughput.Second)))
          MoneyServiceBusiness.section must be(Section(MoneyServiceBusiness.key, Started, false,  controllers.msb.routes.WhatYouNeedController.get()))
        }
      }

      "model is complete" should {
        "return a Completed Section" in {
          when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key)) thenReturn Some(BusinessMatching(msbServices = Some(MsbServices(
            Set(ChequeCashingScrapMetal)))))
          when(cacheMap.getEntry[MoneyServiceBusiness](MoneyServiceBusiness.key)) thenReturn Some(completeModel)
          MoneyServiceBusiness.section must be(Section(MoneyServiceBusiness.key, Completed, false,  controllers.msb.routes.SummaryController.get()))
        }
      }

      "model is complete" should {
        "return a Completed Section when all msb options selected in business matching" in {
          when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key)) thenReturn Some(BusinessMatching(msbServices = Some(MsbServices(
            Set(ChequeCashingScrapMetal, TransmittingMoney, CurrencyExchange, ChequeCashingNotScrapMetal)))))
          when(cacheMap.getEntry[MoneyServiceBusiness](MoneyServiceBusiness.key)) thenReturn Some(completeModel)
          MoneyServiceBusiness.section must be(Section(MoneyServiceBusiness.key, Completed, false,  controllers.msb.routes.SummaryController.get()))
        }
      }
    }

    "have an isComplete function that" must {

      "correctly show if the model is complete" in {
        completeModel.isComplete(true, true) must be(true)
      }

      "correctly show if the model is incomplete" in {
        emptyModel.isComplete(false, false) must be(false)
      }
    }

    "Serialise to expected Json" when {
      "model is complete" in {
        Json.toJson(completeModel) must be(completeJson)
      }
    }

    "Deserialise from Json as expected" when {
      "model is complete" in {
        completeJson.as[MoneyServiceBusiness] must be(completeModel)
      }
    }
  }
}

trait MoneyServiceBusinessTestData {

  private val businessUseAnIPSP = BusinessUseAnIPSPYes("name", "123456789123456")
  private val sendTheLargestAmountsOfMoney = SendTheLargestAmountsOfMoney(Country("United Kingdom", "GB"))

  val completeModel = MoneyServiceBusiness(
    throughput = Some(ExpectedThroughput.Second),
    businessUseAnIPSP = Some(businessUseAnIPSP),
    identifyLinkedTransactions = Some(IdentifyLinkedTransactions(true)),
    Some(WhichCurrencies(
      Seq("USD", "GBP", "EUR"),
      usesForeignCurrencies = Some(true),
      Some(BankMoneySource("bank names")),
      Some(WholesalerMoneySource("Wholesaler Names")),
      Some(true))),
    sendMoneyToOtherCountry = Some(SendMoneyToOtherCountry(true)),
    fundsTransfer = Some(FundsTransfer(true)),
    branchesOrAgents = Some(BranchesOrAgents(Some(Seq(Country("United Kingdom", "GB"))))),
    sendTheLargestAmountsOfMoney = Some(sendTheLargestAmountsOfMoney),
    mostTransactions = Some(MostTransactions(Seq(Country("United Kingdom", "GB")))),
    transactionsInNext12Months = Some(TransactionsInNext12Months("12345678963")),
    ceTransactionsInNext12Months = Some(CETransactionsInNext12Months("12345678963"))
  )

  val emptyModel = MoneyServiceBusiness(None)


  val completeJson = Json.obj(
    "throughput" -> Json.obj("throughput" -> "02"),
    "businessUseAnIPSP" -> Json.obj(
      "useAnIPSP" -> true,
      "name" -> "name",
      "referenceNumber" -> "123456789123456"
    ),
    "identifyLinkedTransactions" -> Json.obj("linkedTxn" -> true),
    "whichCurrencies" -> Json.obj(
      "currencies" -> Json.arr("USD", "GBP", "EUR"),
      "bankMoneySource" -> "Yes",
      "bankNames" -> "bank names",
      "wholesalerMoneySource" -> "Yes",
      "wholesalerNames" -> "Wholesaler Names",
      "customerMoneySource" -> "Yes",
      "usesForeignCurrencies" -> true
    ),
    "sendMoneyToOtherCountry" -> Json.obj("money" -> true),
    "fundsTransfer" -> Json.obj("transferWithoutFormalSystems" -> true),
    "branchesOrAgents" -> Json.obj("hasCountries" -> true,"countries" ->Json.arr("GB")),
    "transactionsInNext12Months" -> Json.obj("txnAmount" -> "12345678963"),
    "fundsTransfer" -> Json.obj("transferWithoutFormalSystems" -> true),
    "mostTransactions" -> Json.obj("mostTransactionsCountries" -> Seq("GB")),
    "sendTheLargestAmountsOfMoney" -> Json.obj("country_1" ->"GB"),
    "ceTransactionsInNext12Months" -> Json.obj("ceTransaction" -> "12345678963"),
    "hasChanged" -> false
  )

  val emptyJson = Json.obj("msbServices" -> Json.arr())
}
