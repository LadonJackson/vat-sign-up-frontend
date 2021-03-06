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
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{ContactPreferencesJourney, VerifyAgentEmail, VerifyClientEmail}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.agent_email_verified
import play.api.i18n.Messages.Implicits._


class AgentVerifiedEmailControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockControllerComponents {

  object TestAgentVerifiedEmailController extends AgentVerifiedEmailController(mockControllerComponents)

  "Calling the show action of the AgentVerifiedEmailController" when {
      implicit lazy val testGetRequest = FakeRequest("GET", "/verified-your-email")
      "the capture client email feature switch is disabled" should {
        "go to the Agent email Verified page with a link to the terms of participation page" in {
          enable(VerifyAgentEmail)

          mockAuthRetrieveAgentEnrolment()
          val result = await(TestAgentVerifiedEmailController.show(testGetRequest))

          status(result) shouldBe Status.OK
          contentAsString(result) shouldBe agent_email_verified(routes.TermsController.show().url).body
        }
      }

    "the capture client email feature switch is enabled" should {
      "go to the Agent email Verified page with a link to the agree to capture client email page" in {
        enable(VerifyAgentEmail)
        enable(VerifyClientEmail)

        mockAuthRetrieveAgentEnrolment()
        val result = await(TestAgentVerifiedEmailController.show(testGetRequest))

        status(result) shouldBe Status.OK
        contentAsString(result) shouldBe agent_email_verified(routes.AgreeCaptureClientEmailController.show().url).body
      }
    }
    "the contact preference feature switch is enabled" should {
      "go to the Agent email Verified page with a link to the Contact Preferences page" in {
        enable(VerifyAgentEmail)
        enable(ContactPreferencesJourney)

        mockAuthRetrieveAgentEnrolment()
        val result = await(TestAgentVerifiedEmailController.show(testGetRequest))

        status(result) shouldBe Status.OK
        contentAsString(result) shouldBe agent_email_verified(routes.ContactPreferenceController.show().url).body
      }
    }
  }
}
