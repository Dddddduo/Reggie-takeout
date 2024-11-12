package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    // 新增套餐 同时需要保存套餐和菜品的关系
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {

        // 在表setmeal中 保存套餐的基本信息
        this.save(setmealDto);

        // 获取套餐中的菜品 这个集合
        List<SetmealDish>  setmealDishes = setmealDto.getSetmealDishes();

        // 给每个setmealDish的setmealID赋值
        setmealDishes = setmealDishes.stream().map((setmealDish) ->{
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());

        // 在表setmeal_dish表中 保存套餐和菜品的关联信息
        setmealDishService.saveBatch(setmealDishes);
    }

    // 删除套餐 同时删除套餐和菜品之间的关系
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        // 查询套餐状态 确定是否可以删除
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(setmealLambdaQueryWrapper);

        // 不可以删除
        if(count> 0){
            throw new CustomException("该套餐正在售卖中，不能删除");
        }

        // 可以删除
        // -------删除套餐表中的数据
        this.removeByIds(ids);

        // -------删除菜品与套餐之间的联系
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper =new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    // 根据id查询套餐信息以及对应菜品信息
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        SetmealDto setmealDto = new SetmealDto();

        // 套餐基本信息
        Setmeal setmeal = this.getById(id);
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 套餐菜品信息
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishes = setmealDishService.list(lambdaQueryWrapper);

        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    // 修改套餐 同时修改套餐和菜品之间的关系
    @Transactional
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        // 修改套餐基本信息
        this.updateById(setmealDto);

        // 修改关联的菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((setmealDish)->{
            setmealDish.setSetmealId(setmealDto.getId());
            return setmealDish;
        }).collect(Collectors.toList());


        // 先删除所有菜品
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lambdaQueryWrapper);

        // 再添加新的菜品
        setmealDishService.saveBatch(setmealDishes);
    }

    // 停/启售套餐
    @Override
    public void updateStatus(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId, ids);
        List<Setmeal> setmealList = this.list(lambdaQueryWrapper);
        for (Setmeal setmeal : setmealList) {
            setmeal.setStatus(setmeal.getStatus() == 0 ? 1 : 0);
            this.updateById(setmeal);
        }
    }
}
