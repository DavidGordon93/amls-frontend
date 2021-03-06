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

import play.api.libs.json.{Json, Reads}

case class Renewal(
                    involvedInOtherActivities: Option[InvolvedInOther] = None,
                    businessTurnover: Option[BusinessTurnover] = None,
                    turnover: Option[AMLSTurnover] = None,
                    customersOutsideUK: Option[CustomersOutsideUK] = None,
                    percentageOfCashPaymentOver15000: Option[PercentageOfCashPaymentOver15000] = None,
                    receiveCashPayments: Option[ReceiveCashPayments] = None,
                    totalThroughput: Option[TotalThroughput] = None,
                    whichCurrencies: Option[WhichCurrencies] = None,
                    transactionsInLast12Months: Option[TransactionsInLast12Months] = None,
                    sendTheLargestAmountsOfMoney: Option[SendTheLargestAmountsOfMoney] = None,
                    mostTransactions: Option[MostTransactions] = None,
                    ceTransactionsInLast12Months: Option[CETransactionsInLast12Months] = None,
                    hasChanged: Boolean = false
) {
  def involvedInOtherActivities(model: InvolvedInOther): Renewal =
    this.copy(involvedInOtherActivities = Some(model), hasChanged = hasChanged || !this.involvedInOtherActivities.contains(model))

  def businessTurnover(model: BusinessTurnover): Renewal =
    this.copy(businessTurnover = Some(model), hasChanged = hasChanged || !this.businessTurnover.contains(model))

  def resetBusinessTurnover: Renewal =
    this.copy(businessTurnover = None, hasChanged = hasChanged || !this.businessTurnover.isEmpty)

  def turnover(model: AMLSTurnover): Renewal =
    this.copy(turnover = Some(model), hasChanged = hasChanged || !this.turnover.contains(model))

  def customersOutsideUK(model: CustomersOutsideUK): Renewal =
    this.copy(customersOutsideUK = Some(model), hasChanged = hasChanged || !this.customersOutsideUK.contains(model))

  def percentageOfCashPaymentOver15000(v: PercentageOfCashPaymentOver15000): Renewal =
    this.copy(percentageOfCashPaymentOver15000 = Some(v))

  def receiveCashPayments(p: ReceiveCashPayments): Renewal =
    this.copy(receiveCashPayments = Some(p), hasChanged = hasChanged || !this.receiveCashPayments.contains(p))

  def totalThroughput(model: TotalThroughput): Renewal =
    this.copy(totalThroughput = Some(model), hasChanged = hasChanged || !this.totalThroughput.contains(model))

  def whichCurrencies(model: WhichCurrencies): Renewal =
    this.copy(whichCurrencies = Some(model), hasChanged = hasChanged || !this.whichCurrencies.contains(model))

  def transactionsInLast12Months(model: TransactionsInLast12Months): Renewal =
    this.copy(transactionsInLast12Months = Some(model), hasChanged = hasChanged || !this.transactionsInLast12Months.contains(model))

  def sendTheLargestAmountsOfMoney(p: SendTheLargestAmountsOfMoney): Renewal =
    this.copy(sendTheLargestAmountsOfMoney = Some(p), hasChanged = hasChanged || !this.sendTheLargestAmountsOfMoney.contains(p))

  def ceTransactionsInLast12Months(p: CETransactionsInLast12Months): Renewal =
    this.copy(ceTransactionsInLast12Months = Some(p), hasChanged = hasChanged || !this.ceTransactionsInLast12Months.contains(p))

  def mostTransactions(model: MostTransactions): Renewal =
    this.copy(mostTransactions = Some(model), hasChanged = hasChanged || !this.mostTransactions.contains(model))
}

object Renewal {
  val key = "renewal"

  implicit val formats = Json.format[Renewal]

  implicit def default(renewal: Option[Renewal]): Renewal =
    renewal.getOrElse(Renewal())


}
