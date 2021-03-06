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
import models.renewal.{SendTheLargestAmountsOfMoney, Renewal}
import org.jsoup.Jsoup
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.{RenewalService, StatusService}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.Future

class SendTheLargestAmountsOfMoneyControllerSpec extends GenericTestHelper with MockitoSugar with PatienceConfiguration with IntegrationPatience {

  trait Fixture extends AuthorisedFixture {
    self =>
    val request = addToken(authRequest)

    val mockCacheMap = mock[CacheMap]

    val emptyCache = CacheMap("", Map.empty)

    lazy val mockDataCacheConnector = mock[DataCacheConnector]
    lazy val mockStatusService = mock[StatusService]
    lazy val mockRenewalService = mock[RenewalService]

    val controller = new SendTheLargestAmountsOfMoneyController(
      dataCacheConnector = mockDataCacheConnector,
      authConnector = self.authConnector,
      renewalService = mockRenewalService
    )
  }

  val emptyCache = CacheMap("", Map.empty)

  "SendTheLargestAmountsOfMoneyController" when {

    "get is called" must {
      "load the 'Where to Send The Largest Amounts Of Money' page" in new Fixture {

        when(controller.dataCacheConnector.fetch[Renewal](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        val result = controller.get()(request)
        status(result) must be(OK)

        val document = Jsoup.parse(contentAsString(result))
        document.title() must be(Messages("renewal.msb.largest.amounts.title") + " - " + Messages("summary.renewal") + " - " + Messages("title.amls") + " - " + Messages("title.gov"))
      }

      "pre-populate the 'Where to Send The Largest Amounts Of Money' Page" in new Fixture {

        when(controller.dataCacheConnector.fetch[Renewal](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(
            Renewal(sendTheLargestAmountsOfMoney = Some(SendTheLargestAmountsOfMoney(Country("United Kingdom", "GB"))))
          )))

        val result = controller.get()(request)
        status(result) must be(OK)

        val document = Jsoup.parse(contentAsString(result))
        document.select("select[name=country_1] > option[value=GB]").hasAttr("selected") must be(true)

      }
    }

    "post is called" when {
      "edit is false" must {
        "redirect to the MostTransactionsController with valid data" in new Fixture {

          val newRequest = request.withFormUrlEncodedBody(
            "country_1" -> "GS"
          )

          when(mockRenewalService.getRenewal(any(), any(), any()))
            .thenReturn(Future.successful(None))

          when(mockRenewalService.updateRenewal(any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))

          val result = controller.post()(newRequest)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.MostTransactionsController.get().url))
        }
      }

      "edit is true" must {
        "redirect to the SummaryController" in new Fixture {

          val newRequest = request.withFormUrlEncodedBody(
            "country_1" -> "GB"
          )

          when(mockRenewalService.getRenewal(any(), any(), any()))
            .thenReturn(Future.successful(None))

          when(mockRenewalService.updateRenewal(any())(any(), any(), any()))
            .thenReturn(Future.successful(emptyCache))

          val result = controller.post(true)(newRequest)
          status(result) must be(SEE_OTHER)
          redirectLocation(result) must be(Some(routes.SummaryController.get().url))
        }
      }

      "given invalid data, must respond with BAD_REQUEST" in new Fixture {

        val newRequest = request.withFormUrlEncodedBody(
          "country_1" -> ""
        )

        val result = controller.post()(newRequest)
        status(result) must be(BAD_REQUEST)

        val document = Jsoup.parse(contentAsString(result))
        document.select("a[href=#country_1]").html() must include(Messages("error.required.renewal.country.name"))
      }
    }
  }
}
