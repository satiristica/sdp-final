package org.example.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.common.StoreException;

@Aspect
public class ErrorHandlingAspect {
    @Around("execution(void org.example.ui.ConsoleStore.handle(String))")
    public void handleErrors(ProceedingJoinPoint action) throws Throwable {
        try {
            action.proceed();
        } catch (StoreException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
