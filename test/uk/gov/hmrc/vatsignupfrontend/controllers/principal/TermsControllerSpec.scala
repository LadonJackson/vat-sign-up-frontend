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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.assets.MessageLookup
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{DirectDebitTermsJourney, SendYourApplication}
import uk.gov.hmrc.vatsignupfrontend.config.mocks.MockControllerComponents
import uk.gov.hmrc.vatsignupfrontend.controllers.ControllerSpec
import uk.gov.hmrc.vatsignupfrontend.helpers.TestConstants._
import uk.gov.hmrc.vatsignupfrontend.services.mocks.MockSubmissionService

class TermsControllerSpec extends ControllerSpec with MockSubmissionService {

  object TestTermsController extends TermsController(mockControllerComponents, mockSubmissionService)

  lazy val testGetRequest = FakeRequest("GET", "/terms-of-participation")

  lazy val testPostRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/terms-of-participation")

  "Calling the show action of the Terms controller" should {
    "show the Terms page" when {
      "the SendYourApplication feature is disabled" in {
        mockAuthAdminRole()
        val request = testGetRequest

        val result = TestTermsController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
        titleOf(result) shouldBe MessageLookup.PrincipalTerms.title
      }
    }
    "show the SendYourApplication page" when {
      "the SendYourApplication feature is enabled" in {
        enable(SendYourApplication)
        mockAuthAdminRole()
        val request = testGetRequest

        val result = TestTermsController.show(request)
        status(result) shouldBe Status.OK
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
        titleOf(result) shouldBe MessageLookup.PrincipalSendYourApplication.title
      }
    }
  }

  "Calling the submit action of the Terms controller" when {
    "the user does not have the direct debit attribute on the control list" should {
      "submit successfully and go to information received" in {
        mockAuthAdminRole()
        mockSubmitSuccess(testVatNumber)

        val result = TestTermsController.submit(
          testPostRequest.withSession(
            SessionKeys.vatNumberKey -> testVatNumber,
            SessionKeys.acceptedDirectDebitTermsKey -> "true"
          )
        )

        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.InformationReceivedController.show().url)
      }

      "not submit" when {
        "the vat number is missing and go to resolve vat number" in {
          mockAuthAdminRole()
          val result = TestTermsController.submit(testPostRequest)
          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ResolveVatNumberController.resolve().url)
        }
      }
    }

    "the user does have the direct debit attribute on the control list" should {
      "VAT number isn't in session but acceptedDirectDebit is in session" should {
        "goto resolve vat number" in {
          mockAuthAdminRole()
          val result = TestTermsController.submit(
            testPostRequest.withSession(
              SessionKeys.hasDirectDebitKey -> "true",
              SessionKeys.acceptedDirectDebitTermsKey -> "true"
            )
          )

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ResolveVatNumberController.resolve().url)
        }
      }

      "VAT number is in session and acceptedDirectDebit isn't in session" should {
        "goto accept direct debit terms and conditions" in {
          enable(DirectDebitTermsJourney)

          mockAuthAdminRole()
          val result = TestTermsController.submit(
            testPostRequest.withSession(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.hasDirectDebitKey -> "true"
            )
          )

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DirectDebitTermsAndConditionsController.show().url)
        }
      }

      "VAT number is in session and acceptedDirectDebit is false" should {
        "goto accept direct debit terms and conditions" in {
          enable(DirectDebitTermsJourney)

          mockAuthAdminRole()
          val result = TestTermsController.submit(
            testPostRequest.withSession(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.hasDirectDebitKey -> "true",
              SessionKeys.acceptedDirectDebitTermsKey -> "false"
            )
          )

          status(result) shouldBe Status.SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DirectDebitTermsAndConditionsController.show().url)
        }
      }
    }

    "submission service fails" should {
      "goto technical difficulties" in {
        mockAuthAdminRole()
        mockSubmitFailure(testVatNumber)
        intercept[InternalServerException] {
          await(TestTermsController.submit(
            testPostRequest.withSession(
              SessionKeys.vatNumberKey -> testVatNumber,
              SessionKeys.hasDirectDebitKey -> "true",
              SessionKeys.acceptedDirectDebitTermsKey -> "true"
            )
          ))
        }
      }
    }
  }

}
