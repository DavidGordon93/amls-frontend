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

package models.businessmatching

import models.businessmatching.BusinessType._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import jto.validation.{Invalid, Path, Valid}
import jto.validation.ValidationError

class BusinessTypeSpec extends PlaySpec with MockitoSugar {

  "Form Validation" must {

    "successfully validate" when {
      "successfully validate given a valid enum value" in {

        BusinessType.formR.validate(Map("businessType" -> Seq("01"))) must
          be(Valid(LimitedCompany))

        BusinessType.formR.validate(Map("businessType" -> Seq("02"))) must
          be(Valid(SoleProprietor))

        BusinessType.formR.validate(Map("businessType" -> Seq("03"))) must
          be(Valid(Partnership))

        BusinessType.formR.validate(Map("businessType" -> Seq("04"))) must
          be(Valid(LPrLLP))

        BusinessType.formR.validate(Map("businessType" -> Seq("05"))) must
          be(Valid(UnincorporatedBody))
      }
    }

    "fail validation" when {
      "fail validation when given invalid data" in {
        BusinessType.formR.validate(Map("businessType" -> Seq("20"))) must
          be(Invalid(Seq((Path \ "businessType", Seq(ValidationError("error.invalid"))))))
      }

      "fail validation when given missing data represented by an empty Map" in {
        BusinessType.formR.validate(Map.empty) must
          be(Invalid(Seq((Path \ "businessType", Seq(ValidationError("error.required"))))))
      }

      "fail validation when given missing data represented by an empty string" in {
        BusinessType.formR.validate(Map("businessType" -> Seq(""))) must
          be(Invalid(Seq((Path \ "businessType", Seq(ValidationError("error.invalid"))))))
      }
    }

    "write correct data from enum value" in {

      BusinessType.formW.writes(LimitedCompany) must
        be(Map("businessType" -> Seq("01")))

      BusinessType.formW.writes(SoleProprietor) must
        be(Map("businessType" -> Seq("02")))

      BusinessType.formW.writes(Partnership) must
        be(Map("businessType" -> Seq("03")))

      BusinessType.formW.writes(LPrLLP) must
        be(Map("businessType" -> Seq("04")))

      BusinessType.formW.writes(UnincorporatedBody) must
        be(Map("businessType" -> Seq("05")))
    }
  }
}
