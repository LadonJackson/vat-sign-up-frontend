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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import play.api.http.Status._
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.GovernmentOrganisationJourney
import uk.gov.hmrc.vatsignupfrontend.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.AuthStub.{stubAuth, successfulAuthResponse}
import uk.gov.hmrc.vatsignupfrontend.helpers.servicemocks.StoreGovernmentOrganisationInformationStub._
import uk.gov.hmrc.vatsignupfrontend.helpers.{ComponentSpecBase, CustomMatchers}

class GovernmentOrganisationResolverControllerISpec extends ComponentSpecBase with CustomMatchers {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(GovernmentOrganisationJourney)
  }

  "GET /government-organisation-resolver" when {
    "the government organisation feature switch is on" when {
      "store government organisation information returned NO_CONTENT" should {
        "goto agree capture email" in {
          stubAuth(OK, successfulAuthResponse())

          stubStoreGovernmentOrganisationInformation(testVatNumber)(NO_CONTENT)

          val res = get("/government-organisation-resolver", Map(
            SessionKeys.vatNumberKey -> testVatNumber
          ))

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.AgreeCaptureEmailController.show().url)
          )
        }
      }
      "store government organisation information returned failure status" should {
        "return INTERNAL_SERVER_ERROR" in {
          stubAuth(OK, successfulAuthResponse())

          stubStoreGovernmentOrganisationInformation(testVatNumber)(INTERNAL_SERVER_ERROR)

          val res = get("/government-organisation-resolver", Map(
            SessionKeys.vatNumberKey -> testVatNumber
          ))

          res should have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
      "there is no vat number in session" should {
        "goto resolve vat number" in {
          stubAuth(OK, successfulAuthResponse())

          stubStoreGovernmentOrganisationInformation(testVatNumber)(OK)

          val res = get("/government-organisation-resolver")

          res should have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.ResolveVatNumberController.resolve().url)
          )
        }
      }
    }

    "the government organisation feature switch is off" should {
      "return NOT_FOUND" in {
        disable(GovernmentOrganisationJourney)

        val res = get("/government-organisation-resolver")

        res should have(
          httpStatus(NOT_FOUND)
        )
      }
    }
  }

}
