package com.travel.function.config;

import com.travel.commons.enums.ResultStatus;
import com.travel.function.entity.MiaoShaUser;
import com.travel.function.exception.UserException;
import com.travel.function.logic.MiaoShaLogic;
import com.travel.service.MiaoShaUserService;
import com.travel.vo.MiaoShaUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static com.travel.commons.enums.CustomerConstant.COOKIE_NAME_TOKEN;

@Service
@Slf4j
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private MiaoShaLogic mSLogic ;
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        //获取参数类型
      Class<?> clazz =    methodParameter.getParameterType() ;
      return clazz == MiaoShaUser.class ;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        String cookieToken = getCookieValue(request,COOKIE_NAME_TOKEN);
        String paramToken = request.getParameter(COOKIE_NAME_TOKEN);

        if(StringUtils.isEmpty(cookieToken)&& StringUtils.isEmpty(paramToken)){
            log.info("***resolveArgument token为空请登录!***");
            throw new UserException(ResultStatus.USER_NOT_EXIST);
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        MiaoShaUser user = mSLogic.getByToken(response,token);
        return user;
    }

    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            log.error(" ***cookies 为null! 请登录***");
            return null;
        }
        Cookie cookieValue =  Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(cookieNameToken)).findFirst().get();
        return cookieValue.getValue();
    };

}
