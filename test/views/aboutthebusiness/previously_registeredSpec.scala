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

package views.aboutthebusiness

import forms.{InvalidForm, ValidForm, Form2}
import models.aboutthebusiness.{PreviouslyRegisteredYes, PreviouslyRegistered}
import org.scalatest.{MustMatchers}
import  utils.GenericTestHelper
import jto.validation.Path
import jto.validation.ValidationError
import play.api.i18n.Messages
import views.Fixture


class previously_registeredSpec extends GenericTestHelper with MustMatchers  {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
  }

  "previously_registered view" must {
    "have correct title" in new ViewFixture {

      val form2: ValidForm[PreviouslyRegistered] = Form2(PreviouslyRegisteredYes("prevMLRRegNo"))

      def view = views.html.aboutthebusiness.previously_registered(form2, true)

      doc.title must startWith(Messages("aboutthebusiness.registeredformlr.title") + " - " + Messages("summary.aboutbusiness"))
    }

    "have correct headings" in new ViewFixture {

      val form2: ValidForm[PreviouslyRegistered] = Form2(PreviouslyRegisteredYes("prevMLRRegNo"))

      def view = views.html.aboutthebusiness.previously_registered(form2, true)

      heading.html must be(Messages("aboutthebusiness.registeredformlr.title"))
      subHeading.html must include(Messages("summary.aboutbusiness"))

    }

    "show errors in the correct locations" in new ViewFixture {

      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "previouslyRegistered") -> Seq(ValidationError("not a message Key")),
          (Path \ "prevMLRRegNo-panel") -> Seq(ValidationError("second not a message Key"))
        ))

      def view = views.html.aboutthebusiness.previously_registered(form2, true)

      errorSummary.html() must include("not a message Key")
      errorSummary.html() must include("second not a message Key")

      doc.getElementById("previouslyRegistered")
        .getElementsByClass("error-notification").first().html() must include("not a message Key")

      doc.getElementById("prevMLRRegNo-panel")
        .getElementsByClass("error-notification").first().html() must include("second not a message Key")

    }
  }
}
