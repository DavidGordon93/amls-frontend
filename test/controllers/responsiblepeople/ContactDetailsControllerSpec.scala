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

package controllers.responsiblepeople

import connectors.DataCacheConnector
import models.responsiblepeople.{ContactDetails, PersonName, ResponsiblePeople}
import org.jsoup.Jsoup
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{AuthorisedFixture, GenericTestHelper}

import scala.concurrent.Future

class ContactDetailsControllerSpec extends GenericTestHelper with MockitoSugar with ScalaFutures {

  trait Fixture extends AuthorisedFixture {
    self =>
    val request = addToken(authRequest)

    val controller = new ContactDetailsController {
      override val dataCacheConnector = mock[DataCacheConnector]
      override val authConnector = self.authConnector
    }
  }

  val emptyCache = CacheMap("", Map.empty)

  "ContactDetailsController" when {

    val personName = Some(PersonName("firstname", None, "lastname", None, None))

    "get is called" must {
      "display the contact details page" in new Fixture {
        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(Seq(ResponsiblePeople(personName)))))

        val result = controller.get(1)(request)
        status(result) must be(OK)

        val document = Jsoup.parse(contentAsString(result))
        document.select("input[name=phoneNumber]").`val` must be("")
        document.select("input[name=emailAddress]").`val` must be("")

      }

      "display the contact details page with pre populated data" in new Fixture {

        val contact = ContactDetails("07702745869", "test@test.com")
        val res = ResponsiblePeople(personName = personName, contactDetails = Some(contact))

        when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
          .thenReturn(Future.successful(Some(Seq(res))))

        val result = controller.get(1)(request)
        status(result) must be(OK)

        val document = Jsoup.parse(contentAsString(result))
        document.select("input[name=phoneNumber]").`val` must be(contact.phoneNumber)
        document.select("input[name=emailAddress]").`val` must be(contact.emailAddress)
      }

      "respond with NOT_FOUND" when {
        "there is no responsible person for the index" in new Fixture {
          val contact = ContactDetails("07000000000", "test@test.com")
          val res = ResponsiblePeople(personName = personName, contactDetails = Some(contact))

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
            .thenReturn(Future.successful(Some(Seq(res))))

          val result = controller.get(0)(request)
          status(result) must be(NOT_FOUND)
        }
      }
    }

    "post is called" when {
      "given a valid form" when {
        "index of the responsible person is 1" must {
          "go to ConfirmAddressController" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "phoneNumber" -> "07000000000",
              "emailAddress" -> "test@test.com"
            )

            when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
              .thenReturn(Future.successful(Some(Seq(ResponsiblePeople()))))

            when(controller.dataCacheConnector.save[ContactDetails](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post(1)(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.ConfirmAddressController.get(1).url))
          }
        }
        "index of the responsible person is greater than 1" must {
          "go to CurrentAddressController" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "phoneNumber" -> "07000000000",
              "emailAddress" -> "test@test.com"
            )

            when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
              .thenReturn(Future.successful(Some(Seq(ResponsiblePeople(), ResponsiblePeople()))))

            when(controller.dataCacheConnector.save[ContactDetails](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post(2)(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.CurrentAddressController.get(2).url))
          }
        }
        "there is no responsible person for the index" must {
          "respond with NOT_FOUND" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "phoneNumber" -> "07000000000",
              "emailAddress" -> "test@test.com"
            )

            when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())(any(), any(), any()))
              .thenReturn(Future.successful(Some(Seq(ResponsiblePeople(), ResponsiblePeople()))))

            when(controller.dataCacheConnector.save[ContactDetails](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(emptyCache))

            val result = controller.post(0)(newRequest)
            status(result) must be(NOT_FOUND)
          }
        }
        "in edit mode" must {
          "go to DetailedAnswersController" in new Fixture {

            val newRequest = request.withFormUrlEncodedBody(
              "phoneNumber" -> "07000000000",
              "emailAddress" -> "test@test.com"
            )

            when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
              (any(), any(), any())).thenReturn(Future.successful(Some(Seq(ResponsiblePeople()))))

            when(controller.dataCacheConnector.save[ContactDetails](any(), any())
              (any(), any(), any())).thenReturn(Future.successful(emptyCache))

            val result = controller.post(1, true)(newRequest)
            status(result) must be(SEE_OTHER)
            redirectLocation(result) must be(Some(controllers.responsiblepeople.routes.DetailedAnswersController.get(1).url))
          }
        }
      }

      "given an invalid form" must {
        "respond with BAD_REQUEST" in new Fixture {

          val newRequest = request.withFormUrlEncodedBody(
            "phoneNumber" -> "<070>00000000",
            "emailAddress" -> "test@test.com"
          )

          when(controller.dataCacheConnector.fetch[Seq[ResponsiblePeople]](any())
            (any(), any(), any())).thenReturn(Future.successful(Some(Seq(ResponsiblePeople(personName)))))

          when(controller.dataCacheConnector.save[ContactDetails](any(), any())
            (any(), any(), any())).thenReturn(Future.successful(emptyCache))

          val result = controller.post(1)(newRequest)
          status(result) must be(BAD_REQUEST)

        }
      }
    }

  }
}


