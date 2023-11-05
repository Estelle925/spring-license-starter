# License-Verify-Starter
license-verify-starter是基于truelicense开发的，便于生成license证书安装校验一体化springboot starter安装包

# 使用步骤
## 引入 SDK依赖
```xml
<dependency>
  <groupId>com.dtstars.license</groupId>
  <artifactId>license-verify-starter</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 使用相关方法
SDK包所有方法入口集成在 **LicenseTemplate** 类中，需要在类中引入
```java
    @Resource
    private LicenseTemplate licenseTemplate;
```

-  获取设备信息
```java

licenseTemplate.acquireHardwareInfo();

```

-  上传证书
```java

licenseTemplate.uploadLicense();

```


-  判断证书是否存在或安装过
```java

licenseTemplate.existLicense();

```


-  license 证书生成 
```java

licenseTemplate.createLicense(Date issuedTime, Date expiryTime, LicenseCheckModel model);

```

-  license 证书安装 
```java

LicenseVerifyInfo licenseVerifyInfo = licenseTemplate.installLicense(param);

```

-  license 证书校验 
   - 由于证书安装时已经初始了相关参数，校验证书只需要调用verify方法去校验上文安装的证书
```java

LicenseVerifyInfo licenseVerifyInfo = licenseTemplate.verify()

```

-  license 证书卸载
```java

licenseTemplate.unInstallLicense();

```

## 相关配置
licenseTemplate 初始化参数
```yaml

license:
  subject: 星喆科技
  publicAlias: publicCert
  privateAlias: privateKey
  storePass: Admin@123$
  licensePath: license-launcher/conf/idata.lic
  publicKeysStorePath: license-launcher/conf/publicCerts.keystore
  privateKeysStorePath: license-launcher/conf/privateKeys.keystore
  keyPass: Admin@123$

apex:
  zookeeper:
    address: 192.168.3.226:2181

```





