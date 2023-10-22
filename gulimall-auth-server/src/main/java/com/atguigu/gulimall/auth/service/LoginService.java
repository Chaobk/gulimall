package com.atguigu.gulimall.auth.service;

import com.atguigu.common.utils.R;

public interface LoginService {
    R sendCode(String phone);
}
