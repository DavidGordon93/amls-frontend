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

package controllers.hvd

import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.hvd.{ExciseGoods, Hvd}
import models.status.{ReadyForRenewal, SubmissionDecisionApproved}
import services.StatusService
import utils.DateOfChangeHelper
import views.html.hvd.excise_goods

import scala.concurrent.Future

trait ExciseGoodsController extends BaseController with DateOfChangeHelper {

  val dataCacheConnector: DataCacheConnector
  val statusService: StatusService


  def get(edit: Boolean = false) =
    Authorised.async {
      implicit authContext =>
        implicit request =>
          dataCacheConnector.fetch[Hvd](Hvd.key) map {
            response =>
              val form: Form2[ExciseGoods] = (for {
                hvd <- response
                exciseGoods <- hvd.exciseGoods
              } yield Form2[ExciseGoods](exciseGoods)).getOrElse(EmptyForm)
              Ok(excise_goods(form, edit))
          }
    }


  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request => {
        Form2[ExciseGoods](request.body) match {
          case f: InvalidForm =>
            Future.successful(BadRequest(excise_goods(f, edit)))
          case ValidForm(_, data) =>
            for {
              hvd <- dataCacheConnector.fetch[Hvd](Hvd.key)
              status <- statusService.getStatus
              _ <- dataCacheConnector.save[Hvd](Hvd.key,
                hvd.exciseGoods(data)
              )
            } yield {
              if (redirectToDateOfChange[ExciseGoods](status, hvd.exciseGoods, data)) {
                Redirect(routes.HvdDateOfChangeController.get())
              } else {
                edit match {
                  case true => Redirect(routes.SummaryController.get())
                  case false => Redirect(routes.HowWillYouSellGoodsController.get())
                }
              }
            }
        }
      }
  }
}

object ExciseGoodsController extends ExciseGoodsController {
  // $COVERAGE-OFF$
  override val dataCacheConnector = DataCacheConnector
  override val authConnector = AMLSAuthConnector
  override val statusService = StatusService
}
