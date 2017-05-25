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

sealed trait UpdateOrRenew

object UpdateOrRenew {

  case object First extends UpdateOrRenew
  case object Second extends UpdateOrRenew

  import utils.MappingUtils.Implicits._

  implicit val formRule: Rule[UrlFormEncoded, UpdateOrRenew] = From[UrlFormEncoded] { __ =>
    import jto.validation.forms.Rules._
    import models.FormTypes._
    (__ \ "updateorrenew").read[String] flatMap {
      case "01" => First
      case "02" => Second
      case _ =>
        (Path \ "updateorrenew") -> Seq(ValidationError("error.required.updateorrenew"))
    }
  }
}
