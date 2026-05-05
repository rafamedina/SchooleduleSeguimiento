package com.tfg.schooledule.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimitFilterTest {

  @Test
  void oncePerRequestFilter_primeros10IntentosPermitidos_11esRetorna429() throws Exception {
    LoginRateLimitFilter filter = new LoginRateLimitFilter();

    for (int i = 1; i <= 10; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
      request.setServletPath("/login");
      request.setRemoteAddr("192.168.1.1");
      MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilterInternal(request, response, new MockFilterChain());
      assertThat(response.getStatus()).isNotEqualTo(429);
    }

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
    request.setServletPath("/login");
    request.setRemoteAddr("192.168.1.1");
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilterInternal(request, response, new MockFilterChain());
    assertThat(response.getStatus()).isEqualTo(429);
  }

  @Test
  void diferentesIPs_tienenBucketsSeparados() throws Exception {
    LoginRateLimitFilter filter = new LoginRateLimitFilter();

    for (int i = 0; i < 10; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
      request.setServletPath("/login");
      request.setRemoteAddr("10.0.0.1");
      filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());
    }

    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
    request.setServletPath("/login");
    request.setRemoteAddr("10.0.0.2");
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilterInternal(request, response, new MockFilterChain());
    assertThat(response.getStatus()).isNotEqualTo(429);
  }

  @Test
  void getRequests_noConsumenBucket() throws Exception {
    LoginRateLimitFilter filter = new LoginRateLimitFilter();

    for (int i = 0; i < 11; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
      request.setServletPath("/login");
      request.setRemoteAddr("10.0.0.3");
      MockHttpServletResponse response = new MockHttpServletResponse();
      filter.doFilterInternal(request, response, new MockFilterChain());
      assertThat(response.getStatus()).isNotEqualTo(429);
    }
  }
}
