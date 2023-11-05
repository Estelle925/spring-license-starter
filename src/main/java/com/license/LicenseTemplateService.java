package com.license;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.schlichtherle.license.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

@Slf4j
public class LicenseTemplateService {
    private static final String LICENSE_CACHE_ROOT_NODE = "/idata";
    private static final String LICENSE_CACHE_MAC_NODE = "/license_mac";
    private static final String LICENSE_CACHE_CPU_NODE = "/license_cpu";
    private static final String LICENSE_CACHE_MOTHER_BOARD_NODE = "/license_board";

    @Value("${zookeeper.address}")
    private String zookeeperUrl;

    private static Watcher getWatcher() {
        return event -> {
            if (event.getType() == Watcher.Event.EventType.NodeCreated) {
                log.info("license节点创建事件: " + event.getPath());
            } else if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                log.info("license节点删除事件: " + event.getPath());
            } else if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                log.info("license节点数据变化事件: " + event.getPath());
            }
        };
    }

    @PostConstruct
    public void initLicenseDataInfo() {
        try {
            //读取zookeeper 中硬件设备信息
            if (StringUtils.isNotBlank(zookeeperUrl)) {
                Watcher watcher = getWatcher();
                ZooKeeper zookeeper = new ZooKeeper(zookeeperUrl, 30000, watcher);
                String macNode = LICENSE_CACHE_ROOT_NODE + LICENSE_CACHE_MAC_NODE;
                String cpuNode = LICENSE_CACHE_ROOT_NODE + LICENSE_CACHE_CPU_NODE;
                String mainBoardNode = LICENSE_CACHE_ROOT_NODE + LICENSE_CACHE_MOTHER_BOARD_NODE;
                AbstractServerInfo serverInfo = AbstractServerInfo.getSystemServer();
                List<String> macAddress = serverInfo.getMacAddress();
                List<String> cpuSerialNumber = serverInfo.getCPUSerial();
                List<String> motherboardInfo = serverInfo.getMainBoardSerial();
                checkZKValueAndSave(zookeeper, macNode, LICENSE_CACHE_ROOT_NODE, macAddress);
                checkZKValueAndSave(zookeeper, cpuNode, LICENSE_CACHE_ROOT_NODE, cpuSerialNumber);
                checkZKValueAndSave(zookeeper, mainBoardNode, LICENSE_CACHE_ROOT_NODE, motherboardInfo);
                zookeeper.close();
            }
        } catch (Exception e) {
            log.error("初始化硬件设备数据信息异常", e);
        }
    }

    /**
     * 获取所有设备信息
     */
    public LicenseCheckModel getMachineInfo() {
        try {
            if (StringUtils.isNotBlank(zookeeperUrl)) {
                Watcher watcher = getWatcher();
                ZooKeeper zookeeper = new ZooKeeper(zookeeperUrl, 30000, watcher);
                String macNode = LICENSE_CACHE_ROOT_NODE + LICENSE_CACHE_MAC_NODE;
                String cpuNode = LICENSE_CACHE_ROOT_NODE + LICENSE_CACHE_CPU_NODE;
                String mainBoardNode = LICENSE_CACHE_ROOT_NODE + LICENSE_CACHE_MOTHER_BOARD_NODE;
                String macValues = new String(getZKValue(zookeeper, LICENSE_CACHE_ROOT_NODE, macNode), StandardCharsets.UTF_8);
                String cpuNumValues = new String(getZKValue(zookeeper, LICENSE_CACHE_ROOT_NODE, cpuNode), StandardCharsets.UTF_8);
                String motherboardInfoValues = new String(getZKValue(zookeeper, LICENSE_CACHE_ROOT_NODE, mainBoardNode), StandardCharsets.UTF_8);
                LicenseCheckModel info = new LicenseCheckModel();
                info.setMacAddress(StringUtils.isNotBlank(macValues) ? JSONObject.parseArray(macValues, String.class) : Lists.newArrayList());
                info.setCpuSerial(StringUtils.isNotBlank(cpuNumValues) ? JSONObject.parseArray(cpuNumValues, String.class) : Lists.newArrayList());
                info.setMainBoardSerial(StringUtils.isNotBlank(motherboardInfoValues) ? JSONObject.parseArray(motherboardInfoValues, String.class) : Lists.newArrayList());
                zookeeper.close();
                return info;
            }
        } catch (Exception e) {
            log.error("获取硬件设备数据信息异常", e);
        }
        return null;
    }

    public byte[] getZKValue(ZooKeeper zooKeeper, String rootNode, String nodePath) throws InterruptedException, KeeperException {
        // 检查节点是否存在
        Stat rootNodeStat = zooKeeper.exists(rootNode, false);
        if (rootNodeStat == null) {
            // 节点不存在，创建节点并写入数据
            zooKeeper.create(rootNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        Stat nodeStat = zooKeeper.exists(nodePath, false);
        if (nodeStat == null) {
            // 节点不存在，创建节点并写入数据
            return null;
        } else {
            // 节点存在，获取节点数据
            return zooKeeper.getData(nodePath, false, nodeStat);
        }
    }

    public void checkZKValueAndSave(ZooKeeper zooKeeper, String nodePath, String rootNode, List<String> data) throws InterruptedException, KeeperException {
        // 检查节点是否存在
        Stat rootNodeStat = zooKeeper.exists(rootNode, false);
        if (rootNodeStat == null) {
            // 节点不存在，创建节点并写入数据
            zooKeeper.create(rootNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        Stat nodeStat = zooKeeper.exists(nodePath, false);
        if (nodeStat == null) {
            // 节点不存在，创建节点并写入数据
            zooKeeper.create(nodePath, JSONObject.toJSONString(data).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            // 节点存在，获取节点数据
            byte[] existingData = zooKeeper.getData(nodePath, false, nodeStat);
            // 判断数据是否包含要写入的值
            String existingDataString = new String(existingData);
            List<String> existValues = StringUtils.isNotBlank(existingDataString) ? JSONObject.parseArray(existingDataString, String.class) : Lists.newArrayList();
            Set<String> ipValuesSets = Sets.newHashSet(existValues);
            if (CollectionUtils.isNotEmpty(data)) {
                ipValuesSets.addAll(data);
            }
            zooKeeper.setData(nodePath, JSONObject.toJSONString(ipValuesSets).getBytes(), -1);
        }
    }

    /**
     * 生成证书
     *
     * @param param 证书参数
     * @return 证书路径
     */
    public byte[] createLicense(LicenseCreatorParam param) {
        LicenseCreator licenseCreator = new LicenseCreator(param);
        return licenseCreator.generateLicense();
    }


    /**
     * 安装License证书
     */
    public synchronized LicenseContent install(LicenseVerifyParam param) {
        LicenseContent result = null;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //1. 安装证书
        try {
            LicenseManager licenseManager = new CustomLicenseManager(initLicenseParam(param));
            licenseManager.uninstall();

            result = licenseManager.install(new File(param.getLicensePath()));
            log.info(MessageFormat.format("证书安装成功，证书有效期：{0} - {1}", format.format(result.getNotBefore()), format.format(result.getNotAfter())));
        } catch (Exception e) {
            log.error("证书安装失败！", e);
        }

        return result;
    }

    /**
     * 校验License证书
     */
    public LicenseContent verify(LicenseVerifyParam param) {
        LicenseManager licenseManager = new CustomLicenseManager(initLicenseParam(param));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //2. 校验证书
        try {
            LicenseContent licenseContent = licenseManager.verify();
            log.info(MessageFormat.format("证书校验通过，证书有效期：{0} - {1}", format.format(licenseContent.getNotBefore()), format.format(licenseContent.getNotAfter())));
            return licenseContent;
        } catch (Exception e) {
            log.error("证书校验失败！", e);
            return null;
        }
    }


    /**
     * 卸载License证书
     */
    public Boolean unInstall(LicenseVerifyParam param) {
        LicenseManager licenseManager = new CustomLicenseManager(initLicenseParam(param));
        try {
            licenseManager.uninstall();
            log.info("卸载证书成功");
            return true;
        } catch (Exception e) {
            log.error("卸载证书失败！", e);
            return false;
        }
    }

    private LicenseParam initLicenseParam(LicenseVerifyParam param) {
        Preferences preferences = Preferences.userNodeForPackage(LicenseTemplateService.class);
        CipherParam cipherParam = new DefaultCipherParam(param.getStorePass());
        KeyStoreParam publicStoreParam = new CustomKeyStoreParam(LicenseTemplateService.class, param.getPublicKeysStorePath(), param.getPublicAlias(), param.getStorePass(), null);
        return new DefaultLicenseParam(param.getSubject(), preferences, publicStoreParam, cipherParam);
    }
}
