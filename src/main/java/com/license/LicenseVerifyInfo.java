package com.license;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class LicenseVerifyInfo {
    /**
     * 认证的公司名称
     */
    private String company;
    /**
     * 授权的组件列表
     */
    private List<String> authComponentList;
    /**
     * 证书生成日期
     */
    private Date generateAt;
    /**
     * 过期日期
     */
    private Date expireAt;

}
