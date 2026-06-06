package com.zhuanbaomao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 转鱼宝猫 - 电商平台启动类
 *
 * @author ZhuanBaoMao Team
 * @since 2024
 */
@SpringBootApplication
@EnableScheduling
public class ZhuanBaoMaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhuanBaoMaoApplication.class, args);
        System.out.println("\n" +
                "============================================\n" +
                "  🐟 转鱼宝猫 (ZhuanYuBaoMao) 电商平台\n" +
                "  🚀 启动成功！\n" +
                "  📡 API 地址: http://localhost:8080/api\n" +
                "============================================");
    }
}
