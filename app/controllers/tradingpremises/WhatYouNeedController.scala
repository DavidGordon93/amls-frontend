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

package controllers.tradingpremises


import config.AMLSAuthConnector
import connectors.DataCacheConnector
import controllers.BaseController
import models.businessmatching.{BusinessMatching, MoneyServiceBusiness}
import utils.ControllerHelper
import views.html.tradingpremises._

trait WhatYouNeedController extends BaseController {

  val dataCacheConnector: DataCacheConnector

  def get(index: Int) = Authorised.async {
    implicit authContext => implicit request =>
      dataCacheConnector.fetch[BusinessMatching](BusinessMatching.key) map {
        response =>
        Ok(what_you_need(index, ControllerHelper.isMSBSelected(response)))
      }
  }

}

object WhatYouNeedController extends WhatYouNeedController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
}
