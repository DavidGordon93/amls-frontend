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

import javax.inject.{Inject, Singleton}

import connectors.DataCacheConnector
import controllers.BaseController
import forms.{ValidForm, InvalidForm, EmptyForm, Form2}
import models.businessmatching._
import models.renewal.{Renewal, InvolvedInOther, InvolvedInOtherYes, InvolvedInOtherNo}
import play.api.i18n.Messages
import services.{RenewalService, StatusService}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.renewal.involved_in_other

@Singleton
class InvolvedInOtherController @Inject()(
                                           val dataCacheConnector: DataCacheConnector,
                                           val authConnector: AuthConnector,
                                           val renewalService: RenewalService
                                         ) extends BaseController {

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
        dataCacheConnector.fetchAll map {
          optionalCache =>
            (for {
              cache <- optionalCache
              businessMatching <- cache.getEntry[BusinessMatching](BusinessMatching.key)
            } yield {
              (for {
                renewal <- cache.getEntry[Renewal](Renewal.key)
                involvedInOther <- renewal.involvedInOtherActivities
              } yield {
                Ok(involved_in_other(Form2[InvolvedInOther](involvedInOther),
                  edit, businessTypes(businessMatching)))
              }) getOrElse Ok(involved_in_other(EmptyForm, edit, businessTypes(businessMatching)))
            }) getOrElse Ok(involved_in_other(EmptyForm, edit, None))
        }
  }

  private def businessTypes(activities: BusinessMatching): Option[String] = {
    val typesString = activities.activities map { a =>
      a.businessActivities.map {
        case AccountancyServices => Messages("businessmatching.registerservices.servicename.lbl.01")
        case BillPaymentServices => Messages("businessmatching.registerservices.servicename.lbl.02")
        case EstateAgentBusinessService => Messages("businessmatching.registerservices.servicename.lbl.03")
        case HighValueDealing => Messages("businessmatching.registerservices.servicename.lbl.04")
        case MoneyServiceBusiness => Messages("businessmatching.registerservices.servicename.lbl.05")
        case TrustAndCompanyServices => Messages("businessmatching.registerservices.servicename.lbl.06")
        case TelephonePaymentService => Messages("businessmatching.registerservices.servicename.lbl.07")
      }

    }

    typesString match {
      case Some(_) => Some(typesString.getOrElse(Set()).mkString(", ") + ".")
      case None => None
    }

  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request => {
        Form2[InvolvedInOther](request.body) match {
          case f: InvalidForm =>
            for {
              businessMatching <- dataCacheConnector.fetch[BusinessMatching](BusinessMatching.key)
            } yield businessMatching match {
              case Some(_) => BadRequest(involved_in_other(f, edit, businessTypes(businessMatching)))
              case None => BadRequest(involved_in_other(f, edit, None))
            }
          case ValidForm(_, data) =>
            for {
              renewal <- renewalService.getRenewal
              _ <- renewalService.updateRenewal(getUpdatedRenewal(renewal, data))
            } yield data match {
              case models.renewal.InvolvedInOtherYes(_) => Redirect(routes.BusinessTurnoverController.get(edit))
              case models.renewal.InvolvedInOtherNo => redirectDependingOnEdit(edit)
            }
        }
      }
  }

  private def getUpdatedRenewal(renewal: Option[Renewal], data: InvolvedInOther): Renewal = {
    (renewal, data) match {
      case (Some(renew), InvolvedInOtherYes(_)) => {
        renewal.involvedInOtherActivities(data)
      }
      case (Some(renew), InvolvedInOtherNo) => {
        renewal.involvedInOtherActivities(data).resetBusinessTurnover
      }
      case (None, _) => {
        Renewal(involvedInOtherActivities = Some(data), hasChanged = true)
      }
    }
  }

  private def redirectDependingOnEdit(edit: Boolean) = edit match {
    case true => Redirect(routes.SummaryController.get())
    case false => Redirect(routes.AMLSTurnoverController.get(edit))
  }

}




