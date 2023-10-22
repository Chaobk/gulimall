package com.atguigu.gulimall.auth.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.service.LoginService;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private LoginService loginService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // 1.接口防刷

        // 2.验证码的再次校验。redis

        return loginService.sendCode(phone);
    }


    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if(result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            // model在重定向的时候数据会丢失
            // model.addAttribute("errors", errors);

            // Request Method 'POST' not supported
            // 用户注册 -> /regist[post] -> 转发/reg.html （路径映射默认都是get方式访问的)，
            // 为了避免这个错误，可以不使用forward，而是交给系统自己拼接

            // 校验出错，回到注册页
//            return "forward:/reg.html";
//            return "reg";
            return "redirect:/reg.html";
        }

        return "redirect:/login.html";
    }

}
