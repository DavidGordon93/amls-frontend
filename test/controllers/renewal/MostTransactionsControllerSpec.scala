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

package controllers.renewal

import connectors.DataCacheConnector
import models.Country
import models.businessmatching.{BusinessMatching, ChequeCashingScrapMetal, CurrencyExchange, MsbServices}
import models.renewal.{CETransactionsInLast12Months, MostTransactions, Renewal}
import org.jsoup.Jsoup
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.Future

class MostTransactionsControllerSpec extends GenericTestHelper with MockitoSugar {

  trait Fixture extends AuthorisedFixture {
    self =>
    val request = addToken(authRequest)

    val cache: DataCacheConnector = mock[DataCacheConnector]
    val cacheMap = mock[CacheMap]
    val controller = new MostTransactionsController(self.authConnector, self.cache)
  }

  "MostTransactionsController" must {

    "show an empty form on get with no data in store" in new Fixture {

      when(cache.fetch[Renewal](eqTo(Renewal.key))(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val result = controller.get()(request)
      val document = Jsoup.parse(contentAsString(result))

      status(result) mustEqual OK

      document.select("select").size mustEqual 3
      document.select("option[selected]").size mustEqual 0
      document.select(".amls-error-summary").size mustEqual 0
    }

    "show a prefilled form when there is data in the store" in new Fixture {

      val model = Renewal(
        mostTransactions = Some(
          MostTransactions(
            models.countries.take(3)
          )
        )
      )

      when(cache.fetch[Renewal](eqTo(Renewal.key))(any(), any(), any()))
        .thenReturn(Future.successful(Some(model)))

      val result = controller.get()(request)
      val document = Jsoup.parse(contentAsString(result))

      status(result) mustEqual OK

      document.select("select").size mustEqual 3
      document.select("option[selected]").size mustEqual 3
      document.select(".amls-error-summary").size mustEqual 0
    }

    "return a Bad request with errors on invalid submission" in new Fixture {

      val result = controller.post()(request)
      val document = Jsoup.parse(contentAsString(result))

      status(result) mustEqual BAD_REQUEST

      document.select("select").size mustEqual 3
      document.select("option[selected]").size mustEqual 0
      document.select(".amls-error-summary").size mustEqual 1
    }

    "on valid submission (no edit) (CE)" in new Fixture {

      val msbServices = Some(
        MsbServices(
          Set(
            CurrencyExchange
          )
        )
      )
      val incomingModel = Renewal()

      val outgoingModel = incomingModel.copy(
        mostTransactions = Some(
          MostTransactions(
            Seq(Country("United Kingdom", "GB"))
          )
        ), hasChanged = true
      )

      val newRequest = request.withFormUrlEncodedBody(
        "mostTransactionsCountries[]" -> "GB"
      )

      when(cache.fetchAll(any(), any()))
        .thenReturn(Future.successful(Some(cacheMap)))

      when(cacheMap.getEntry[Renewal](eqTo(Renewal.key))(any()))
        .thenReturn(Some(incomingModel))

      when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
        .thenReturn(Some(BusinessMatching(msbServices = msbServices)))

      when(cache.save[Renewal](eqTo(Renewal.key), eqTo(outgoingModel))(any(), any(), any()))
        .thenReturn(Future.successful(new CacheMap("", Map.empty)))

      val result = controller.post(edit = false)(newRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.CETransactionsInLast12MonthsController.get().url)
    }

    "on valid submission (no edit) (non-CE)" in new Fixture {

      val incomingModel = Renewal()

      val outgoingModel = incomingModel.copy(
        mostTransactions = Some(
          MostTransactions(
            Seq(Country("United Kingdom", "GB"))
          )
        ), hasChanged = true
      )
      val msbServices = Some(
        MsbServices(
          Set(
            ChequeCashingScrapMetal
          )
        )
      )
      val newRequest = request.withFormUrlEncodedBody(
        "mostTransactionsCountries[]" -> "GB"
      )
      when(cache.fetchAll(any(), any()))
        .thenReturn(Future.successful(Some(cacheMap)))

      when(cacheMap.getEntry[Renewal](eqTo(Renewal.key))(any()))
        .thenReturn(Some(incomingModel))

      when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
        .thenReturn(Some(BusinessMatching(msbServices = msbServices)))

      when(cache.save[Renewal](eqTo(Renewal.key), eqTo(outgoingModel))(any(), any(), any()))
        .thenReturn(Future.successful(new CacheMap("", Map.empty)))

      val result = controller.post(edit = false)(newRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.SummaryController.get().url)
    }

    "return a redirect to the summary page on valid submission where the next page data exists (edit) (CE)" in new Fixture {

      val msbServices = Some(
        MsbServices(
          Set(
            CurrencyExchange
          )
        )
      )

      val incomingModel = Renewal(
        ceTransactionsInLast12Months = Some(CETransactionsInLast12Months(
          "1223131"
        ))
      )

      val outgoingModel = Renewal(
        ceTransactionsInLast12Months = Some(CETransactionsInLast12Months(
          "1223131"
        )),
        mostTransactions = Some(
          MostTransactions(
            Seq(Country("United Kingdom", "GB"))
          )
        ), hasChanged = true
      )

      val newRequest = request.withFormUrlEncodedBody(
        "mostTransactionsCountries[]" -> "GB"
      )

      when(cache.fetchAll(any(), any()))
        .thenReturn(Future.successful(Some(cacheMap)))

      when(cacheMap.getEntry[Renewal](eqTo(Renewal.key))(any()))
        .thenReturn(Some(incomingModel))

      when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
        .thenReturn(Some(BusinessMatching(msbServices = msbServices)))

      when(cache.save[Renewal](eqTo(Renewal.key), eqTo(outgoingModel))(any(), any(), any()))
        .thenReturn(Future.successful(new CacheMap("", Map.empty)))

      val result = controller.post(edit = true)(newRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.SummaryController.get().url)
    }

    "return a redirect on valid submission where in non edit mode" in new Fixture {
      val msbServices = Some(
        MsbServices(
          Set(
            CurrencyExchange
          )
        )
      )
      val incomingModel = Renewal()

      val outgoingModel = incomingModel.copy(
        mostTransactions = Some(
          MostTransactions(
            Seq(Country("United Kingdom", "GB"))
          )
        ), hasChanged = true
      )

      val newRequest = request.withFormUrlEncodedBody(
        "mostTransactionsCountries[0]" -> "GB"
      )

      when(cache.fetchAll(any(), any()))
        .thenReturn(Future.successful(Some(cacheMap)))
      when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
        .thenReturn(Some(BusinessMatching(msbServices = msbServices)))
      when(cacheMap.getEntry[Renewal](eqTo(Renewal.key))(any()))
        .thenReturn(Some(incomingModel))
      when(cache.save[Renewal](eqTo(Renewal.key), eqTo(outgoingModel))(any(), any(), any()))
        .thenReturn(Future.successful(new CacheMap("", Map.empty)))

      val result = controller.post(edit = false)(newRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.CETransactionsInLast12MonthsController.get().url)
    }

    "return a redirect to the summary page on valid submission (edit) (non-CE)" in new Fixture {

      val incomingModel = Renewal()

      val outgoingModel = incomingModel.copy(
        mostTransactions = Some(
          MostTransactions(
            Seq(Country("United Kingdom", "GB"))
          )
        ), hasChanged = true
      )

      val newRequest = request.withFormUrlEncodedBody(
        "mostTransactionsCountries[]" -> "GB"
      )
      val msbServices = Some(
        MsbServices(
          Set(
            ChequeCashingScrapMetal
          )
        )
      )
      when(cache.fetchAll(any(), any()))
        .thenReturn(Future.successful(Some(cacheMap)))
      when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
        .thenReturn(Some(BusinessMatching(msbServices = msbServices)))
      when(cacheMap.getEntry[Renewal](eqTo(Renewal.key))(any()))
        .thenReturn(Some(incomingModel))
      when(cache.save[Renewal](eqTo(Renewal.key), eqTo(outgoingModel))(any(), any(), any()))
        .thenReturn(Future.successful(new CacheMap("", Map.empty)))

      val result = controller.post(edit = true)(newRequest)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustEqual Some(routes.SummaryController.get().url)
    }
  }

  "throw exception when Msb services in Business Matching returns none" in new Fixture {

    val newRequest = request.withFormUrlEncodedBody(
      "mostTransactionsCountries[]" -> "GB"
    )

    val incomingModel = Renewal()

    when(cache.fetchAll(any(), any()))
      .thenReturn(Future.successful(Some(cacheMap)))

    when(cacheMap.getEntry[BusinessMatching](BusinessMatching.key))
      .thenReturn(None)

    when(cacheMap.getEntry[Renewal](eqTo(Renewal.key))(any()))
      .thenReturn(Some(incomingModel))

    when(cache.save[Renewal](eqTo(Renewal.key), any())
      (any(), any(), any())).thenReturn(Future.successful(new CacheMap("", Map.empty)))


    a[Exception] must be thrownBy {
      ScalaFutures.whenReady(controller.post(true)(newRequest)) { x => x }
    }
  }

}
