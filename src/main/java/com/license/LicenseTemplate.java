package com.license;

import com.alibaba.fastjson2.JSONObject;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import de.schlichtherle.license.LicenseContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class LicenseTemplate {


    @Value("${license.subject}")
    private String subject;

    @Value("${license.publicAlias}")
    private String publicAlias;

    @Value("${license.privateAlias}")
    private String privateAlias;

    @Value("${license.storePass}")
    private String storePass;

    @Value("${license.licensePath}")
    private String licensePath;

    @Value("${license.publicKeysStorePath}")
    private String publicKeysStorePath;

    @Value("${license.privateKeysStorePath}")
    private String privateKeysStorePath;

    @Value("${license.keyPass}")
    private String keyPass;

    @Resource
    private LicenseTemplateService licenseTemplateService;

    /**
     * 获取硬件信息(最终生成的下载文件为 idata-license-apply.info)
     *
     * @return
     */
    public String acquireHardwareInfo() {
        try {
            LicenseCheckModel machineData = licenseTemplateService.getMachineInfo();
            return Base64.encode(JSONObject.toJSONString(machineData).getBytes());
        } catch (Exception e) {
            log.error("硬件信息出错 {}", e.getMessage(), e);
            throw new IllegalArgumentException("硬件信息出错");
        }

    }


    /**
     * 创建证书
     *
     * @param issuedTime 有效时间
     * @param expiryTime 过期时间
     * @param model      校验参数 (mac,cpu,主板) 需要记录的授权模块
     * @return 证书字节码
     */
    public byte[] createLicense(Date issuedTime, Date expiryTime, LicenseCheckModel model) {
        LicenseCreatorParam param = new LicenseCreatorParam();
        param.setSubject(subject);
        param.setPrivateAlias(privateAlias);
        param.setStorePass(storePass);
        param.setKeyPass(keyPass);
        param.setPrivateKeysStorePath(privateKeysStorePath);
        param.setIssuedTime(issuedTime);
        param.setExpiryTime(expiryTime);
        param.setLicenseCheckModel(model);
        return licenseTemplateService.createLicense(param);
    }

    /**
     * 安装证书
     *
     * @return 证书信息
     */
    public LicenseVerifyInfo installLicense() {
        LicenseVerifyParam param = new LicenseVerifyParam();
        param.setSubject(subject);
        param.setPublicAlias(publicAlias);
        param.setStorePass(storePass);
        param.setLicensePath(licensePath);
        param.setPublicKeysStorePath(publicKeysStorePath);
        LicenseContent licenseContent = licenseTemplateService.install(param);
        if (licenseContent != null) {
            String extra = JSONObject.toJSONString(licenseContent.getExtra());
            LicenseCheckModel model = JSONObject.parseObject(extra, LicenseCheckModel.class);
            LicenseVerifyInfo info = new LicenseVerifyInfo();
            info.setCompany(licenseContent.getSubject());
            info.setAuthComponentList(model.getAuthComponentList());
            info.setGenerateAt(licenseContent.getIssued());
            info.setExpireAt(licenseContent.getNotAfter());
            return info;
        }
        return null;
    }


    /**
     * 传入上传的License证书内容
     */
    public LicenseVerifyInfo verify() {
        LicenseVerifyParam param = new LicenseVerifyParam();
        param.setSubject(subject);
        param.setPublicAlias(publicAlias);
        param.setStorePass(storePass);
        param.setLicensePath(licensePath);
        param.setPublicKeysStorePath(publicKeysStorePath);
        LicenseContent licenseContent = licenseTemplateService.verify(param);
        if (Objects.nonNull(licenseContent)) {
            LicenseCheckModel model = JSONObject.parseObject(JSONObject.toJSONString(licenseContent.getExtra()), LicenseCheckModel.class);
            LicenseVerifyInfo info = new LicenseVerifyInfo();
            info.setCompany(licenseContent.getSubject());
            info.setAuthComponentList(model.getAuthComponentList());
            info.setGenerateAt(licenseContent.getIssued());
            info.setExpireAt(licenseContent.getNotAfter());
            return info;
        }
        return null;
    }

    /**
     * 判断证书是否存在
     */
    public Boolean existLicense() {
        File file = new File(licensePath);
        return file.exists();
    }

    /**
     * 上传证书
     *
     * @param fileByte 证书文件字节码
     */
    public Boolean uploadLicense(byte[] fileByte) {
        try {
            File destFile = new File(licensePath);
            // 如果文件存在，清空文件数据
            if (destFile.exists()) {
                try (FileOutputStream fos = new FileOutputStream(destFile, false)) {
                    fos.write(fileByte);
                }
            } else {
                // 如果文件不存在，创建文件并写入数据
                destFile.createNewFile();
                try (FileOutputStream fos = new FileOutputStream(destFile)) {
                    fos.write(fileByte);
                }
            }
        } catch (Exception e) {
            log.error("证书上传异常", e);
            return false;
        }
        return true;
    }


    /**
     * 卸载当前的证书
     */
    public Boolean unInstallLicense() {
        LicenseVerifyParam param = new LicenseVerifyParam();
        param.setSubject(subject);
        param.setPublicAlias(publicAlias);
        param.setStorePass(storePass);
        param.setLicensePath(licensePath);
        param.setPublicKeysStorePath(publicKeysStorePath);
        return licenseTemplateService.unInstall(param);
    }
}
