package controllers;

import operators.ErrorOperator;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import responses.ErrorsResponse;
import entities.Error;
import entities.Status;

/**
 * Errors apis.
 * 
 * @author ruochen.xu
 *
 */
@RestController
public class ErrorController {
	private static final ErrorOperator ERROR = ErrorOperator.INSTANCE;

	/**
	 * Get specified errors.
	 * 
	 * @return Status and Error.
	 */
	@RequestMapping("/api/errors/{id}")
	public ErrorsResponse getError(@PathVariable(value = "id") Integer id) {
		Error error = ERROR.getError(id);
		Status status = new Status(error != null);
		return new ErrorsResponse(status, error);
	}
}
