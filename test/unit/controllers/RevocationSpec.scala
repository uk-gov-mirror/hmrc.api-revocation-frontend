/*
 * Copyright 2016 HM Revenue & Customs
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

package unit.controllers

import config.{WSHttp, FrontendAuthConnector}
import config.FrontendAuthConnector._
import controllers.Revocation
import org.mockito.BDDMockito.given
import org.mockito.Matchers.any
import org.mockito.{Matchers, Mockito, BDDMockito}
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.play.frontend.auth.AuthenticationProviderIds.GovernmentGatewayId
import uk.gov.hmrc.play.frontend.auth.{AuthenticationProviderIds, AuthContext}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{CredentialStrength, ConfidenceLevel, Accounts, Authority}
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

class RevocationSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val authority = Authority(s"Test User", Accounts(), None, None, CredentialStrength.Strong, ConfidenceLevel.L50)
  val headerCarrier = HeaderCarrier()

  lazy val loggedOutRequest = FakeRequest()
  lazy val loggedInRequest = FakeRequest().withSession(
    SessionKeys.sessionId -> "SessionId",
    SessionKeys.token -> "Token",
    SessionKeys.userId -> "Test User",
    SessionKeys.authProvider -> GovernmentGatewayId
  )

  val underTest = new Revocation {
    implicit val hc = headerCarrier
    override val authConnector: AuthConnector = mock[AuthConnector]
    given(authConnector.currentAuthority(any())).willReturn(Some(authority))
  }

  "Start" should {
    "return 200" in {

      val result = underTest.start(loggedOutRequest)

      status(result) shouldBe Status.OK
    }
  }

  "listAuthorizedApplications" should {
    "return 200 when the user is logged in" in {

      val result = underTest.listAuthorizedApplications(loggedInRequest)

      status(result) shouldBe Status.OK
    }

    "redirect to the login page when the user is not logged in" in {

      val result = underTest.listAuthorizedApplications(loggedOutRequest)

      status(result) shouldBe 303
      result.header.headers("Location") shouldEqual s"http://localhost:9025/gg/sign-in?continue=http://localhost:9686/api-revocation/applications"
    }

  }


}