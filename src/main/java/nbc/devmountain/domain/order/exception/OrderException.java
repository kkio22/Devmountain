package nbc.devmountain.domain.order.exception;

import nbc.devmountain.common.exception.BaseException;
import nbc.devmountain.common.exception.ExceptionCode;

public class OrderException extends BaseException {
    public OrderException(ExceptionCode code) {
        super(code);
    }
}
