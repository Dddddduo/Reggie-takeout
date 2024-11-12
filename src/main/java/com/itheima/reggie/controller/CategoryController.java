package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /*
    * 新增分类
    * */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增分类：{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /*
    * 分页查询
    * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        // 分页构造器
        Page pageInfo = new Page(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 添加排序条件  根据sort升序排序;
        lambdaQueryWrapper.orderByAsc(Category::getSort);

        // 进行分页查询
        categoryService.page(pageInfo, lambdaQueryWrapper);

        // 返回分页构造器
        return R.success(pageInfo);
    }

    /*
    * 根据ID
    * 删除分类
    * */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除id为{}的分类", ids);
        categoryService.remove(ids);
        return R.success("删除分类成功");
    }

    /*
    * 根据ID
    * 修改分类
    * */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类{}的信息", category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }


    /*
    * 根据条件
    * 查询分类
    * */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        log.info("显示菜品分类：", category);
        // 条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加条件 先判断下category的type不为空
        lambdaQueryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        // 添加排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(lambdaQueryWrapper);
        return R.success(list);
    }
}
