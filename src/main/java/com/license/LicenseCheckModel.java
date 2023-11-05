package com.license;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LicenseCheckModel implements Serializable {

    /**
     * 可被允许的IP地址
     */
    private List<String> ipAddress;

    /**
     * 可被允许的MAC地址
     */
    private List<String> macAddress;

    /**
     * 可被允许的CPU序列号
     */
    private List<String> cpuSerial;

    /**
     * 可被允许的主板序列号
     */
    private List<String> mainBoardSerial;

    /**
     * 授权的组件列表
     */
    private List<String> authComponentList;
}
