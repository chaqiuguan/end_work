package com.zhuanbaomao.user.service;

import com.zhuanbaomao.config.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务 —— 生成、存储、校验
 *
 * <p>生产环境应使用 Redis 存储，当前使用内存存储（开发环境）
 */
@Slf4j
@Service
public class VerificationCodeService {

    /** email → {code, expireTime} */
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000; // 5分钟
    private static final int CODE_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成6位数字验证码并存储
     */
    public String generateAndStore(String email) {
        String code = String.format("%06d", random.nextInt(1000000));
        codeStore.put(email, new CodeEntry(code, System.currentTimeMillis() + CODE_EXPIRE_MS));
        log.info("验证码已生成: email={}, code={}", email, code);
        return code;
    }

    /**
     * 校验验证码
     */
    public boolean verify(String email, String code) {
        CodeEntry entry = codeStore.get(email);
        if (entry == null) {
            return false; // 未发送验证码
        }
        if (System.currentTimeMillis() > entry.expireTime) {
            codeStore.remove(email);
            return false; // 已过期
        }
        boolean valid = entry.code.equals(code);
        if (valid) {
            codeStore.remove(email); // 校验成功后删除，防止重复使用
            log.info("验证码校验成功: email={}", email);
        }
        return valid;
    }

    /**
     * 检查是否可以重新发送（距上次发送需间隔60秒）
     */
    public void checkResendCooldown(String email) {
        CodeEntry entry = codeStore.get(email);
        if (entry != null) {
            long elapsed = System.currentTimeMillis() - (entry.expireTime - CODE_EXPIRE_MS);
            if (elapsed < 60_000) {
                throw new BusinessException("请等待 " + (60 - elapsed / 1000) + " 秒后再重新发送");
            }
        }
    }

    /** 内部存储条目 */
    private static class CodeEntry {
        final String code;
        final long expireTime;

        CodeEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}
