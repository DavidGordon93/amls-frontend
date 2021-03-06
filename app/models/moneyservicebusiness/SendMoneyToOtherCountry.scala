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

package models.moneyservicebusiness

import jto.validation.{Write, From, Rule}
import jto.validation.forms._
import play.api.libs.json.Json

case class SendMoneyToOtherCountry(money: Boolean)

object SendMoneyToOtherCountry {

  import utils.MappingUtils.Implicits._

  implicit val format =  Json.format[SendMoneyToOtherCountry]

  implicit val formRule: Rule[UrlFormEncoded, SendMoneyToOtherCountry] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    (__ \ "money").read[Boolean].withMessage("error.required.msb.send.money") map SendMoneyToOtherCountry.apply
  }

  implicit val formWrites: Write[SendMoneyToOtherCountry, UrlFormEncoded] = Write {x =>
    "money" -> x.money.toString
  }
}



