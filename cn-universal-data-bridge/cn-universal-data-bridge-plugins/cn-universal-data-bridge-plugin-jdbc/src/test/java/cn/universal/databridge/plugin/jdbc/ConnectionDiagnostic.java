/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: MySQL连接诊断工具
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 */

package cn.universal.databridge.plugin.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MySQL连接诊断工具
 * 用于排查连接问题
 */
public class ConnectionDiagnostic {

    public static void main(String[] args) {
        // 测试不同的连接配置
        testConnection("jdbc:mysql://192.168.31.194:3306/test?useSSL=false&allowPublicKeyRetrieval=true", "root", "password");
        testConnection("jdbc:mysql://192.168.31.194:3306/test?useSSL=false&allowPublicKeyRetrieval=true&autoReconnect=true", "root", "password");
        testConnection("jdbc:mysql://192.168.31.194:3306/test?useSSL=false&allowPublicKeyRetrieval=true&useLocalSessionState=true", "root", "password");
    }

    private static void testConnection(String url, String username, String password) {
        System.out.println("=== 测试连接配置 ===");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        
        try {
            // 设置连接属性
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("useSSL", "false");
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("autoReconnect", "true");
            props.setProperty("failOverReadOnly", "false");
            props.setProperty("maxReconnects", "3");
            props.setProperty("connectTimeout", "30000");
            props.setProperty("socketTimeout", "30000");
            
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("✅ 连接成功！");
            
            // 测试查询
            conn.createStatement().executeQuery("SELECT 1");
            System.out.println("✅ 查询测试成功！");
            
            conn.close();
            System.out.println("✅ 连接关闭成功！");
            
        } catch (SQLException e) {
            System.out.println("❌ 连接失败: " + e.getMessage());
            System.out.println("错误代码: " + e.getErrorCode());
            System.out.println("SQL状态: " + e.getSQLState());
        }
        
        System.out.println();
    }
}
