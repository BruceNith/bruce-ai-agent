package com.bruce.bruceaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TerminalOperationToolTest {

    @Test
    public void testExecuteTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "dir"; // 使用更合适的Windows命令
        String result = tool.executeTerminalCommand(command);
        assertNotNull(result);
        System.out.println("Command output: " + result);
    }
    
    @Test
    public void testExecuteTerminalCommandWithError() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "nonexistentcommand"; // 一个不存在的命令
        String result = tool.executeTerminalCommand(command);
        assertNotNull(result);
        System.out.println("Error command output: " + result);
        // 验证结果中包含错误信息
        assertTrue(result.contains("Error executing command") || result.contains("failed"));
    }
}