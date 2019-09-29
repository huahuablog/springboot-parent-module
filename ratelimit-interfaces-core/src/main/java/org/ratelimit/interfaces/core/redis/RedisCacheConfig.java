package org.ratelimit.interfaces.core.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *使用注解@Configuration，后面某个方法头使用@bean就会自动加载到spring ioc容器中 
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {
	private static final Logger LOGGER=LoggerFactory.getLogger(RedisCacheConfig.class);
	
	@Bean
	public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate) {
		CacheManager cacheManager=new RedisCacheManager(redisTemplate);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Springboot Redis cacheManager loading ok !");
		}
		return cacheManager;
	}
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
		RedisTemplate<String, Object> template=new RedisTemplate<>();
		template.setConnectionFactory(factory);
		//使用jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列方式）
		Jackson2JsonRedisSerializer serializer=new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper objMapper=new ObjectMapper();
		//objMapper.setVisibility(PropertyAccessor.ALL,JsonAutoDetect.Visibility.ANY);
		objMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		serializer.setObjectMapper(objMapper);
		template.setValueSerializer(serializer);
		//使用StringRedisSerializer来序列化和反序列化redis的key值
		template.setKeySerializer(new StringRedisSerializer());
		template.afterPropertiesSet();
		LOGGER.info("SpringBoot redis template loading ok!");
		return template;
	}
}
