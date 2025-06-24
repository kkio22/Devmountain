import static net.grinder.script.Grinder.grinder
import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import net.grinder.script.GTest
import net.grinder.script.Grinder
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import org.ngrinder.http.HTTPRequest
import org.ngrinder.http.HTTPRequestControl
import org.ngrinder.http.HTTPResponse

@RunWith(GrinderRunner)
class SignUpAndLoginTestRunner {

	public static GTest test
	public static HTTPRequest request
	public static Map<String, String> headers = [:]
	public static String baseUrl = "http://172.25.0.30:8080"

	@BeforeProcess
	public static void beforeProcess() {
		HTTPRequestControl.setConnectionTimeout(300000)
		// HTTPRequestControl.setFollowRedirects() 메서드가 없으므로 제거
		test = new GTest(1, "Signup + Login Performance Test")
		request = new HTTPRequest()
		grinder.logger.info("Signup + Login performance test initialized for: ${baseUrl}")
	}

	@BeforeThread
	public void beforeThread() {
		test.record(this, "test")
		grinder.statistics.delayReports = true
		grinder.logger.info("Thread ${grinder.threadNumber} initialized")
	}

	@Before
	public void before() {
		// Clear and set headers
		headers.clear()
		headers["Content-Type"] = "application/json"
		headers["Accept"] = "application/json"
		headers["User-Agent"] = "nGrinder Performance Test"
		request.setHeaders(headers)

		grinder.logger.info("Headers initialized for thread ${grinder.threadNumber}")
	}

	@Test
	public void test() {
		try {
			// 스레드별 고유 사용자 데이터 생성
			def timestamp = System.currentTimeMillis()
			def uniqueEmail = "testuser${grinder.threadNumber}_${timestamp}@example.com"
			def password = "Test1234!"
			def userName = "TestUser${grinder.threadNumber}"
			def phoneNumber = "010-${String.format('%04d', grinder.threadNumber)}-${String.format('%04d', timestamp % 10000)}"
			
			// 1단계: 회원가입 API 호출
			def signupData = [
				email: uniqueEmail,
				password: password,
				name: userName,
				phoneNumber: phoneNumber,
				role: "ADMIN"
			]
			
			def signupJson = groovy.json.JsonOutput.toJson(signupData)
			grinder.logger.info("Thread ${grinder.threadNumber}: Starting signup for ${uniqueEmail}")
			
			HTTPResponse signupResponse = request.POST("${baseUrl}/users/signup", signupJson.getBytes("UTF-8"))
			
			grinder.logger.info("Signup Test - Thread: {}, Status: {}", 
				grinder.threadNumber, signupResponse.statusCode)

			// 회원가입 응답 검증
			if (signupResponse.statusCode == 200 || signupResponse.statusCode == 201) {
				grinder.logger.info("Signup successful for thread ${grinder.threadNumber}")
			} else if (signupResponse.statusCode == 400 || signupResponse.statusCode == 409) {
				grinder.logger.warn("Signup failed (expected - duplicate or validation error) for thread ${grinder.threadNumber}")
			} else if (signupResponse.statusCode >= 500) {
				grinder.logger.error("Signup server error for thread ${grinder.threadNumber}: ${signupResponse.statusCode}")
			} else {
				grinder.logger.warn("Unexpected signup response for thread ${grinder.threadNumber}: ${signupResponse.statusCode}")
			}

			// 2단계: 로그인 API 호출 (회원가입과 동일한 이메일/비밀번호 사용)
			def loginData = [
				email: uniqueEmail,
				password: password
			]
			
			def loginJson = groovy.json.JsonOutput.toJson(loginData)
			grinder.logger.info("Thread ${grinder.threadNumber}: Starting login for ${uniqueEmail}")

			HTTPResponse loginResponse = request.POST("${baseUrl}/users/login", loginJson.getBytes("UTF-8"))
			
			grinder.logger.info("Login Test - Thread: {}, Status: {}", 
				grinder.threadNumber, loginResponse.statusCode)

			// 로그인 응답 검증
			if (loginResponse.statusCode == 200) {
				grinder.logger.info("Login successful for thread ${grinder.threadNumber}")
			} else if (loginResponse.statusCode == 401 || loginResponse.statusCode == 400) {
				grinder.logger.info("Login rejected for thread ${grinder.threadNumber}")
			} else if (loginResponse.statusCode >= 500) {
				grinder.logger.error("Login server error for thread ${grinder.threadNumber}: ${loginResponse.statusCode}")
			} else {
				grinder.logger.warn("Unexpected login response for thread ${grinder.threadNumber}: ${loginResponse.statusCode}")
			}

		} catch (Exception e) {
			grinder.logger.error("Test execution error for thread ${grinder.threadNumber}: ${e.getMessage()}")
			fail("Test execution failed: ${e.getMessage()}")
		}
	}
} 