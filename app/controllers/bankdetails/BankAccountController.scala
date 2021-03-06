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

package controllers.bankdetails

import cats.data.OptionT
import cats.implicits._
import config.{AMLSAuditConnector, AMLSAuthConnector}
import connectors.DataCacheConnector
import controllers.BaseController
import forms.{EmptyForm, Form2, InvalidForm, ValidForm}
import models.bankdetails.{BankAccount, BankDetails}
import services.StatusService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import utils.{ControllerHelper, RepeatingSection, StatusConstants}

import scala.concurrent.Future

trait BankAccountController extends RepeatingSection with BaseController {

  val dataCacheConnector: DataCacheConnector
  val auditConnector: AuditConnector
  implicit val statusService: StatusService

  def get(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request =>
        for {
          bankDetails <- getData[BankDetails](index)
          allowedToEdit <- ControllerHelper.allowedToEdit(edit)
        } yield bankDetails match {
          case Some(BankDetails(_, Some(data), _, _, _)) if allowedToEdit => Ok(views.html.bankdetails.bank_account_details(Form2[BankAccount](data), edit, index))
          case Some(_) if allowedToEdit => Ok(views.html.bankdetails.bank_account_details(EmptyForm, edit, index))
          case _ => NotFound(notFoundView)
        }
  }

  def post(index: Int, edit: Boolean = false) = Authorised.async {
    implicit authContext =>
      implicit request => {
        lazy val sendAudit = for {
          details <- OptionT(getData[BankDetails](index))
          result <- OptionT.liftF(auditConnector.sendEvent(audit.AddBankAccountEvent(details)))
        } yield result

        Form2[BankAccount](request.body) match {
          case f: InvalidForm =>
            Future.successful(BadRequest(views.html.bankdetails.bank_account_details(f, edit, index)))
          case ValidForm(_, data) =>
            updateDataStrict[BankDetails](index) { bd =>
              bd.copy(
                bankAccount = Some(data),
                status = Some(if (edit) {
                  StatusConstants.Updated
                } else {
                  StatusConstants.Added
                })
              )
            }.flatMap { _ =>
              if (edit) {
                Future.successful(Redirect(routes.SummaryController.get(false)))
              } else {
                lazy val redirect = Redirect(routes.BankAccountRegisteredController.get(index))
                (sendAudit map { _ =>
                  redirect
                }) getOrElse redirect
              }
            }
        }
      }.recoverWith {
        case _: IndexOutOfBoundsException => Future.successful(NotFound(notFoundView))
      }
  }
}

object BankAccountController extends BankAccountController {
  // $COVERAGE-OFF$
  override val authConnector = AMLSAuthConnector
  override val dataCacheConnector = DataCacheConnector
  override implicit val statusService = StatusService
  override val auditConnector = AMLSAuditConnector
}
