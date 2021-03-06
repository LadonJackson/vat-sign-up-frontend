/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.httpparsers.StoreOverseasInformationHttpParser._
import uk.gov.hmrc.vatsignupfrontend.models.BusinessEntity.BusinessEntitySessionFormatter
import uk.gov.hmrc.vatsignupfrontend.models.Overseas
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockStoreOverseasInformationService

import scala.concurrent.Future

class OverseasResolverControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents
  with MockStoreOverseasInformationService {

  object TestOverseasResolverController extends OverseasResolverController(
    mockControllerComponents,
    mockStoreOverseasInformationService
  )

  lazy val testGetRequest = FakeRequest("GET", "/overseas-resolver")

  "calling the resolve method on OverseasResolverController" when {
    "store overseas information returns StoreOverseasInformationSuccess" should {
      "goto email" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreOverseasInformation(testVatNumber)(Future.successful(Right(StoreOverseasInformationSuccess)))

        val request = testGetRequest.withSession(SessionKeys.vatNumberKey -> testVatNumber)

        val res = await(TestOverseasResolverController.resolve(request))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.EmailRoutingController.route().url)
        res.session(request) get SessionKeys.businessEntityKey contains Overseas.toString
      }
    }
    "store Overseas information returns StoreOverseasInformationFailureResponse" should {
      "throw internal server exception" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreOverseasInformation(testVatNumber)(Future.successful(Left(StoreOverseasInformationFailureResponse(INTERNAL_SERVER_ERROR))))

        intercept[InternalServerException] {
          await(TestOverseasResolverController.resolve(testGetRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber
          )))
        }
      }
    }
    "vat number is not in session" should {
      "goto capture vat number" in {
        mockAuthRetrieveAgentEnrolment()
        mockStoreOverseasInformation(testVatNumber)(Future.successful(Right(StoreOverseasInformationSuccess)))

        val res = await(TestOverseasResolverController.resolve(testGetRequest))

        status(res) shouldBe SEE_OTHER
        redirectLocation(res) shouldBe Some(routes.CaptureVatNumberController.show().url)
      }
    }
  }

}

