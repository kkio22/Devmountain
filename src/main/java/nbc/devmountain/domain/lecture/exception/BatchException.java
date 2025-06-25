package nbc.devmountain.domain.lecture.exception;

import nbc.devmountain.common.exception.BaseException;
import nbc.devmountain.common.exception.ExceptionCode;

public class BatchException extends BaseException {
	public BatchException(ExceptionCode exceptionCode) {
		super(exceptionCode);
	}
}