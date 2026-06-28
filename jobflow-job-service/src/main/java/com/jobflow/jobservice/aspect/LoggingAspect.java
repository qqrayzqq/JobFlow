package com.jobflow.jobservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // matches every method in the service package and its subpackages
    @Around("execution(* com.jobflow.jobservice.service..*(..))")
    public Object logMethod(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("-> {}", pjp.getSignature().toShortString());
        try{
            Object result = pjp.proceed();
            log.info("<- {} ({} ms) ", pjp.getSignature().toShortString(), System.currentTimeMillis() - start);
            return result;
        }catch (Throwable ex){
            log.error("x {} failed: {}",pjp.getSignature().toShortString(), ex.getMessage());
            throw ex;
        }
    }
}
