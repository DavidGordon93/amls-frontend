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

package controllers.estateagentbusiness

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.estateagentbusiness.{EstateAgentBusiness, Residential, Services}
import models.status.{ReadyForRenewal, SubmissionDecisionApproved}
import services.StatusService
import utils.DateOfChangeHelper
import views.html.estateagentbusiness._

import scala.concurrent.Future

trait BusinessServicesController extends BaseController with DateOfChangeHelper {

  val dataCacheConnector: DataCacheConnector
  val statusService: StatusService

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetch[EstateAgentBusiness](EstateAgentBusiness.key) map {
        response =>
          val form = (for {
            estateAgentBusiness <- response
            accountant <- estateAgentBusiness.services
          } yield Form2[Services](accountant)).getOrElse(EmptyForm)
          Ok(business_servicess(form, edit))
      }
  }

  private def redirectToNextPage(edit: Boolean, data: Services) = {
    if(data.services.contains(Residential)) {
      Redirect(routes.ResidentialRedressSchemeController.get(edit))
    } else {
      if(edit) {
        Redirect(routes.SummaryController.get())
      } else  {
        Redirect(routes.PenalisedUnderEstateAgentsActController.get())
      }
    }
  }

  def updateData(business: EstateAgentBusiness, data: Services) : EstateAgentBusiness = {
    if(data.services.contains(Residential)) {
      business
    } else {
      business.copy(redressScheme = None)
    }
  }

  def post(edit: Boolean = false) = Authorised.async {
    import jto.validation.forms.Rules._
    implicit authContext => implicit request =>
      Form2[Services](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(business_servicess(f, edit)))
        case ValidForm(_, data) =>
          for {
            estateAgentBusiness <- dataCacheConnector.fetch[EstateAgentBusiness](EstateAgentBusiness.key)
            _ <- dataCacheConnector.save[EstateAgentBusiness](EstateAgentBusiness.key,
              updateData(estateAgentBusiness.services(data), data))
            status <- statusService.getStatus
          } yield {
            if (redirectToDateOfChange[Services](status, estateAgentBusiness.services, data)) {
              Redirect(routes.ServicesDateOfChangeController.get())
            } else {
              redirectToNextPage(edit, data)
            }
          }
      }
  }
}

object BusinessServicesController extends BusinessServicesController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
  override val statusService = StatusService
}
