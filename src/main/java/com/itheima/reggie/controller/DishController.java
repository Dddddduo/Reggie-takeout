package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /*
    * 新增菜品
    * */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("新增菜品信息:{}", dishDto);
        dishService.saveWithFlavor(dishDto);

        // 清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        return R.success("新增菜品成功");
    }

    /*
    * 分页查询
    * */
    @GetMapping("/page")
    public R<Page> list(int page, int pageSize, String name) {
        // 分页构造器对象
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dtoPageInfo = new Page<>(page, pageSize);
        // 条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        lambdaQueryWrapper.like(name != null, Dish::getName, name);
        // 添加排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        // 执行分页查询
        dishService.page(pageInfo, lambdaQueryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPageInfo, "records");

        // 给dtoPageInfo的records赋值
        List<Dish> dishRecords = pageInfo.getRecords();
        List<DishDto> dishDtoRecords = dishRecords.stream().map((dish) ->{
            // 遍历dishRecords中的每一个dish 将基本属性拷贝给dishDto
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);

            // 获取当前分类ID
            Long categoryID = dish.getCategoryId();
            // 根据ID查询当前分类
            Category category = categoryService.getById(categoryID);
            dishDto.setCategoryName(category.getName());

            return dishDto;
        }).collect(Collectors.toList());

        // 设置为dtoPageInfo的records
        dtoPageInfo.setRecords(dishDtoRecords);

        return R.success(dtoPageInfo);
    }


    /*
    * 信息回显
    * */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        log.info("回显菜品id为{}的信息", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /*
     * 修改菜品
     * */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("修改的菜品信息为:{}", dishDto);
        dishService.updateWithFlavor(dishDto);

        // 清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        // 精确清理缓存
//        String key = "dish_" + dishDto.getCategoryId() + "_1";
//        Set keys = redisTemplate.keys(key);
//        redisTemplate.delete(keys);

        return R.success("修改菜品成功");
    }

    /*
    * 根据条件
    * 查询菜品
    * */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtos = null;

        // 动态构造key 不同分类的key不一样
        String key = "dish_"+ dish.getCategoryId() + "_" + dish.getStatus();

        // 先从redis中获取缓存数据
        dishDtos = (List<DishDto>) redisTemplate.opsForValue().get(key);

        // 如果存在则直接返回无需查询
        if(dishDtos != null){
            return R.success(dishDtos);
        }

        // 如果不存在则查询数据库并将查询到的菜品数据缓存到redis

        log.info("查询分类id为{}的菜品信息", dish.getCategoryId());
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);

        List<Dish> list = dishService.list(lambdaQueryWrapper);

        // 前端调用该接口展示菜品 需要使用DishDto返回给前端菜品口味信息

        dishDtos = list.stream().map( (item) -> {
            // 先创建一个DishDto对象
            DishDto dishDto = new DishDto();

            // 拷贝基本信息
            BeanUtils.copyProperties(item, dishDto);

            // 设置分类名称
            Category category = categoryService.getById(item.getCategoryId());
            dishDto.setCategoryName(category.getName());

            // 获取菜品信息
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, item.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        // 将查询到的菜品数据缓存到redis
        redisTemplate.opsForValue().set(key, dishDtos, 60, TimeUnit.MINUTES);

        return R.success(dishDtos);
    }

    /*
    * 删除菜品
    * */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("删除id为{}的菜品", ids);
        dishService.removeWithFlavor(ids);
        return R.success("删除菜品成功");
    }

    /*
    * 停售商品
    * */
    @PostMapping("/status/0")
    public R<String> status0(@RequestParam List<Long> ids){
        dishService.updateStatus(ids);
        return R.success("禁售菜品成功");
    }

    /*
    * 启售商品
    * */
    @PostMapping("/status/1")
    public R<String> status1(@RequestParam List<Long> ids){
        dishService.updateStatus(ids);
        return R.success("启售菜品成功");
    }
}
