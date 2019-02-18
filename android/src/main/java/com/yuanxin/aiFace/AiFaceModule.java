package com.yuanxin.aiFace;

import com.baidu.idl.face.platform.FaceSDKManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

/**
 * <p>文件描述：<p>
 * <p>作者：Mike<p>
 * <p>创建时间：2019/2/18<p>
 * <p>更改时间：2019/2/18<p>
 */
public class AiFaceModule extends ReactContextBaseJavaModule {

    public static String licenseID = "sinooceanoffice-face-android";
    public static String licenseFileName = "idl-license.face-android";

    public AiFaceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        FaceSDKManager.getInstance().initialize(reactContext, licenseID, licenseFileName);

    }

    @Override
    public String getName() {
        return AiFaceModule.class.getName();
    }
}
