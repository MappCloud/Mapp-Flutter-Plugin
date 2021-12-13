package com.example.mapp_sdk;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appoxee.Appoxee;
import com.appoxee.AppoxeeOptions;
import com.appoxee.DeviceInfo;
import com.appoxee.RequestStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        Appoxee.engage(application);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        List<Object> args = call.arguments();
        switch (call.method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "engage":
                engage(args, result);
                break;
            case "setDeviceAlias":
                setDeviceAlias((String) args.get(0), result);
                break;
            case "getDeviceAlias":
                getDeviceAlias(result);
                break;
            case "isPushEnabled":
                isPushEnabled(result);
                break;
            case "optIn":
                boolean isEnabled = (boolean) args.get(0);
                setPushEnabled(isEnabled, result);
                break;
            case "triggerInApp":
                triggerInApp((String) args.get(0), result);
                break;
            case "isReady":
                isReady(result);
                break;
            case "getDeviceInfo":
                getDeviceInfo(result);
                break;
            case "fetchInboxMessage":
                fetchInboxMessage((Integer) args.get(0), result);
                break;
            case "fetchInboxMessages":
                fetchInboxMessages(result);
                break;
            case "getFcmToken":
                getFcmToken(result);
                break;
            case "setToken":
                setToken((String) args.get(0), result);
                break;
            case "startGeofencing":
                startGeoFencing(result);
                break;
            case "stopGeofencing":
                stopGeoFencing(result);
                break;
            case "addTag":
                addTag((String) args.get(0), result);
                break;
            case "fetchDeviceTags":
                getTags(result);
                break;
            case "logoutWithOptin":
                logOut((Boolean) args.get(0), result);
                break;
            case "isDeviceRegistered":
                isDeviceRegistered(result);
                break;
            case "setRemoteMessage":
                // TODO get remoteMessage and pass data to a native part
                // setRemoteMessage(null,result);
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

    private void engage(List<Object> args, @NonNull Result result) {
        try {
            //[sdkKey, googleProjectId, server.index, appID, tenantID]
            AppoxeeOptions options = new AppoxeeOptions();
            options.sdkKey = (String) args.get(0);
            options.googleProjectId = (String) args.get(1);
            options.server = getServerByIndex((Integer) args.get(2));
            options.appID = (String) args.get(3);
            options.tenantID = (String) args.get(4);
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
        result.success(alias);
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

    private void setPushEnabled(boolean pushEnabled, @NonNull Result result) {
        try {
            RequestStatus status = Appoxee.instance().setPushEnabled(pushEnabled);
            if (status == RequestStatus.SUCCESS) {
                result.success(true);
            } else {
                result.error("setPushEnabled", "Error getting push enabled state", null);
            }
        } catch (Exception e) {
            result.error("setPushEnabled", e.getMessage(), null);
        }
    }

    private void triggerInApp(String event, @NonNull Result result) {
        try {
            Appoxee.instance().triggerInApp(application.getApplicationContext(), event);
            result.success("");
        } catch (Exception e) {
            result.error("isReady", e.getMessage(), null);
        }
    }

    private void isReady(@NonNull Result result) {
        try {
            boolean isReady = Appoxee.instance().isReady();
            result.success(isReady);
        } catch (Exception e) {
            result.error("isReady", e.getMessage(), null);
        }
    }

    private void getDeviceInfo(@NonNull Result result) {
        try {
            DeviceInfo deviceInfo = Appoxee.instance().getDeviceInfo();
            if (deviceInfo != null) {
                result.success(deviceInfoToMap(deviceInfo));
            } else {
                result.error("getDeviceInfo", "Can't get device info!", null);
            }
        } catch (Exception e) {
            result.error("getDeviceInfo", e.getMessage(), null);
        }
    }

    private void fetchInboxMessage(int template, @NonNull Result result) {
        try {
            Appoxee.instance().fetchInboxMessage(application.getApplicationContext(), template);
        } catch (Exception e) {
            result.error("fetchInboxMessage", e.getMessage(), null);
        }
    }

    private void fetchInboxMessages(@NonNull Result result) {
        try {
            Appoxee.instance().fetchInboxMessages(application.getApplicationContext());
        } catch (Exception e) {
            result.error("fetchInboxMessages", e.getMessage(), null);
        }
    }

    private void getFcmToken(@NonNull Result result) {
        try {
            Appoxee.instance().getFcmToken(result::success);
        } catch (Exception e) {
            result.error("getFcmToken", e.getMessage(), null);
        }
    }

    private void setToken(String token, @NonNull Result result) {
        try {
            Appoxee.instance().setToken(token);
            result.success(token);
        } catch (Exception e) {
            result.error("setToken", e.getMessage(), null);
        }
    }

    private void startGeoFencing(@NonNull Result result) {
        try {
            Appoxee.instance().startGeoFencing(result::success);
        } catch (Exception e) {
            result.error("startGeoFencing", e.getMessage(), null);
        }
    }

    private void stopGeoFencing(@NonNull Result result) {
        try {
            Appoxee.instance().stopGeoFencing(result::success);
        } catch (Exception e) {
            result.error("stopGeoFencing", e.getMessage(), null);
        }
    }

    private void addTag(String tag, @NonNull Result result) {
        try {
            RequestStatus status = Appoxee.instance().addTag(tag);
            if (status == RequestStatus.SUCCESS) {
                result.success(tag);
            } else {
                result.error("addTag", "Error adding TAG!", null);
            }
        } catch (Exception e) {
            result.error("addTag", e.getMessage(), null);
        }
    }

    private void getTags(@NonNull Result result) {
        try {
            Set<String> tags = Appoxee.instance().getTags();
            result.success(tags);
        } catch (Exception e) {
            result.error("getTag", e.getMessage(), null);
        }
    }

    private void logOut(boolean pushEnabled, @NonNull Result result) {
        try {
            Appoxee.instance().logOut(application, pushEnabled);
            result.success(true);
        } catch (Exception e) {
            result.error("logOut", e.getMessage(), null);
        }
    }

    private void isDeviceRegistered(@NonNull Result result) {
        try {
            boolean isRegistered = Appoxee.instance().isDeviceRegistered();
            result.success(isRegistered);
        } catch (Exception e) {
            result.error("isDeviceRegistered", e.getMessage(), null);
        }
    }

//    private void setRemoteMessage(RemoteMessage remoteMessage, @NonNull Result result) {
//        try {
//            Appoxee.instance().setRemoteMessage(remoteMessage);
//        } catch (Exception e) {
//            result.error("", e.getMessage(), null);
//        }
//    }

    private AppoxeeOptions.Server getServerByIndex(int index) {
        if (index < 0 || index > AppoxeeOptions.Server.values().length) {
            throw new IndexOutOfBoundsException("Server must be one of the following: L3 [0], L3_US [1], EMC [2], EMC_US [3], CROC [4], TEST [5], TEST55 [6] and proper index provided.");
        }
        return AppoxeeOptions.Server.values()[index];
    }

    private Map<String, Object> deviceInfoToMap(DeviceInfo deviceInfo) {
        Map<String, Object> map = new HashMap<>();
        try {
            map.put("id", deviceInfo.id);
            map.put("appVersion", deviceInfo.appVersion);
            map.put("sdkVersion", deviceInfo.sdkVersion);
            map.put("locale", deviceInfo.locale);
            map.put("timezone", deviceInfo.timezone);
            map.put("deviceModel", deviceInfo.deviceModel);
            map.put("manufacturer", deviceInfo.manufacturer);
            map.put("osVersion", deviceInfo.osVersion);
            map.put("resolution", deviceInfo.resolution);
            map.put("density", String.valueOf(deviceInfo.density));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }
}
