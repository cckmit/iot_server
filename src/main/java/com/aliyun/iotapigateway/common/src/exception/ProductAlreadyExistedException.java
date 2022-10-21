package com.aliyun.iotx.haas.tdserver.common.exception;

import java.io.Serializable;

/**
 * @author benxiliu
 * @date 2020/09/01
 */

public class ProductAlreadyExistedException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1986188761838057647L;

    public ProductAlreadyExistedException() {

    }

    public ProductAlreadyExistedException(String message) {
        super(message);
    }

    public ProductAlreadyExistedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductAlreadyExistedException(Throwable cause) {
        super(cause);
    }
}

