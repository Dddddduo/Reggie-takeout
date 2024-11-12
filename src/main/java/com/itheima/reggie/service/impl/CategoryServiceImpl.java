package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long ids) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件 根据id查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int dishCount = dishService.count(dishLambdaQueryWrapper);

        // 查询当前分类是否关联了菜品
        // 已关联菜品--抛出业务异常
        if(dishCount > 0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件 根据id查询
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        // 查询当前分类是否关联了套餐
        // 已关联套餐--抛出业务异常
        if(setmealCount > 0){
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        // 未关联--正常调用
        super.removeById(ids);
    }
}
