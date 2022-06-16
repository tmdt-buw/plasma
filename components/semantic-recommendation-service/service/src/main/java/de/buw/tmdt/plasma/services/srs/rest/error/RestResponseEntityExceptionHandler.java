package de.buw.tmdt.plasma.services.srs.rest.error;

import de.buw.tmdt.plasma.datamodel.error.ModelError;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass - this allows explicit configuration for this logger as part of the io.hotsprings name space")
	private static final Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

	@NotNull
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			@NotNull HttpMessageNotReadableException ex,
			@NotNull HttpHeaders headers,
			@NotNull HttpStatus status,
			@NotNull WebRequest request
	) {
        logger.warn("HTTP Message not readable", ex);
        ModelError modelError = new ModelError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(modelError, HttpStatus.valueOf(modelError.getStatus()));
    }

	@NotNull
	@ExceptionHandler(value = {ResponseStatusException.class})
	public ResponseEntity<Object> handleResponseStatusException(@NotNull ResponseStatusException ex) {
        logger.warn("A response Status Exception occurred", ex);
        ModelError modelError = new ModelError(ex.getStatus().value(), ex.getReason(), ex.getMessage());
        return new ResponseEntity<>(modelError, HttpStatus.valueOf(modelError.getStatus()));
    }

	@NotNull
	@ExceptionHandler(value = {Exception.class})
	public ResponseEntity<Object> handleGenericException(@NotNull Exception ex) {
        logger.warn("Unhandled Exception", ex);
        ModelError modelError = new ModelError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unknown Exception", ex.getMessage());
        return new ResponseEntity<>(modelError, HttpStatus.valueOf(modelError.getStatus()));
    }
}