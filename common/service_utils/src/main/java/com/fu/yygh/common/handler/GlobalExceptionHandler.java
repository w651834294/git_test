package com.fu.yygh.common.handler;

import com.fu.yygh.common.result.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //全局统一异常处理
    @ExceptionHandler(value = Exception.class)
    public R error(Exception ex){
        ex.printStackTrace();
        return R.error().message("出现异常");
    }


    //特定异常处理
    @ExceptionHandler(value = ArithmeticException.class)
    public R error(ArithmeticException ex){
        ex.printStackTrace();
        return R.error().message("出现了算数异常");
    }

}
