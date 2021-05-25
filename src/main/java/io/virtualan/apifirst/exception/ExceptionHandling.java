package io.virtualan.apifirst.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.web.advice.general.GeneralAdviceTrait;
import org.zalando.problem.spring.web.advice.http.HttpAdviceTrait;
import org.zalando.problem.spring.web.advice.io.IOAdviceTrait;
import org.zalando.problem.spring.web.advice.routing.RoutingAdviceTrait;
import org.zalando.problem.spring.web.advice.validation.ValidationAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;

/**
 * Handles all exceptions thrown out of RestControllers.
 * It converts the exception into a Json Problem object (done by zalando's problem-spring-web library)
 * and logs the exception, in case of json logstash logging enriched with Markers containing addtional
 * request information like path, session id and tracking id.
 */
@ControllerAdvice
public class ExceptionHandling implements
        GeneralAdviceTrait,
        HttpAdviceTrait,
        IOAdviceTrait,
        RoutingAdviceTrait,
        ValidationAdviceTrait {

    private static final String PROBLEM_BASE_URL = "https://www.virtualan.io";
    private static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    private static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");

    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null || request == null) {
            return new ResponseEntity<>(Problem.builder().withTitle("either entity or request is null!").build(),
                    new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!(entity.getBody() instanceof ConstraintViolationProblem || entity.getBody() instanceof DefaultProblem)) {
            return entity;
        }

        ThrowableProblem problem = (ThrowableProblem) entity.getBody();
        if (problem == null) {
            return new ResponseEntity<>(Problem.builder().withTitle("entity body is null!").build(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String path;
        HttpServletRequest nativeRequest = request.getNativeRequest(HttpServletRequest.class);
        if (nativeRequest != null) {
            path = nativeRequest.getRequestURI();
        } else {
            path = "unknown";
        }

        ThrowableProblem apiProblem = Problem.builder()
                .withType(problem.getType())
                .withStatus(problem.getStatus())
                .withTitle(problem.getTitle())
                .withDetail(problem.getDetail())
                .withInstance(Optional.ofNullable(problem.getInstance()).orElse(URI.create(path)))
                .build();

        return new ResponseEntity<>(apiProblem, entity.getHeaders(), entity.getStatusCode());
    }

    @Override
    public URI defaultConstraintViolationType() {
        return CONSTRAINT_VIOLATION_TYPE;
    }

    @Override
    public void log(@Nullable Throwable throwable, Problem problem, NativeWebRequest request, HttpStatus status) {
        Logger log = LoggerFactory.getLogger(getLoggerName(throwable));
        assert throwable != null;
        log.error(problem.getDetail() != null ? problem.getDetail() : throwable.getMessage(), throwable);
    }

    /**
     * picks the name of first class in the stackstrace as logger name.
     * if none is present, it chooses ExceptionHandling class name as logger name
     */
    private String getLoggerName(Throwable t) {
        return t != null && t.getStackTrace() != null && t.getStackTrace().length > 0
                && t.getStackTrace()[0] != null ? t.getStackTrace()[0].getClassName() : getClass().getName();
    }
}