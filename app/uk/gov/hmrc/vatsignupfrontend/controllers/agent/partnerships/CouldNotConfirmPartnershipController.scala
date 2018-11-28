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

package uk.gov.hmrc.vatsignupfrontend.controllers.agent.partnerships

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.vatsignupfrontend.config.ControllerComponents
import uk.gov.hmrc.vatsignupfrontend.config.auth.AgentEnrolmentPredicate
import uk.gov.hmrc.vatsignupfrontend.config.featureswitch.{GeneralPartnershipJourney, LimitedPartnershipJourney}
import uk.gov.hmrc.vatsignupfrontend.controllers.AuthenticatedController
import uk.gov.hmrc.vatsignupfrontend.views.html.agent.partnerships.could_not_confirm_partnership
import uk.gov.hmrc.vatsignupfrontend.controllers.agent.{routes => agentRoutes}

import scala.concurrent.Future


@Singleton
class CouldNotConfirmPartnershipController @Inject()(val controllerComponents: ControllerComponents)
  extends AuthenticatedController(AgentEnrolmentPredicate, featureSwitches = Set(GeneralPartnershipJourney, LimitedPartnershipJourney)) {

  override def featureEnabled[T](func: => T): T =
    if (featureSwitches exists isEnabled) func
    else throw new NotFoundException(featureSwitchError)

  def show: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(Ok(could_not_confirm_partnership(routes.CouldNotConfirmPartnershipController.submit())))
    }
  }

  def submit: Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      Future.successful(Redirect(agentRoutes.CaptureBusinessEntityController.show()))
    }
  }

}
