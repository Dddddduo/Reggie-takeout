package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMataObjectHandler implements MetaObjectHandler {

    // 执行插入操作的时候自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insert自动填充...");
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentID());
        metaObject.setValue("updateUser", BaseContext.getCurrentID());
    }

    // 执行更新操作的时候自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("update自动填充user:{};time:{};",BaseContext.getCurrentID(), LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentID());
    }
}
