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

import cats.implicits._
import models.renewal.{TransactionsInLast12Months, Renewal}
import org.jsoup.Jsoup
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import services.RenewalService
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.Future

class TransactionsInLast12MonthsControllerSpec extends GenericTestHelper with MockitoSugar {

  trait Fixture extends AuthorisedFixture {
    self =>
    val renewalService = mock[RenewalService]
    val request = addToken(authRequest)

    lazy val controller = new TransactionsInLast12MonthsController(self.authConnector, renewalService)

    when {
      renewalService.getRenewal(any(), any(), any())
    } thenReturn Future.successful(Renewal().some)
  }

  trait FormSubmissionFixture extends Fixture {
    val validFormData = "txnAmount" -> "1500"
    val validFormRequest = request.withFormUrlEncodedBody(validFormData)

    when {
      renewalService.updateRenewal(any())(any(), any(), any())
    } thenReturn Future.successful(mock[CacheMap])
  }

  "Calling the GET action" must {
    "return the correct view" when {
      "edit is false" in new Fixture {
        val result = controller.get()(request)

        status(result) mustBe OK

        val doc = Jsoup.parse(contentAsString(result))
        doc.select(".heading-xlarge").text mustBe Messages("renewal.msb.transfers.header")
      }

      "edit is true" in new Fixture {
        val result = controller.get(true)(request)

        status(result) mustBe OK

        val doc = Jsoup.parse(contentAsString(result))
        doc.select("form").first.attr("action") mustBe routes.TransactionsInLast12MonthsController.post(true).url
      }

      "reads the current value from the renewals model" in new Fixture {

        when {
          renewalService.getRenewal(any(), any(), any())
        } thenReturn Future.successful(Renewal(transactionsInLast12Months = TransactionsInLast12Months("2500").some).some)

        val result = controller.get(true)(request)
        val doc = Jsoup.parse(contentAsString(result))

        doc.select("input[name=txnAmount]").first.attr("value") mustBe "2500"

        verify(renewalService).getRenewal(any(), any(), any())
      }
    }
  }

  "Calling the POST action" when {
    "posting valid data" must {
      "redirect to the next page in the flow" in new FormSubmissionFixture {
        val result = controller.post()(validFormRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe routes.SendTheLargestAmountsOfMoneyController.get().url.some
      }

      "redirect to the summary page when edit = true" in new FormSubmissionFixture {
        val result = controller.post(edit = true)(validFormRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe controllers.renewal.routes.SummaryController.get().url.some
      }

      "return a bad request" when {
        "the form fails validation" in new FormSubmissionFixture {
          val result = controller.post()(request)

          status(result) mustBe BAD_REQUEST
          verify(renewalService, never()).updateRenewal(any())(any(), any(), any())
        }
      }

      "save the model data into the renewal object" in new FormSubmissionFixture {
        val result = await(controller.post()(validFormRequest))
        val captor = ArgumentCaptor.forClass(classOf[Renewal])

        verify(renewalService).updateRenewal(captor.capture())(any(), any(), any())

        captor.getValue.transactionsInLast12Months mustBe TransactionsInLast12Months("1500").some
      }
    }
  }
}
