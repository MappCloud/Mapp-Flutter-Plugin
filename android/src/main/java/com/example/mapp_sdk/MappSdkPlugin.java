package com.example.mapp_sdk;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appoxee.Appoxee;
import com.appoxee.AppoxeeOptions;
import com.appoxee.RequestStatus;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * MappSdkPlugin
 */
public class MappSdkPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Application application;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "mapp_sdk");
        channel.setMethodCallHandler(this);
        application = (Application) flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "engage":
                engage(call.arguments(), result);
                break;
            case "setDeviceAlias":
                setDeviceAlias(call.argument("alias"), result);
                break;
            case "getDeviceAlias":
                getDeviceAlias(result);
                break;
            case "isPushEnabled":
                isPushEnabled(result);
                break;
            case "setPushEnabled":
                setPushEnabled(call.argument("pushEnabled"), result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        application = null;
    }

    private void engage(Map<String, String> args, @NonNull Result result) {
        try {
            AppoxeeOptions options = new AppoxeeOptions();
            options.appID = args.get("appID");
            options.sdkKey = args.get("sdkKey");
            options.googleProjectId = args.get("googleProjectId");
            options.tenantID = args.get("tenantId");
            options.server = getServer(args.get("server"));
            Appoxee.engage(application, options);
            Appoxee.instance().addInitListener((successful, failReason) -> {
                if (successful) {
                    result.success(true);
                } else {
                    result.error("engage", failReason.getMessage(), null);
                }
            });
        } catch (Exception e) {
            result.error("engage", e.getMessage(), null);
        }
    }

    private void setDeviceAlias(String alias, @NonNull Result result) {
        RequestStatus status = Appoxee.instance().setAlias(alias);
        if (status == RequestStatus.SUCCESS) {
            result.success(alias);
        } else {
            result.error("setDeviceAlias", "Error setting alias", null);
        }
    }

    private void getDeviceAlias(@NonNull Result result) {
        try {
            String alias = Appoxee.instance().getAlias();
            result.success(alias);
        } catch (Exception e) {
            result.error("getDeviceAlias", e.getMessage(), null);
        }
    }

    private void isPushEnabled(@NonNull Result result) {
        try {
            boolean isPushEnabled = Appoxee.instance().isPushEnabled();
            result.success(isPushEnabled);
        } catch (Exception e) {
            result.error("isPushEnabled", e.getMessage(), null);
        }
    }

    private void setPushEnabled(boolean pushEnabled, @NonNull Result result){
        try {
            RequestStatus status= Appoxee.instance().setPushEnabled(pushEnabled);
            if(status==RequestStatus.SUCCESS){
                result.success(true);
            }else{
                result.error("setPushEnabled", "Error getting push enabled state", null);
            }
        }catch (Exception e){
            result.error("setPushEnabled",e.getMessage(),null);
        }
    }

    private AppoxeeOptions.Server getServer(String server) {
        if (server == null || server.isEmpty())
            throw new NullPointerException("Server must be provided!");

        switch (server.toUpperCase()) {
            case "TEST":
                return AppoxeeOptions.Server.TEST;
            case "TEST_55":
                return AppoxeeOptions.Server.TEST_55;
            case "EMC":
                return AppoxeeOptions.Server.EMC;
            case "EMC_US":
                return AppoxeeOptions.Server.EMC_US;
            case "L3":
                return AppoxeeOptions.Server.L3;
            case "L3_US":
                return AppoxeeOptions.Server.L3_US;
            default:
                throw new IllegalArgumentException(
                        "Server must be one of the following: TEST, TEST_55, EMC, EMC_US, L3, L3_US"
                );
        }
    }
}
