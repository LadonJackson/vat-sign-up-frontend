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

package uk.gov.hmrc.vatsignupfrontend.controllers.principal

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrievals, ~}
import uk.gov.hmrc.auth.core.{Admin, Enrolments}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._

import scala.concurrent.Future

class ResolveVatNumberControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestResolveVatNumberController extends ResolveVatNumberController(mockControllerComponents)

  lazy val testGetRequest = FakeRequest("GET", "/resolve-vat-number")

  "Calling the resolve action of the Resolve Vat Number controller" when {
    "the user has a VAT-DEC enrolment" should {
      "redirect to Multiple Vat Check page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.affinityGroup)
        )(Future.successful(new ~(Some(Admin), new ~(Enrolments(Set(testVatDecEnrolment)), None))))
        val request = testGetRequest

        val result = TestResolveVatNumberController.resolve(request)
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.MultipleVatCheckController.show().url)

      }
    }

    "the user does not have a VAT-DEC enrolment" should {
      "redirect to Capture VAT number page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.affinityGroup)
        )(Future.successful(new ~(Some(Admin), new ~(Enrolments(Set()), None))))

        val result = TestResolveVatNumberController.resolve(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.CaptureVatNumberController.show().url)
      }

    }

    "the user has an agent enrolment" should {
      "redirect to the agent using principal journey page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.affinityGroup)
        )(Future.successful(new ~(Some(Admin), new ~(Enrolments(Set(testAgentEnrolment)), None))))

        val result = TestResolveVatNumberController.resolve(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.AgentUsingPrincipalJourneyController.show().url)
      }
    }

    "the user has an agent affinity group" should {
      "redirect to the agent using principal journey page" in {
        mockAuthorise(
          retrievals = Retrievals.credentialRole and (Retrievals.allEnrolments and Retrievals.affinityGroup)
        )(Future.successful(new ~(Some(Admin), new ~(Enrolments(Set(testAgentEnrolment)), None))))

        val result = TestResolveVatNumberController.resolve(testGetRequest)

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) should contain(routes.AgentUsingPrincipalJourneyController.show().url)
      }
    }
  }

}
