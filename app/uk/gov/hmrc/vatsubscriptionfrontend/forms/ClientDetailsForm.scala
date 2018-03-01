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

package uk.gov.hmrc.vatsubscriptionfrontend.forms

import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import uk.gov.hmrc.vatsubscriptionfrontend.models.{ClientDetailsModel, DateModel}


object ClientDetailsForm {
  val dateMapping: Mapping[DateModel] = mapping(
    "day" -> text,
    "month" -> text,
    "year" -> text
  )(DateModel.apply)(DateModel.unapply)


  val clientDetailsForm = Form(
    mapping(
      "firstName" -> text,
      "lastName" -> text,
      "nino" -> text,
      "dateOfBirth" -> dateMapping
    )(ClientDetailsModel.apply)(ClientDetailsModel.unapply)
  )
}
