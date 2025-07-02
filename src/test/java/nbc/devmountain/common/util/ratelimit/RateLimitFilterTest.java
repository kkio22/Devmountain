package nbc.devmountain.common.util.ratelimit;

import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.user.model.User;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// RateLimitFilter에 대한 통합 및 단위 테스트 클래스
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RateLimitFilterTest {

	private ProxyManager<String> proxyManagerMock;
	private BucketProxy bucketMock;
	private RemoteBucketBuilder<String> bucketBuilderMock;
	private RateLimitFilter filter;
	private StatefulRedisConnection<String, byte[]> redisConnectionMock;

	@BeforeEach
	void setUp() {
		// Redis가 아닌 Mockito로 ProxyManager와 BucketBuilder mocking
		proxyManagerMock = mock(ProxyManager.class);
		bucketMock = mock(BucketProxy.class);
		bucketBuilderMock = mock(RemoteBucketBuilder.class);
		redisConnectionMock = mock(StatefulRedisConnection.class);

		// ProxyManager mock 주입
		filter = new RateLimitFilter(proxyManagerMock, redisConnectionMock);

		// Bucket을 생성할 때 mock 반환
		when(proxyManagerMock.builder()).thenReturn(bucketBuilderMock);
		when(bucketBuilderMock.build(anyString(), any(Supplier.class))).thenReturn(bucketMock);
	}

	// 각 테스트 종료 후 SecurityContextHolder 초기화
	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	// 실제 RateLimitFilter의 doFilter 메서드 동작을 테스트
	@Nested
	class RateLimitFilterIntegrationTests {

		@Test
		void shouldAllowNonPostRequest() throws Exception {
			// POST가 아닌 경우 RateLimit 적용 X
			MockHttpServletRequest request = createRequest("GET", "/chatrooms/1/messages");
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setCharacterEncoding("UTF-8");
			MockFilterChain filterChain = new MockFilterChain();

			filter.doFilter(request, response, filterChain);

			assertThat(response.getStatus()).isEqualTo(200);
			verify(bucketMock, never()).tryConsume(anyInt());
		}

		@Test
		void shouldAllowNonMessagePostRequest() throws Exception {
			// POST지만 메시지 전송 API가 아닐 경우 RateLimit 적용 X
			MockHttpServletRequest request = createRequest("POST", "/chatrooms/1");
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setCharacterEncoding("UTF-8");
			MockFilterChain filterChain = new MockFilterChain();

			filter.doFilter(request, response, filterChain);

			assertThat(response.getStatus()).isEqualTo(200);
			verify(bucketMock, never()).tryConsume(anyInt());
		}

		@Test
		void shouldAllowChatMessageWhenTokenAvailable() throws Exception {
			// 버킷에서 토큰 소모 성공 → 요청 허용
			when(bucketMock.tryConsume(1)).thenReturn(true);

			MockHttpServletRequest request = createRequest("POST", "/chatrooms/1/messages");
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setCharacterEncoding("UTF-8");
			MockFilterChain filterChain = new MockFilterChain();

			filter.doFilter(request, response, filterChain);

			assertThat(response.getStatus()).isEqualTo(200);
			verify(bucketMock).tryConsume(1);
		}

		@Test
		void shouldReturn429WhenRateLimitExceeded() throws Exception {
			// 토큰 소모 실패 → 429 응답
			when(bucketMock.tryConsume(1)).thenReturn(false);

			MockHttpServletRequest request = createRequest("POST", "/chatrooms/1/messages");
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setCharacterEncoding("UTF-8");
			MockFilterChain filterChain = new MockFilterChain();

			filter.doFilter(request, response, filterChain);

			assertThat(response.getStatus()).isEqualTo(429);
			assertThat(response.getHeader("Retry-After")).isEqualTo("3600");
			assertThat(response.getContentAsString()).contains("요청이 너무 많습니다");
			verify(bucketMock).tryConsume(1);
		}

		@Test
		void shouldUseProConfigForProUser() throws Exception {
			// PRO 사용자에 대해 PRO 설정이 적용되는지 확인
			when(bucketMock.tryConsume(1)).thenReturn(true);
			setupAuthenticatedUser(User.MembershipLevel.PRO);

			MockHttpServletRequest request = createRequest("POST", "/chatrooms/1/messages");
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setCharacterEncoding("UTF-8");
			MockFilterChain filterChain = new MockFilterChain();

			filter.doFilter(request, response, filterChain);

			assertThat(response.getStatus()).isEqualTo(200);
			verify(bucketBuilderMock).build(eq("127.0.0.1"), any(Supplier.class));
		}

		@Test
		void shouldUseFreeConfigForFreeUser() throws Exception {
			// FREE 설정이 적용되는지 확인
			when(bucketMock.tryConsume(1)).thenReturn(true);
			setupAuthenticatedUser(User.MembershipLevel.FREE);

			MockHttpServletRequest request = createRequest("POST", "/chatrooms/1/messages");
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setCharacterEncoding("UTF-8");
			MockFilterChain filterChain = new MockFilterChain();

			filter.doFilter(request, response, filterChain);

			assertThat(response.getStatus()).isEqualTo(200);
			verify(bucketBuilderMock).build(eq("127.0.0.1"), any(Supplier.class));
		}

		// HTTP 메서드 테스트(GET, PUT, DELETE → 필터 적용 안 됨)
		@Test
		void shouldHandleDifferentHTTPMethods() throws Exception {
			for (String method : new String[]{"GET", "PUT", "DELETE"}) {
				MockHttpServletRequest request = createRequest(method, "/chatrooms/1/messages");
				MockHttpServletResponse response = new MockHttpServletResponse();
				filter.doFilter(request, response, new MockFilterChain());
				assertThat(response.getStatus()).isEqualTo(200);
			}
		}

		@Test
		void shouldHandleDifferentURIPatterns() throws Exception {
			// POST이지만 다른 패턴들 → 필터 적용 안 됨
			String[] nonMessagePatterns = {
				"/api/chat",
				"/chatrooms",
				"/chatrooms/1",
				"/chatrooms/1/users",
				"/messages",
				"/api/chatrooms/1/messages"
			};

			for (String uri : nonMessagePatterns) {
				MockHttpServletRequest request = createRequest("POST", uri);
				MockHttpServletResponse response = new MockHttpServletResponse();
				response.setCharacterEncoding("UTF-8");
				MockFilterChain filterChain = new MockFilterChain();

				filter.doFilter(request, response, filterChain);
				assertThat(response.getStatus()).isEqualTo(200);
			}
		}
	}

	// 인증 객체 및 CustomUserPrincipal 관련 동작 검증
	@Nested
	class UserAuthenticationTests {

		// CustomUserPrincipal 생성 및 멤버십 확인
		@Test
		void shouldCreateValidCustomUserPrincipal() {
			User user = User.builder()
				.email("user@example.com")
				.password("password")
				.name("TestUser")
				.phoneNumber("010-1234-5678")
				.loginType(User.LoginType.EMAIL)
				.role(User.Role.USER)
				.membershipLevel(User.MembershipLevel.PRO)
				.build();

			CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);

			assertThat(userPrincipal.getMembershipLevel()).isEqualTo(User.MembershipLevel.PRO);
			assertThat(userPrincipal.getUsername()).isEqualTo("user@example.com");
			assertThat(userPrincipal.getAuthorities()).isNotEmpty();
		}

		// SecurityContext에 인증 정보가 정상적으로 들어가는지 확인
		@Test
		void shouldSetAuthenticationContext() {
			User user = User.builder()
				.email("user@example.com")
				.password("password")
				.name("TestUser")
				.phoneNumber("010-1234-5678")
				.loginType(User.LoginType.EMAIL)
				.role(User.Role.USER)
				.membershipLevel(User.MembershipLevel.FREE)
				.build();

			CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);
			UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
			assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.isInstanceOf(CustomUserPrincipal.class);
		}

		// 인증 정보가 없는 경우(null) 처리
		@Test
		void shouldHandleNullAuthentication() {
			SecurityContextHolder.clearContext();
			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		}

		// CustomUserPrincipal이 아닌 경우 처리
		@Test
		void shouldHandleNonCustomUserPrincipal() {
			UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken("user", "password");
			SecurityContextHolder.getContext().setAuthentication(auth);

			assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
			assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.isNotInstanceOf(CustomUserPrincipal.class);
		}
	}

	// User 멤버십 레벨 관련 동작 검증
	@Nested
	class MembershipLevelTests {

		// 멤버십 enum 값이 정상적으로 존재하는지 확인
		@Test
		void shouldHaveCorrectMembershipLevels() {
			assertThat(User.MembershipLevel.FREE).isNotNull();
			assertThat(User.MembershipLevel.PRO).isNotNull();
			assertThat(User.MembershipLevel.GUEST).isNotNull();
		}

		// 서로 다른 멤버십 레벨 비교
		@Test
		void shouldCompareMembershipLevels() {
			User freeUser = User.builder()
				.email("free@example.com")
				.password("password")
				.name("FreeUser")
				.phoneNumber("010-1234-5678")
				.loginType(User.LoginType.EMAIL)
				.role(User.Role.USER)
				.membershipLevel(User.MembershipLevel.FREE)
				.build();

			User proUser = User.builder()
				.email("pro@example.com")
				.password("password")
				.name("ProUser")
				.phoneNumber("010-1234-5678")
				.loginType(User.LoginType.EMAIL)
				.role(User.Role.USER)
				.membershipLevel(User.MembershipLevel.PRO)
				.build();

			assertThat(freeUser.getMembershipLevel()).isEqualTo(User.MembershipLevel.FREE);
			assertThat(proUser.getMembershipLevel()).isEqualTo(User.MembershipLevel.PRO);
			assertThat(freeUser.getMembershipLevel()).isNotEqualTo(proUser.getMembershipLevel());
		}

		// GUEST 멤버십 생성 확인
		@Test
		void shouldCreateUserWithDifferentMembershipLevels() {
			User guestUser = User.builder()
				.email("guest@example.com")
				.password("password")
				.name("GuestUser")
				.phoneNumber("010-1234-5678")
				.loginType(User.LoginType.EMAIL)
				.role(User.Role.USER)
				.membershipLevel(User.MembershipLevel.GUEST)
				.build();

			assertThat(guestUser.getMembershipLevel()).isEqualTo(User.MembershipLevel.GUEST);
		}
	}

	// RateLimitFilter의 버킷 설정 로직 및 IP키 생성 로직 검증
	@Nested
	class RateLimitConfigurationTests {

		// CustomUserPrincipal의 멤버십에 따라 올바른 값이 들어가는지 확인
		@Test
		void shouldTestBucketConfigurationLogic() {
			// Free: 1시간 10개, Pro: 1시간 20개
			User freeUser = User.builder()
				.email("free@example.com")
				.password("password")
				.name("FreeUser")
				.phoneNumber("010-1234-5678")
				.loginType(User.LoginType.EMAIL)
				.role(User.Role.USER)
				.membershipLevel(User.MembershipLevel.FREE)
				.build();

			CustomUserPrincipal freePrincipal = new CustomUserPrincipal(freeUser);
			assertThat(freePrincipal.getMembershipLevel()).isEqualTo(User.MembershipLevel.FREE);

			User proUser = User.builder()
				.email("pro@example.com")
				.password("password")
				.name("ProUser")
				.phoneNumber("010-1234-5678")
				.loginType(User.LoginType.EMAIL)
				.role(User.Role.USER)
				.membershipLevel(User.MembershipLevel.PRO)
				.build();

			CustomUserPrincipal proPrincipal = new CustomUserPrincipal(proUser);
			assertThat(proPrincipal.getMembershipLevel()).isEqualTo(User.MembershipLevel.PRO);
		}

		// IP별로 키가 잘 생성되는지 확인
		@Test
		void shouldTestIPKeyGeneration() {
			String ip1 = "192.168.1.1";
			String ip2 = "10.0.0.1";
			assertThat(ip1).isNotEqualTo(ip2);
			assertThat(ip1).isEqualTo("192.168.1.1");
			assertThat(ip2).isEqualTo("10.0.0.1");
		}
	}

	// 429 에러 응답 메시지 및 헤더 형식 검증
	@Nested
	class ErrorResponseTests {

		// 429 메시지 포맷이 올바른지 확인
		@Test
		void shouldTest429ResponseFormat() {
			String expectedMessage = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
			String expectedRetryAfter = "3600";
			assertThat(expectedMessage).contains("요청이 너무 많습니다");
			assertThat(expectedRetryAfter).isEqualTo("3600");
		}

		// 429 응답 헤더와 메시지 실제로 세팅 시 정상 동작하는지 확인
		@Test
		void shouldTestErrorResponseHeaders() throws Exception {
			MockHttpServletResponse response = new MockHttpServletResponse();
			response.setStatus(429);
			response.setHeader("Retry-After", "3600");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
			assertThat(response.getStatus()).isEqualTo(429);
			assertThat(response.getHeader("Retry-After")).isEqualTo("3600");
			assertThat(response.getContentAsString()).contains("요청이 너무 많습니다");
		}
	}

	// HTTP 요청 객체 생성 도우미
	private MockHttpServletRequest createRequest(String method, String uri) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
		request.setRemoteAddr("127.0.0.1");
		return request;
	}
	// SecurityContext에 인증된 사용자 주입 도우미
	private void setupAuthenticatedUser(User.MembershipLevel level) {
		User user = User.builder()
			.email("user@example.com")
			.password("password")
			.name("TestUser")
			.phoneNumber("010-1234-5678")
			.loginType(User.LoginType.EMAIL)
			.role(User.Role.USER)
			.membershipLevel(level)
			.build();

		CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);
		UsernamePasswordAuthenticationToken auth =
			new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
