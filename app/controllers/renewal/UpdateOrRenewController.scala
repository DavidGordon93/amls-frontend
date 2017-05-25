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

import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.renewal.UpdateOrRenew
import models.renewal.UpdateOrRenew.{First, Second}
import models.status.ReadyForRenewal
import services.StatusService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import views.html.renewal._

import scala.concurrent.Future

@Singleton
class UpdateOrRenewController @Inject()(val authConnector: AuthConnector, val statusService :StatusService) extends BaseController {

  def get = Authorised.async {
    implicit authContext => implicit request =>
      for {
        statusInfo <- statusService.getDetailedStatus
      } yield {
        statusInfo match {
          case (ReadyForRenewal(renewalDate), _) => Ok(update_or_renew(EmptyForm, renewalDate))
          case _ => throw new Exception("Cannot get renewal date")
        }
      }
  }

  def post() = Authorised.async {
    implicit authContext =>
      implicit request =>
        Form2[UpdateOrRenew](request.body) match {
          case f: InvalidForm =>
            Future.successful(BadRequest(update_or_renew(f, None)))
          case ValidForm(_, data) =>
            data match {
              case First => Future.successful(Redirect(routes.RenewalProgressController.get()))
              case Second => Future.successful(Redirect(controllers.routes.RegistrationProgressController.get()))
              case _ => throw new Exception("Invalid selection")
            }
          }
        }
}
