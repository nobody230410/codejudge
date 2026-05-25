package com.yanzy.codejudge.common.exception;

public class BusinessException extends RuntimeException {
   private final ErrorCodeEnum errorCode;
   private final String message;
   public BusinessException(ErrorCodeEnum errorCode) {
       super(errorCode.getMessage());
       this.errorCode = errorCode;
       this.message = errorCode.getMessage();
   }

   public BusinessException(ErrorCodeEnum errorCode, String message) {
       super(message);
       this.errorCode = errorCode;
       this.message = message;
   }

    public ErrorCodeEnum getErrorCode() { return errorCode; }

    public String getMessage() {return message; }

}
