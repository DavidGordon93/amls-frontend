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

import jto.validation.{Invalid, Path, Valid, ValidationError}
import models.renewal.UpdateOrRenew.{First, Second}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class UpdateOrRenewSpec extends PlaySpec with MockitoSugar {

  "Form Validation" must {
    "successfully validate given a 'renew` value" in {
      val data = Map(
        "updateorrenew" -> Seq("01")
      )

      UpdateOrRenew.formRule.validate(data) must
        be(Valid(First))
    }

    "successfully validate given an 'update` value" in {
      val data = Map(
        "updateorrenew" -> Seq("02")
      )

      UpdateOrRenew.formRule.validate(data) must
        be(Valid(Second))
    }

    "fail to validate given an invalid value" in {

      val data = Map(
        "updateorrenew" -> Seq("")
      )

      UpdateOrRenew.formRule.validate(data) must
        be(Invalid(Seq(
          (Path \ "updateorrenew") -> Seq(ValidationError("error.required.updateorrenew"))
        )))
    }
  }

}
