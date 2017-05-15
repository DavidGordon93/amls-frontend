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

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{Form2, InvalidForm, ValidForm}
import models.responsiblepeople.TimeAtAddress.{OneToThreeYears, ThreeYearsPlus}
import models.responsiblepeople._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import utils.{ControllerHelper, RepeatingSection}
import views.html.responsiblepeople.additional_address

import scala.concurrent.Future

trait AdditionalAddressController extends RepeatingSection with BaseController {

  def dataCacheConnector: DataCacheConnector

  final val DefaultAddressHistory = ResponsiblePersonAddress(PersonAddressUK("", "", None, None, ""), None)

  def get(index: Int, edit: Boolean = false, fromDeclaration: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      getData[ResponsiblePeople](index) map {
        case Some(ResponsiblePeople(Some(personName),_,_,Some(ResponsiblePersonAddressHistory(_,Some(additionalAddress),_)),_,_,_,_,_,_,_,_,_,_,_)) =>
          Ok(additional_address(Form2[ResponsiblePersonAddress](additionalAddress), edit, index, fromDeclaration, personName.titleName))
        case Some(ResponsiblePeople(Some(personName),_,_,_,_,_,_,_,_,_,_,_,_,_,_)) =>
          Ok(additional_address(Form2(DefaultAddressHistory), edit, index, fromDeclaration, personName.titleName))
        case _ => NotFound(notFoundView)
      }
  }


  def post(index: Int, edit: Boolean = false, fromDeclaration: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
      (Form2[ResponsiblePersonAddress](request.body) match {
        case f: InvalidForm =>
          getData[ResponsiblePeople](index) map { rp =>
            BadRequest(additional_address(f, edit, index, fromDeclaration, ControllerHelper.rpTitleName(rp)))
          }
        case ValidForm(_, data) => {
          getData[ResponsiblePeople](index) flatMap { responsiblePerson =>
            (for {
              rp <- responsiblePerson
              addressHistory <- rp.addressHistory
              additionalAddress <- addressHistory.additionalAddress
            } yield {
              val additionalAddressWithTime = data.copy(timeAtAddress = additionalAddress.timeAtAddress)
              updateAndRedirect(additionalAddressWithTime, index, edit, fromDeclaration)
            }) getOrElse updateAndRedirect(data, index, edit, fromDeclaration)
          }
        }
      }).recoverWith {
        case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
      }
    }
  }

  private def updateAndRedirect
  (data: ResponsiblePersonAddress, index: Int, edit: Boolean, fromDeclaration: Boolean)
  (implicit authContext: AuthContext, request: Request[AnyContent]) = {
    updateDataStrict[ResponsiblePeople](index) { res =>
      res.addressHistory(
        res.addressHistory match {
          case Some(a) if data.timeAtAddress.contains(ThreeYearsPlus) | data.timeAtAddress.contains(OneToThreeYears) =>
            a.additionalAddress(data).removeAdditionalExtraAddress
          case Some(a) => a.additionalAddress(data)
          case _ => ResponsiblePersonAddressHistory(additionalAddress = Some(data))
        })
    } map { _ =>
      data.timeAtAddress match {
        case Some(_) if edit =>  Redirect(routes.DetailedAnswersController.get(index))
        case _ => Redirect(routes.TimeAtAdditionalAddressController.get(index, edit, fromDeclaration))
      }
    }
  }
}

object AdditionalAddressController extends AdditionalAddressController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector: DataCacheConnector = DataCacheConnector
}