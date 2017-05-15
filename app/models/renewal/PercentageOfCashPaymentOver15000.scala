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

package models.renewal

import jto.validation._
import jto.validation.forms.UrlFormEncoded
import jto.validation.ValidationError
import play.api.libs.json._

sealed trait PercentageOfCashPaymentOver15000

object PercentageOfCashPaymentOver15000 {

  case object First extends PercentageOfCashPaymentOver15000
  case object Second extends PercentageOfCashPaymentOver15000
  case object Third extends PercentageOfCashPaymentOver15000
  case object Fourth extends PercentageOfCashPaymentOver15000
  case object Fifth extends PercentageOfCashPaymentOver15000

  import utils.MappingUtils.Implicits._

  implicit val formRule: Rule[UrlFormEncoded, PercentageOfCashPaymentOver15000] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    (__ \ "percentage").read[String].withMessage("error.required.renewal.hvd.percentage") flatMap {
      case "01" => First
      case "02" => Second
      case "03" => Third
      case "04" => Fourth
      case "05" => Fifth
      case _ =>
        (Path \ "percentage") -> Seq(ValidationError("error.invalid"))
    }
  }

  implicit val formWrites: Write[PercentageOfCashPaymentOver15000, UrlFormEncoded] = Write {
    case First => "percentage" -> "01"
    case Second => "percentage" -> "02"
    case Third => "percentage" -> "03"
    case Fourth => "percentage" -> "04"
    case Fifth => "percentage" -> "05"

  }

  implicit val jsonReads = {
    import play.api.libs.json.Reads.StringReads
    (__ \ "percentage").read[String].flatMap[PercentageOfCashPaymentOver15000] {
      case "01" => First
      case "02" => Second
      case "03" => Third
      case "04" => Fourth
      case "05" => Fifth
      case _ =>
        play.api.data.validation.ValidationError("error.invalid")
    }
  }

  implicit val jsonWrites = Writes[PercentageOfCashPaymentOver15000] {
    case First => Json.obj("percentage" -> "01")
    case Second => Json.obj("percentage" -> "02")
    case Third => Json.obj("percentage" -> "03")
    case Fourth => Json.obj("percentage" -> "04")
    case Fifth => Json.obj("percentage" -> "05")
  }

  implicit def convert(model: PercentageOfCashPaymentOver15000): models.hvd.PercentageOfCashPaymentOver15000 = model match {
    case PercentageOfCashPaymentOver15000.First => models.hvd.PercentageOfCashPaymentOver15000.First
    case PercentageOfCashPaymentOver15000.Second => models.hvd.PercentageOfCashPaymentOver15000.First
    case PercentageOfCashPaymentOver15000.Third => models.hvd.PercentageOfCashPaymentOver15000.First
    case PercentageOfCashPaymentOver15000.Fourth => models.hvd.PercentageOfCashPaymentOver15000.First
    case PercentageOfCashPaymentOver15000.Fifth => models.hvd.PercentageOfCashPaymentOver15000.First
    case _ => throw new Exception("Invalid PercentageOfCashPaymentOver15000")
  }
}