/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object VatNumberEligibilityHttpParser {
  type VatNumberEligibilityResponse = Either[VatNumberIneligible, VatNumberEligible.type]

  implicit object VatNumberEligibilityHttpReads extends HttpReads[VatNumberEligibilityResponse] {
    override def read(method: String, url: String, response: HttpResponse): VatNumberEligibilityResponse = {
      response.status match {
        case NO_CONTENT => Right(VatNumberEligible)
        case BAD_REQUEST => Left(IneligibleForMtdVatNumber)
        case NOT_FOUND => Left(InvalidVatNumber)
        case CONFLICT => Left(VatNumberAlreadySubscribed)
        case status => Left(VatNumberEligibilityFailureResponse(status))
      }
    }
  }

  case object VatNumberEligible

  sealed trait VatNumberIneligible

  case object IneligibleForMtdVatNumber extends VatNumberIneligible

  case object VatNumberAlreadySubscribed extends VatNumberIneligible

  case object InvalidVatNumber extends VatNumberIneligible

  case class VatNumberEligibilityFailureResponse(status: Int) extends VatNumberIneligible

}
