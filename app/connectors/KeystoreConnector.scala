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

package connectors

import config.{AmlsSessionCache, BusinessCustomerSessionCache}
import models.businesscustomer.ReviewDetails
import models.status.ConfirmationStatus
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}

import scala.concurrent.{ExecutionContext, Future}

trait KeystoreConnector {

  private[connectors] def businessCustomerDataCache: SessionCache
  private[connectors] def amlsDataCache: SessionCache

  private val key = "BC_Business_Details"

  def optionalReviewDetails
  (implicit
   hc: HeaderCarrier,
   ec: ExecutionContext
  ): Future[Option[ReviewDetails]] =
    businessCustomerDataCache.fetchAndGetEntry[ReviewDetails](key)

  def reviewDetails
  (implicit
   hc: HeaderCarrier,
   ec: ExecutionContext
  ): Future[ReviewDetails] =
    businessCustomerDataCache.fetchAndGetEntry[ReviewDetails](key) flatMap {
      case Some(reviewDetails) =>
        Future.successful(reviewDetails)
      case None =>
        Future.failed {
          new NotFoundException("No review details found for Session")
        }
    }

  def confirmationStatus(implicit hc: HeaderCarrier, ec: ExecutionContext) =
    amlsDataCache.fetchAndGetEntry[ConfirmationStatus](ConfirmationStatus.key) flatMap {
      case Some(s) => Future.successful(s)
      case _ => Future.successful(ConfirmationStatus(None))
    }

  def setConfirmationStatus(implicit hc: HeaderCarrier, ec: ExecutionContext) =
    amlsDataCache.cache(ConfirmationStatus.key, ConfirmationStatus(Some(true))) flatMap { _ => Future.successful() }

  def resetConfirmation(implicit hc: HeaderCarrier, ec: ExecutionContext) =
    amlsDataCache.cache(ConfirmationStatus.key, ConfirmationStatus(None)) flatMap { _ => Future.successful() }

}

object KeystoreConnector extends KeystoreConnector {
  // $COVERAGE-OFF$
  override private[connectors] def businessCustomerDataCache: SessionCache = BusinessCustomerSessionCache
  override private[connectors] def amlsDataCache: SessionCache = AmlsSessionCache
}
