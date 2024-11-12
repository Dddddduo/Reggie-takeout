package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /*
    * 新增套餐
    * 同时需要保存套餐和菜品的关系
    * */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("保存套餐成功");
    }

    /*
    * 分页查询
    * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        Page<Setmeal> setmealPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(name != null, Setmeal::getName, name);

        setmealService.page(setmealPage, lambdaQueryWrapper);

        // 转换成SetmealDtoPage
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);
        // 对象拷贝
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        // 获取原本的records
        List<Setmeal> setmealList = setmealPage.getRecords();

        // 转化成dto的records -- 给分类名称赋值
        List<SetmealDto> setmealDtoList =  setmealList.stream().map((setmeal) ->{
            // 根据分类id查询分类
            Long categoryID = setmeal.getCategoryId();
            Category category = categoryService.getById(categoryID);

            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);

            if(category != null){
                setmealDto.setCategoryName(category.getName());
            }

            return setmealDto;

        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);

        return R.success(setmealDtoPage);
    }

    /*
    * 删除套餐
    * */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("要删除的套餐的id为：",ids);
        setmealService.removeWithDish(ids);
        return R.success("删除套餐成功");
    }

    /*
    * 信息回显
    * */
    @GetMapping("/{id}")
    public R<SetmealDto> update(@PathVariable Long id){
        log.info("信息回显id为{}的套餐信息",id);
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        log.info("套餐信息为：",setmealDto);
        return R.success(setmealDto);
    }

    /*
    * 修改套餐
    * */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("修改套餐为{}", setmealDto);
        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功");
    }

    /*
    * 停售套餐
    * */
    @PostMapping("/status/0")
    public R<String> status0(@RequestParam List<Long> ids){
        log.info("停售套餐id为{}",ids);
        setmealService.updateStatus(ids);
        return R.success("停售成功");

    }

    /*
     * 启售套餐
     * */
    @PostMapping("/status/1")
    public R<String> status1(@RequestParam List<Long> ids){
        log.info("停售套餐id为{}",ids);
        setmealService.updateStatus(ids);
        return R.success("启售成功");

    }

    /*
    * 根据条件
    * 查询套餐
    * */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        lambdaQueryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());

        List<Setmeal> setmealList = setmealService.list(lambdaQueryWrapper);

        return R.success(setmealList);
    }
}
