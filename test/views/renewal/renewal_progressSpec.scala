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

import models.registrationprogress.{Completed, Section}
import org.joda.time.LocalDate
import play.api.i18n.Messages
import utils.GenericTestHelper
import views.Fixture
import utils.DateHelper
import utils.Strings.TextHelpers
import org.scalatest.MustMatchers

class renewal_progressSpec extends GenericTestHelper {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val renewalSection = Section("renewal", Completed, true, controllers.renewal.routes.SummaryController.get())

    val renewalDate = LocalDate.now().plusDays(15)

  }

  "The renewal progress view" must {

    "Have the correct title and headings " in new ViewFixture {
      override def view = views.html.renewal.renewal_progress(renewalSection, Seq.empty, true, true, Some(renewalDate))

      doc.title must startWith(Messages("renewal.progress.title"))

      doc.title must be(Messages("renewal.progress.title") +
        " - " + Messages("summary.status") +
        " - " + Messages("title.amls") +
        " - " + Messages("title.gov"))
      heading.html must be(Messages("renewal.progress.title"))
      subHeading.html must include(Messages("summary.status"))
    }


    "enable the submit registration button" in new ViewFixture {

      override def view = views.html.renewal.renewal_progress(renewalSection, Seq.empty, true, true, Some(renewalDate))

      doc.select("form button[name=submit]").hasAttr("disabled") mustBe false
    }

    "disable the submit registration button" in new ViewFixture {

      override def view = views.html.renewal.renewal_progress(renewalSection, Seq.empty, false, true,Some(renewalDate))

      doc.select("form button[name=submit]").hasAttr("disabled") mustBe true
    }

    "show intro for MSB and TCSP businesses" in new ViewFixture {

      override def view = views.html.renewal.renewal_progress(renewalSection, Seq.empty, false, true, Some(renewalDate))

      html must include (Messages("renewal.progress.tpandrp.intro", DateHelper.formatDate(renewalDate)).convertLineBreaks)
    }

    "show intro for non MSB and TCSP businesses" in new ViewFixture {

      override def view = views.html.renewal.renewal_progress(renewalSection, Seq.empty, false, false, Some(renewalDate))

      html must include (Messages("renewal.progress.tponly.intro", DateHelper.formatDate(renewalDate)).convertLineBreaks)
    }

  }

}
