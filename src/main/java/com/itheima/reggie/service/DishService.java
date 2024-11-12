package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    // 新增菜品 同时插入菜品对应的口味数据 需要操作dish、dish_flavor两张表
    void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    // 更新菜品 同时更新对应的口味信息
    void updateWithFlavor(DishDto dishDto);

    // 删除菜品 同时删除对应的口味信息
    void removeWithFlavor(List<Long> ids);

    // 停/启售菜品
    void updateStatus(List<Long> ids);
}
