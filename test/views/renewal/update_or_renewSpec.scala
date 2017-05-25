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

package views.renewal

import forms.{EmptyForm, InvalidForm}
import jto.validation.{Path, ValidationError}
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import utils.GenericTestHelper
import views.Fixture

class update_or_renewSpec extends GenericTestHelper with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val renewalDate = LocalDate.now().plusDays(15)
  }

  "update_or_renew view" must {
    "have correct title, headings and form fields" in new ViewFixture {
      val form2 = EmptyForm

      def view = views.html.renewal.update_or_renew(form2, Some(renewalDate))

      doc.title must be(Messages("renewal.updateorrenew.title") +
        " - " + Messages("amendment.section-heading") +
        " - " + Messages("title.amls") +
        " - " + Messages("title.gov"))
      heading.html must be(Messages("renewal.updateorrenew.title"))
      subHeading.html must include(Messages("amendment.section-heading"))

    }

    "show errors in the correct locations" in new ViewFixture {
      val form2: InvalidForm = InvalidForm(Map.empty,
        Seq(
          (Path \ "updateorrenew") -> Seq(ValidationError("not a message Key 1"))
        ))

      def view = views.html.renewal.update_or_renew(form2, Some(renewalDate))

      errorSummary.html() must include("not a message Key 1")
    }
  }
}
