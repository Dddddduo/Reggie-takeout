package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /*
    * 添加购物车
    * */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}", shoppingCart);

        // 设置用户id 指定当前是哪个用户的购物车数据
        Long userID = BaseContext.getCurrentID();
        shoppingCart.setUserId(userID);

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        // 判断是菜品还是套餐
        Long dishID = shoppingCart.getDishId();
        if(dishID != null){
            // 菜品
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, dishID);
        }else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        // 查询当前菜品或者套餐是否在购物车中
        // select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart shoppingCartOne =shoppingCartService.getOne(lambdaQueryWrapper);

        if(shoppingCartOne != null){
            // 在-----份数在原来的基础上+1
            Integer number = shoppingCartOne.getNumber() + 1;
            shoppingCartOne.setNumber(number);
            shoppingCartService.updateById(shoppingCartOne);
        }else {
            // 不在---则添加到购物车中，份数默认是1
            shoppingCartOne = shoppingCart;
            shoppingCartOne.setNumber(1);
            shoppingCartOne.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCartOne);
        }

        return R.success(shoppingCartOne);
    }

    /*
    * 购物车中数量减少
    * */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){


        // 设置用户id 指定当前是哪个用户的购物车数据
        Long userID = BaseContext.getCurrentID();
        shoppingCart.setUserId(userID);

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());

        // 判断是菜品还是套餐
        Long dishID = shoppingCart.getDishId();
        if(dishID != null){
            // 菜品
            lambdaQueryWrapper.eq(ShoppingCart::getDishId, dishID);
        }else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        // 查询当前菜品或者套餐
        // select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart shoppingCartOne =shoppingCartService.getOne(lambdaQueryWrapper);

        if(shoppingCart == null) {
            throw new CustomException("请先添加该商品");
        }

        if(shoppingCartOne.getNumber() > 1){
            //份数在原来的基础上-1
            Integer number = shoppingCartOne.getNumber() - 1;
            shoppingCartOne.setNumber(number);
            shoppingCartService.updateById(shoppingCartOne);
        }else if (shoppingCartOne.getNumber() == 1){
            shoppingCartService.removeById(shoppingCartOne);
        }

        return R.success(shoppingCartOne);
    }

    /*
    * 查看购物车
    * */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentID());
        lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lambdaQueryWrapper);
        return R.success(shoppingCartList);
    }


    /*
    * 清空购物车
    * */
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentID());
        shoppingCartService.remove(lambdaQueryWrapper);

        return R.success("清空购物车成功");
    }



}
