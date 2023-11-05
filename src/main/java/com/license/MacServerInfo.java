package com.license;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class MacServerInfo extends AbstractServerInfo {

    @Override
    protected List<String> getIpAddress() throws Exception {
        // 在 macOS 上获取 IP 地址的方法
        return executeShellCommand("ifconfig | grep 'inet ' | awk '{print $2}'");
    }

    @Override
    protected List<String> getMacAddress() throws Exception {
        // 在 macOS 上获取 MAC 地址的方法
        return executeShellCommand("ifconfig | grep 'ether' | awk '{print $2}'");
    }

    @Override
    protected List<String> getCPUSerial() throws Exception {
        // 在 macOS 上获取 CPU 序列号的方法
        return executeShellCommand("system_profiler SPHardwareDataType | grep 'Serial Number (system)' | awk '{print $4}'");
    }

    @Override
    protected List<String> getMainBoardSerial() throws Exception {
        // 在 macOS 上获取主板序列号的方法
        return executeShellCommand("system_profiler SPHardwareDataType | grep 'Hardware UUID' | awk '{print $3}'");
    }

    // 辅助方法用于执行 shell 命令并返回结果
    private List<String> executeShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", command});
        process.getOutputStream().close();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        List<String> result = reader.lines().collect(Collectors.toList());

        reader.close();

        return result;
    }
}

