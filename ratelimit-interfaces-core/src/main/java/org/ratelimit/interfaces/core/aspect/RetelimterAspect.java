package org.ratelimit.interfaces.core.aspect;




import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.ratelimit.interfaces.core.annotation.RateLimiterAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;


@Aspect
@Component
public class RetelimterAspect {
	private static final Logger LOGGER= LoggerFactory.getLogger(RetelimterAspect.class);
	
	@Autowired
	RedisTemplate redisTemplate;
	private DefaultRedisScript<Long> redisScript;
	
	@PostConstruct
	public void init() {
		redisScript=new DefaultRedisScript<>();
		redisScript.setResultType(Long.class);
		redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("rateLimter.lua")));
		LOGGER.info("redisLimter lua script loading OK!");
		
	}
	
	@Pointcut("@annotation(com.myredis.ratelimter.annotation.RateLimiterAnnotation)")
	public void rateLimiterAnnotation() {}
	
	@Around("@annotation(rateLimiterAnnotation)")
	public Object around(ProceedingJoinPoint proceedingJoinPoit,RateLimiterAnnotation rateLimiterAnnotation) throws Throwable {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("RateLimiterAspect handler has start limiter");
		}
		Signature signature=proceedingJoinPoit.getSignature();
		if(!(signature instanceof MethodSignature)) {
			throw new IllegalArgumentException("the Annotation @RateLimiterAnnotation must used on method!");
		}
		//获取注解的参数
		//限流key
		String limitKey=rateLimiterAnnotation.key();
		Preconditions.checkNotNull(limitKey);
		//限流value
		long limitTimes=rateLimiterAnnotation.limit();
		//限流expire
		long expireTime=rateLimiterAnnotation.expire();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("RateLimiter params: limitTimes={},limitTimeout={}",limitTimes,expireTime);
		}
		//限流提示语
		String message=rateLimiterAnnotation.message();
		if(StringUtils.isBlank(message)) {
			message="false";
		}
		//执行Lua脚本
		List<String> keyList=new ArrayList<>();
		keyList.add(limitKey);
		//调用脚本并执行
		Long result=(Long) redisTemplate.execute(redisScript, keyList, expireTime,limitTimes);
		if(result==0) {
			String msg="由于超过单位时间="+expireTime+"-允许的请求次数="+limitTimes+"[触发限流]";
			LOGGER.debug(msg);
			return message;
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("RateLimter 分布式限流处理执行结果-result={},请求[正常]响应",result);
		}
		return proceedingJoinPoit.proceed();
	}
}
