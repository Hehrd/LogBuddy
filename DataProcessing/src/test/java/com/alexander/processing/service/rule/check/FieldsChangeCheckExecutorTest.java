package com.alexander.processing.service.rule.check;

import com.alexander.processing.model.dto.LogEntryDTO;
import com.alexander.processing.model.log.LogTraceSession;
import com.alexander.processing.model.rule.check.StringValueCheck;
import com.alexander.processing.model.rule.check.trace.FieldsChangeCheck;
import com.alexander.processing.model.rule.check.trace.TraceCheckStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldsChangeCheckExecutorTest {
    private final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

    FieldsChangeCheckExecutorTest() {
        applicationContext.register(StringValueCheckExecutor.class, FieldsChangeCheckExecutor.class);
        applicationContext.refresh();
    }

    @AfterEach
    void tearDown() {
        applicationContext.close();
    }

    @Test
    void requiresAllFieldTransitionsToMatchTheSameComparedPair() {
        FieldsChangeCheckExecutor executor = applicationContext.getBean(FieldsChangeCheckExecutor.class);
        LogTraceSession traceSession = buildTraceSession(
                log("user-a", "pending"),
                log("user-b", "pending"),
                log("user-b", "approved")
        );

        FieldsChangeCheck check = new FieldsChangeCheck(
                Map.of(
                        "userId", new FieldsChangeCheck.FieldTransitionCheck(
                                FieldsChangeCheck.FieldChangeMode.CHANGED,
                                stringCheck("userId", "user-b"),
                                stringCheck("userId", "user-a")
                        ),
                        "status", new FieldsChangeCheck.FieldTransitionCheck(
                                FieldsChangeCheck.FieldChangeMode.CHANGED,
                                stringCheck("status", "approved"),
                                stringCheck("status", "pending")
                        )
                ),
                TraceCheckStrategy.COMPARE_TO_PREVIOUS_EVENT
        );

        assertFalse(executor.executeCheck(check, traceSession));
    }

    @Test
    void passesWhenOneComparedPairSatisfiesAllTransitions() {
        FieldsChangeCheckExecutor executor = applicationContext.getBean(FieldsChangeCheckExecutor.class);
        LogTraceSession traceSession = buildTraceSession(
                log("user-a", "pending"),
                log("user-b", "approved")
        );

        FieldsChangeCheck check = new FieldsChangeCheck(
                Map.of(
                        "userId", new FieldsChangeCheck.FieldTransitionCheck(
                                FieldsChangeCheck.FieldChangeMode.CHANGED,
                                stringCheck("userId", "user-b"),
                                stringCheck("userId", "user-a")
                        ),
                        "status", new FieldsChangeCheck.FieldTransitionCheck(
                                FieldsChangeCheck.FieldChangeMode.CHANGED,
                                stringCheck("status", "approved"),
                                stringCheck("status", "pending")
                        )
                ),
                TraceCheckStrategy.COMPARE_TO_PREVIOUS_EVENT
        );

        assertTrue(executor.executeCheck(check, traceSession));
    }

    private static LogTraceSession buildTraceSession(LogEntryDTO... entries) {
        LogTraceSession session = new LogTraceSession("trace-1", Map.of());
        for (LogEntryDTO entry : entries) {
            session.addLog(entry);
        }
        return session;
    }

    private static LogEntryDTO log(String userId, String status) {
        return new LogEntryDTO(
                "",
                "trace-1",
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Map.of("userId", userId, "status", status)
        );
    }

    private static StringValueCheck stringCheck(String fieldName, String expectedValue) {
        return new StringValueCheck(Map.of(
                fieldName,
                new StringValueCheck.StringValueInfo(expectedValue, null, 0, 0)
        ));
    }
}
