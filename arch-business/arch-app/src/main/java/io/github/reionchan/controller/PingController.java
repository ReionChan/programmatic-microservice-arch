package io.github.reionchan.controller;

import io.github.reionchan.AppBootstrap;
import io.github.reionchan.dto.R;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

import static io.github.reionchan.consts.Role.ADMIN;
import static io.github.reionchan.consts.Role.USER;

/**
 * @author Reion
 * @date 2024-06-01
 **/
@Slf4j
@RestController
@Tag(name = "PingController", description = "测试服务请求端点")
public class PingController {

    private final AttributeKey<String> ATTR_METHOD = AttributeKey.stringKey("method");

    private final Random random = new Random();
    private final Tracer tracer;
    private final LongHistogram doWorkHistogram;

    @Autowired
    public PingController(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(AppBootstrap.class.getName());
        Meter meter = openTelemetry.getMeter(AppBootstrap.class.getName());
        doWorkHistogram = meter.histogramBuilder("do-work").ofLongs().build();
    }

    @GetMapping("/ping")
    @PreAuthorize("hasAnyRole('" + USER + "','" + ADMIN + "')")
    @Operation(summary = "ping 方法", description = "测试接口",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "R 传输对象",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    )
    public R<String> ping() throws InterruptedException {
        int sleepTime = random.nextInt(200);
        doWork(sleepTime);
        doWorkHistogram.record(sleepTime, Attributes.of(ATTR_METHOD, "ping"));
        return R.success("pong");
    }

    private void doWork(int sleepTime) throws InterruptedException {
        Span span = tracer.spanBuilder("doWork").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            Thread.sleep(sleepTime);
            log.info("A sample log message!");
        } finally {
            span.end();
        }
    }
}
