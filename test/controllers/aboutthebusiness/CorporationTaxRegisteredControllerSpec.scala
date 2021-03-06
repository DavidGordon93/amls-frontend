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

package controllers.aboutthebusiness

import connectors.{BusinessMatchingConnector, DataCacheConnector}
import models.aboutthebusiness.{AboutTheBusiness, CorporationTaxRegisteredYes}
import models.businessmatching.BusinessMatching
import org.jsoup.Jsoup
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import utils.GenericTestHelper
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.AuthorisedFixture

import scala.concurrent.Future

class CorporationTaxRegisteredControllerSpec extends GenericTestHelper with MockitoSugar with ScalaFutures {

  trait Fixture extends AuthorisedFixture {
    self =>
    val request = addToken(authRequest)

    val controller = new CorporationTaxRegisteredController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val authConnector = self.authConnector
      override val businessMatchingConnector = mock[BusinessMatchingConnector]
    }
  }

  val emptyCache = CacheMap("", Map.empty)

  "CorporationTaxRegisteredController" must {

    "on get display the registered for corporation tax page" in new Fixture {

      when(controller.businessMatchingConnector.getReviewDetails(any())) thenReturn Future.successful(None)

      when(controller.dataCacheConnector.fetch[AboutTheBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(None))

      val result = controller.get()(request)

      status(result) must be(OK)
      contentAsString(result) must include(Messages("aboutthebusiness.registeredforcorporationtax.title"))
    }

    "on get display the registered for corporation tax page with pre populated data" in new Fixture {

      val data = AboutTheBusiness(corporationTaxRegistered = Some(CorporationTaxRegisteredYes("1111111111")))

      when(controller.dataCacheConnector.fetch[AboutTheBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(data)))

      val result = controller.get()(request)
      status(result) must be(OK)
      val document = Jsoup.parse(contentAsString(result))
      document.getElementById("registeredForCorporationTax-true").hasAttr("checked") must be(true)
      document.getElementById("corporationTaxReference").`val` must be("1111111111")
    }

    "on get display an empty form when no previous entry" in new Fixture {

      val data = AboutTheBusiness(corporationTaxRegistered = None)

      when(controller.dataCacheConnector.fetch[AboutTheBusiness](eqTo(AboutTheBusiness.key))
        (any(), any(), any())).thenReturn(Future.successful(Some(data)))

      when(controller.dataCacheConnector.fetch[BusinessMatching](eqTo(BusinessMatching.key))
        (any(), any(), any())).thenReturn(Future.successful(None))

      val result = controller.get()(request)

      status(result) must be(OK)

      val document = Jsoup.parse(contentAsString(result))
      document.getElementById("registeredForCorporationTax-true").hasAttr("checked") must be(false)
      document.getElementById("corporationTaxReference").`val` must be("")
    }

    "on post with valid data and edit false continue to registered office page" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "registeredForCorporationTax" -> "true",
        "corporationTaxReference" -> "1111111111"
      )

      when(controller.dataCacheConnector.fetch[AboutTheBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(mock[AboutTheBusiness])))

      when(controller.dataCacheConnector.save[AboutTheBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post()(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.aboutthebusiness.routes.ConfirmRegisteredOfficeController.get().url))
    }

    "on post with valid data and edit true redirect to summary page" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "registeredForCorporationTax" -> "true",
        "corporationTaxReference" -> "1111111111"
      )

      when(controller.dataCacheConnector.fetch[AboutTheBusiness](any())
        (any(), any(), any())).thenReturn(Future.successful(Some(mock[AboutTheBusiness])))

      when(controller.dataCacheConnector.save[AboutTheBusiness](any(), any())
        (any(), any(), any())).thenReturn(Future.successful(emptyCache))

      val result = controller.post(true)(newRequest)
      status(result) must be(SEE_OTHER)
      redirectLocation(result) must be(Some(controllers.aboutthebusiness.routes.SummaryController.get().url))
    }

    "on post with no yes/no value selected show an error message" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "registeredForCorporationTax" -> ""
      )

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      val document = Jsoup.parse(contentAsString(result))
      document.select("a[href=#registeredForCorporationTax]").html() must include(Messages("error.required.atb.corporation.tax"))
    }

    "on post with yes with missing tax number show an error message" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "registeredForCorporationTax" -> "true",
        "corporationTaxReference" -> ""
      )

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      val document = Jsoup.parse(contentAsString(result))
      document.select("a[href=#corporationTaxReference]").html() must include(Messages("error.required.atb.corporation.tax.number"))
    }

    "on post with yes with invalid tax number show an error message" in new Fixture {

      val newRequest = request.withFormUrlEncodedBody(
        "registeredForCorporationTax" -> "true",
        "corporationTaxReference" -> "ABCDEF"
      )

      val result = controller.post()(newRequest)
      status(result) must be(BAD_REQUEST)
      val document = Jsoup.parse(contentAsString(result))
      document.select("a[href=#corporationTaxReference]").html() must include(Messages("error.invalid.atb.corporation.tax.number"))
    }
  }

}
