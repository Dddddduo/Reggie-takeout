package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.LongAccumulator;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /*
    * 员工登录
    * */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        // 1.将页面提交的密码进行MD5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper= new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        // 3.如果没有查询到则返回登陆失败结果
        if(emp == null){
            return R.error("登录失败");
        }

        // 4.密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        // 5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        // 6.登录成功，将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());

        return R.success(emp);
    }


    /*
    * 员工退出
    * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 清理Session中保存的当前员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /*
    * 新增员工
    * */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){

        log.info(employee.toString());

        // 设置初始密码123456 且使用md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // 设置操作时间
        /*employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());*/

        // 获取当前用户的id
        Long userID = (Long) request.getSession().getAttribute("employee");

        // 设置操作员的信息
        /*employee.setCreateUser(userID);
        employee.setUpdateUser(userID);*/

        // 调用service方法
        employeeService.save(employee);

        return R.success("新曾员工成功");
    }

    /*
    * 分页查询
    * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page:{}, pageSize:{}, name:{}",page, pageSize, name);

        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        if(name != null){
            lambdaQueryWrapper.like(Employee::getName,name);
        }
        // 添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo, lambdaQueryWrapper);

        return R.success(pageInfo);
    }

    /*
    * 根据id
    * 更新员工
    * */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        log.info("emp:{}",employee);

        /*Long userID = (Long) request.getSession().getAttribute("employee");

        employee.setUpdateUser(userID);
        employee.setUpdateTime(LocalDateTime.now());*/

        employeeService.updateById(employee);

        return R.success("更新成功");

    }

    /*
    * 根据id
    * 查询员工
    * */
    @GetMapping("/{id}")
    public R<Employee> getByID(@PathVariable Long id){
        log.info("查询id为{}的员工信息", id);
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("未能查询到该员工");
    }

}
