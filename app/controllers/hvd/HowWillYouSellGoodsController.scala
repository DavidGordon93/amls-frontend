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
import models.hvd.{HowWillYouSellGoods, Hvd, SalesChannel}
import models.status.{ReadyForRenewal, SubmissionDecisionApproved}
import play.api.Logger
import services.StatusService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.DateOfChangeHelper
import views.html.hvd.how_will_you_sell_goods

import scala.concurrent.Future


trait HowWillYouSellGoodsController extends BaseController with DateOfChangeHelper {

  protected def dataCacheConnector: DataCacheConnector

  protected def statusService: StatusService


  def get(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        dataCacheConnector.fetch[Hvd](Hvd.key) map {
          response =>
            val form: Form2[HowWillYouSellGoods] = (for {
              hvd <- response
              channels <- hvd.howWillYouSellGoods
            } yield Form2[HowWillYouSellGoods](channels)).getOrElse(EmptyForm)
            Ok(how_will_you_sell_goods(form, edit))
        }
  }

  def post(edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request => {
        Form2[HowWillYouSellGoods](request.body) match {
          case f: InvalidForm =>
            Future.successful(BadRequest(how_will_you_sell_goods(f, edit)))
          case ValidForm(_, model) =>
            for {
              hvd <- dataCacheConnector.fetch[Hvd](Hvd.key)
              status <- statusService.getStatus
              _ <- dataCacheConnector.save[Hvd](Hvd.key,
                hvd.howWillYouSellGoods(model)
              )
            } yield {
              if (redirectToDateOfChange[HowWillYouSellGoods](status, hvd.howWillYouSellGoods, model)) {
                Redirect(routes.HvdDateOfChangeController.get())
              } else {
                edit match {
                  case true => Redirect(routes.SummaryController.get())
                  case false => Redirect(routes.CashPaymentController.get())
                }
              }
            }
        }
      }
  }
}

object HowWillYouSellGoodsController extends HowWillYouSellGoodsController {
  override protected def authConnector: AuthConnector = AMLSAuthConnector

  override protected def dataCacheConnector: DataCacheConnector = DataCacheConnector

  override protected def statusService: StatusService = StatusService
}
