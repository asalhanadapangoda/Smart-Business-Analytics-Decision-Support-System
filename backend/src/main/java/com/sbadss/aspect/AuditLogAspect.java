package com.sbadss.aspect;

import com.sbadss.entity.AuditLog;
import com.sbadss.entity.User;
import com.sbadss.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @Pointcut("execution(* com.sbadss.service.*.create*(..)) || " +
              "execution(* com.sbadss.service.*.update*(..)) || " +
              "execution(* com.sbadss.service.*.delete*(..)) || " +
              "execution(* com.sbadss.service.*.import*(..))")
    public void adminActions() {}

    @AfterReturning("adminActions()")
    public void logAdminAction(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = null;
            if (auth != null && auth.getPrincipal() instanceof User) {
                user = (User) auth.getPrincipal();
            }

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String ip = request.getRemoteAddr();

            AuditLog auditLog = AuditLog.builder()
                    .action(methodName)
                    .entityName(className.replace("ServiceImpl", ""))
                    .performedBy(user)
                    .ipAddress(ip)
                    .details("Executed " + methodName + " in " + className)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit Log saved: {} by {}", methodName, user != null ? user.getUsername() : "anonymous");
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}
