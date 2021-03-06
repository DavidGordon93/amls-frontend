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

package controllers.supervision

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.supervision.{ProfessionalBodyMember, Supervision}
import views.html.supervision.member_of_professional_body

import scala.concurrent.Future

trait ProfessionalBodyMemberController extends BaseController {
  val dataCacheConnector: DataCacheConnector

  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetch[Supervision](Supervision.key) map {
        response =>
          val form: Form2[ProfessionalBodyMember] = (for {
            supervision <- response
            members <- supervision.professionalBodyMember
          } yield Form2[ProfessionalBodyMember](members)).getOrElse(EmptyForm)
          Ok(member_of_professional_body(form, edit))
      }
  }

  def post(edit : Boolean = false) = Authorised.async {
    implicit authContext => implicit request =>
      Form2[ProfessionalBodyMember](request.body) match {
        case f: InvalidForm =>
          Future.successful(BadRequest(member_of_professional_body(f, edit)))
        case ValidForm(_, data) => {
          for {
            supervision <-
            dataCacheConnector.fetch[Supervision](Supervision.key)
            _ <- dataCacheConnector.save[Supervision](Supervision.key,
              supervision.professionalBodyMember(data)
            )
          } yield edit match {
            case true => Redirect(routes.SummaryController.get())
            case false => Redirect(routes.PenalisedByProfessionalController.get())
          }
        }
      }
  }
}

object ProfessionalBodyMemberController extends ProfessionalBodyMemberController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
}
