package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.EmailSender;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    /*
    * 发送验证码
    * */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

        /*// 获取邮箱
        String phone = user.getPhone();

        // 邮箱不为空
        if( phone != null){
            // 生成随机的四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            // 邮箱发送验证码
            EmailSender.sendEmail(phone, code);

            // 将生成的验证码保存到Session
            session.setAttribute(phone, code);



            return R.success("邮箱验证码发送成功");
        }

        // 邮箱为空
        return R.error("邮箱验证码发送失败");*/

        // 获取手机
        String phone = user.getPhone();

        // 手机不为空
        if( phone != null){
            // 生成随机的四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code:{}",code);

            // 调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖", "",phone, code);

            // 将生成的验证码保存到Session
            session.setAttribute(phone, code);

            // 将生成的验证码缓存到redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("手机验证码发送成功");
        }

        // 手机为空
        return R.error("手机验证码发送失败");

    }


    /*
    * 用户登录
    * */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        // 获取手机号
        String phone = map.get("phone").toString();

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getPhone, phone);
        User user = userService.getOne(lambdaQueryWrapper);

        //从redis中获取缓存验证码
        //redisTemplate.opsForValue().get(phone);

        // 判断当前手机号对应的用户是否为新用户
        if(user == null){
            // 新用户 --> 自动完成注册
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        }

        session.setAttribute("user", user.getId());

        //如果用户登录成功 删除缓存的验证码
        //redisTemplate.delete(phone);

        return R.success(user);
    }


}
