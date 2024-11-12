package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    // 新增套餐 同时需要保存套餐和菜品的关系
    void saveWithDish(SetmealDto setmealDto);

    // 删除套餐 同时删除套餐和菜品之间的关系
    void removeWithDish(List<Long> ids);

    // 根据id查询套餐信息以及对应菜品信息
    SetmealDto getByIdWithDish(Long id);

    // 修改套餐 同时修改套餐和菜品之间的关系
    void updateWithDish(SetmealDto setmealDto);

    // 停/启售套餐
    void updateStatus(List<Long> ids);
}
