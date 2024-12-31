package dev.jacobandersen.cams.auth.security;

import dev.jacobandersen.cams.auth.annotation.WithRateLimit;
import dev.jacobandersen.cams.auth.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Aspect
@Component
public class RateLimitAspect {
    private final Map<String, ExpiringMap<String, Bucket>> rateLimiters;

    public RateLimitAspect() {
        rateLimiters = new ConcurrentHashMap<>();
    }

    @Around("@annotation(dev.jacobandersen.cams.auth.annotation.WithRateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();
        final WithRateLimit annotation = method.getAnnotation(WithRateLimit.class);

        final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        final String remoteAddr = requestAttributes.getRequest().getRemoteAddr();
        final String discriminator = requestAttributes.getRequest().getHeader("X-Request-Discriminator");
        final String key = String.format("%s:%s", remoteAddr, discriminator == null ? "static" : discriminator);

        final Bucket bucket = rateLimiters
                .compute(annotation.id(), (k, v) -> v == null ? ExpiringMap.builder()
                        .expirationPolicy(ExpirationPolicy.ACCESSED)
                        .expiration(annotation.limitPeriodSeconds(), TimeUnit.SECONDS)
                        .build() : v)
                .compute(key, (k, v) -> v == null ? Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(annotation.allowedCalls())
                                .refillIntervally(
                                        annotation.allowedCalls(),
                                        Duration.ofSeconds(annotation.limitPeriodSeconds())
                                )
                                .build())
                        .build() : v);

        if (!bucket.tryConsume(1)) {
            throw new RateLimitException(remoteAddr);
        }

        return joinPoint.proceed();
    }
}
