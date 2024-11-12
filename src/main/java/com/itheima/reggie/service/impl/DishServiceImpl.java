package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /*
    * 新增菜品
    * */
    // 由于涉及多个表所以要添加事务 （还要在启动类开启事务支持）
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品基本信息到dish表
        this.save(dishDto);

        // 获取dishID
        Long dishID = dishDto.getId();

        List<DishFlavor> flavors = dishDto.getFlavors();
        // 遍历flavors为每个DishFlavor绑定dishID
        flavors = flavors.stream().map((item) ->{
            item.setDishId(dishID);
            return item;
        }).collect(Collectors.toList());

        // 调用dishFlavorService保存信息到dish_flavor表中
        dishFlavorService.saveBatch(flavors);
    }

    /*
    * 根据id
    * 查询菜品信息和口味信息
    * */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();

        // 从dish表中 查询菜品信息
        Dish dish = this.getById(id);
        // 设置基本菜品信息
        BeanUtils.copyProperties(dish, dishDto);

        // 从dish_flavor表中 查询当前菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
        // 设置口味信息
        dishDto.setFlavors(dishFlavorList);

        return dishDto;
    }

    /*
    * 更新菜品
    * */
    @Transactional
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 在dish表中 更新dish表基本信息
        this.updateById(dishDto);

        // 在dish_flavor表中 清理当前菜品对应口味数据
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);

        // ----------------  添加当前口味数据
        List<DishFlavor> dishFlavorList = dishDto.getFlavors();
        dishFlavorList = dishFlavorList.stream().map((dishFlavor) ->{
            dishFlavor.setDishId(dishDto.getId());
            return dishFlavor;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavorList);
    }

    // 删除菜品 同时删除对应的口味信息
    @Transactional
    @Override
    public void removeWithFlavor(List<Long> ids) {
        // 判断菜品状态
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus, 1);

        int count = this.count(dishLambdaQueryWrapper);

        // 不能删除
        if(count > 0){
            throw new CustomException("有菜品正在销售中，不能删除");
        }

        // 可以删除
        // 删除菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);

        // 删除菜品信息
        this.removeByIds(ids);
    }

    // 停/启售菜品
    @Override
    public void updateStatus(List<Long> ids) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId, ids);
        List<Dish> dishList = this.list(lambdaQueryWrapper);

        for (Dish dish : dishList) {
            dish.setStatus(dish.getStatus() == 0 ? 1 : 0);
            this.updateById(dish);
        }
    }
}
