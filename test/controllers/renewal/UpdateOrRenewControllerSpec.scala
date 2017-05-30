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
import models.ReadStatusResponse
import models.businessmatching.{BusinessActivities => BMActivities, _}
import models.renewal.{InvolvedInOtherYes, Renewal}
import models.status.ReadyForRenewal
import org.joda.time.{LocalDate, LocalDateTime}
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.PrivateMethodTester
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import services.{ProgressService, RenewalService, StatusService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.Future

class UpdateOrRenewControllerSpec extends GenericTestHelper with MockitoSugar with ScalaFutures with PrivateMethodTester {

  trait Fixture extends AuthorisedFixture {
    self =>
    val request = addToken(authRequest)

    val mockCacheMap = mock[CacheMap]
    val dataCacheConnector = mock[DataCacheConnector]

    val emptyCache = CacheMap("", Map.empty)

    val statusService = mock[StatusService]
    val renewalService = mock[RenewalService]
    lazy val app = new GuiceApplicationBuilder()
      .disable[com.kenshoo.play.metrics.PlayModule]
      .overrides(bind[DataCacheConnector].to(dataCacheConnector))
      .overrides(bind[AuthConnector].to(self.authConnector))
      .overrides(bind[StatusService].to(statusService))
      .overrides(bind[RenewalService].to(renewalService))
      .build()

    val controller = app.injector.instanceOf[UpdateOrRenewController]
    val renewalDate = LocalDate.now().plusDays(15)

    val readStatusResponse = ReadStatusResponse(LocalDateTime.now(), "Approved", None, None, None, Some(renewalDate), false)

  }

  "UpdateOrRenewController" must {

    "when get is called" must {
      "load the page" in new Fixture {

        when(statusService.getDetailedStatus(any(), any(), any()))
          .thenReturn(Future.successful((ReadyForRenewal(Some(renewalDate)), Some(readStatusResponse))))

        val result = controller.get()(request)

        status(result) mustBe OK

        val html = Jsoup.parse(contentAsString(result))

        html.select(".page-header").text() must include(Messages("renewal.updateorrenew.title"))
      }

    }

    "when post is called" must {
      "redirect to the renewal progress page when renew is selected" in new Fixture {
        val newRequest = request.withFormUrlEncodedBody(
          "updateorrenew" -> "01"
        )

        when(controller.dataCacheConnector.fetch[Renewal](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        when(renewalService.updateRenewal(any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyCache))

        val result = controller.post()(newRequest)


        redirectLocation(result) mustBe Some(controllers.renewal.routes.RenewalProgressController.get().url)
      }

      "redirect to the registration progress page when update is selected" in new Fixture {
        val newRequest = request.withFormUrlEncodedBody(
          "updateorrenew" -> "02"
        )

        when(controller.dataCacheConnector.fetch[Renewal](any())(any(), any(), any()))
          .thenReturn(Future.successful(None))

        when(renewalService.updateRenewal(any())(any(), any(), any()))
          .thenReturn(Future.successful(emptyCache))

        val result = controller.post()(newRequest)


        redirectLocation(result) mustBe Some(controllers.routes.RegistrationProgressController.get().url)
      }

    }
  }
}
