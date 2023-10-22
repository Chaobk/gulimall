package com.atguigu.gulimall.auth.service.impl;

import com.atguigu.common.constant.AuthConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public R sendCode(String phone) {

        String redisCode = (String) redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (redisCode != null) {
            long codeTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - codeTime < 60 * 1000) {
                // 60s内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 1.接口防刷 TODO

        // 2.验证码校验 redis
        // 2.1 存key key-phone, value-code  sms:code:phone -> 123456
        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        thirdPartyFeignService.sendCode(phone, code);
        return R.ok();
    }
}
