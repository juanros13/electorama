package com.electorama.msvc.polls.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
public class FeignClientInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

    if (requestAttributes != null) {
      final HttpServletRequest httpServletRequest =
          ((ServletRequestAttributes) requestAttributes).getRequest();
      // log.info(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
      // log.info(httpServletRequest.getHeader("X-Auth-Id"));
      requestTemplate.header(
          HttpHeaders.AUTHORIZATION, httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
      requestTemplate.header("X-Auth-Id", httpServletRequest.getHeader("X-Auth-Id"));
    }
  }
}
