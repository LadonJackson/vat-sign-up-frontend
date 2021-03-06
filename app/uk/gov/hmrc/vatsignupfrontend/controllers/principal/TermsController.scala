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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Call, Result}
import play.api.mvc.Request
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.vatsignupfrontend.SessionKeys
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AdministratorRolePredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{DirectDebitTermsJourney, FeatureSwitching, SendYourApplication}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.httpparsers.SubmissionHttpParser.SubmissionFailureResponse
import uk.gov.hmrc.vatsignupfrontend.services.SubmissionService
import uk.gov.hmrc.vatsignupfrontend.views.html.principal.{send_your_application, terms}

import scala.concurrent.Future

@Singleton
class TermsController @Inject()(val controllerComponents: ControllerComponents,
                                val submissionService: SubmissionService
                               ) extends AuthenticatedController(AdministratorRolePredicate) {

  val show: Action[AnyContent] = Action.async { implicit request =>

    val renderView = (postAction: Call) => if (isEnabled(SendYourApplication)) send_your_application(postAction) else terms(postAction)

    authorised() {
      Future.successful(
        Ok(renderView(routes.TermsController.submit()))
      )
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      val optVatNumber = request.session.get(SessionKeys.vatNumberKey).filter(_.nonEmpty)
      val acceptedDirectDebitTerms = request.session.get(SessionKeys.acceptedDirectDebitTermsKey).getOrElse("false").toBoolean
      val hasDirectDebit = request.session.get(SessionKeys.hasDirectDebitKey).getOrElse("false").toBoolean

      def submit(vatNumber: String): Future[Result] = {
        submissionService.submit(vatNumber).map {
          case Right(_) =>
            Redirect(routes.InformationReceivedController.show())
          case Left(SubmissionFailureResponse(status)) =>
            throw new InternalServerException(s"Submission failed, backend returned: $status")
        }
      }

      (optVatNumber, hasDirectDebit) match {
        case (Some(vatNumber), true) if acceptedDirectDebitTerms || !isEnabled(DirectDebitTermsJourney) =>
          submit(vatNumber)
        case (Some(vatNumber), false) =>
          submit(vatNumber)
        case (Some(_), true) =>
          Future.successful(
            Redirect(routes.DirectDebitTermsAndConditionsController.show())
          )
        case _ =>
          Future.successful(
            Redirect(routes.ResolveVatNumberController.resolve())
          )
      }
    }
  }
}
