package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ResponseBody
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
public class GlobalExceptionHandler {
    /*
    * sql异常处理
    * */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){
        log.error(exception.getMessage());

        if(exception.getMessage().contains("Duplicate entry")){
            String[] splits = exception.getMessage().split(" ");
            String msg = splits[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /*
    * 自定义异常处理
    * */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException exception){
        log.error(exception.getMessage());
        return R.error(exception.getMessage());
    }
}
