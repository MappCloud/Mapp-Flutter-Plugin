package com.mapp.flutter.sdk;

import static com.appoxee.internal.ui.UiUtils.getInAppStatisticsRequestObject;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.appoxee.Appoxee;
import com.appoxee.AppoxeeOptions;
import com.appoxee.DeviceInfo;
import com.appoxee.RequestStatus;
import com.appoxee.internal.inapp.model.APXInboxMessage;
import com.appoxee.internal.inapp.model.InAppInboxCallback;
import com.appoxee.internal.inapp.model.InAppStatistics;
import com.appoxee.internal.logger.Logger;
import com.appoxee.internal.logger.LoggerFactory;
import com.appoxee.push.NotificationMode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * MappSdkPlugin
 */
public class MappSdkPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler {

    public static final String ENGINE_ID = "MappSdkPluggin";
    public static final String MAPP_CHANNEL_NAME = "mapp_sdk";

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Application application;
    private Activity activity;
    private Result result;

    private final Logger devLogger = LoggerFactory.getDevLogger();

    private final Appoxee.OnInitCompletedListener onInitCompletedListener = new Appoxee.OnInitCompletedListener() {
        @Override
        public void onInitCompleted(boolean successful, Exception failReason) {
            if (result != null) {
                if (successful) {
                    Appoxee.instance().getDeviceInfoDMC();
                    DeviceInfo info = Appoxee.instance().getDeviceInfo();
                    devLogger.d("DEVICE INFO: " + info);
                    result.success(info.sdkVersion);
                }
                if (failReason != null) {
                    result.error("engage", failReason.getMessage(), null);
                }
            }
        }
    };

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        devLogger.d("attached to engine");
        application = (Application) flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), MAPP_CHANNEL_NAME);
        EventEmitter.getInstance().attachChannel(channel);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        devLogger.d("detached from engine");
        channel.setMethodCallHandler(null);
        EventEmitter.getInstance().detachChannel();
        application = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        List<Object> args = call.arguments();
        devLogger.d("method: " + call.method);
        int templateId;
        String eventId;
        switch (call.method) {
            case Method.GET_PLATFORM_VERSION:
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case Method.ENGAGE:
                engage(args, result);
                break;
            case Method.SET_DEVICE_ALIAS:
                setDeviceAlias((String) args.get(0), result);
                break;
            case Method.GET_DEVICE_ALIAS:
                getDeviceAlias(result);
                break;
            case Method.IS_PUSH_ENABLED:
                isPushEnabled(result);
                break;
            case Method.OPT_IN:
                boolean isEnabled = (boolean) args.get(0);
                setPushEnabled(isEnabled, result);
                break;
            case Method.TRIGGER_IN_APP:
                triggerInApp((String) args.get(0), result);
                break;
            case Method.IS_READY:
                isReady(result);
                break;
            case Method.GET_DEVICE_INFO:
                getDeviceInfo(result);
                break;
            case Method.FETCH_INBOX_MESSAGE:
                templateId = args != null && args.size() > 0 ? (Integer) args.get(0) : -1;
                fetchInboxMessage(templateId, result);
                break;
            case Method.FETCH_INBOX_MESSAGES:
                fetchInboxMessages(result);
                break;
            case Method.GET_FCM_TOKEN:
                getFcmToken(result);
                break;
            case Method.SET_TOKEN:
                setToken((String) args.get(0), result);
                break;
            case Method.START_GEOFENCING:
                startGeoFencing(result);
                break;
            case Method.STOP_GEOFENCING:
                stopGeoFencing(result);
                break;
            case Method.ADD_TAG:
                addTag((String) args.get(0), result);
                break;
            case Method.FETCH_DEVICE_TAGS:
                getTags(result);
                break;
            case Method.LOGOUT_WITH_OPT_IN:
                logOut((Boolean) args.get(0), result);
                break;
            case Method.IS_DEVICE_REGISTERED:
                isDeviceRegistered(result);
                break;
            case Method.SET_REMOTE_MESSAGE:
                // TODO get remoteMessage and pass data to a native part
                // setRemoteMessage(null,result);
                break;
            case Method.REMOVE_BADGE_NUMBER:
                removeBadgeNumber(result);
                break;
            case Method.INAPP_MARK_AS_READ:
                if(args==null || args.size()<2)
                    return;
                templateId=Integer.parseInt(args.get(0).toString());
                eventId=args.get(1).toString();
                inAppMarkAsRead(templateId,eventId,result);
                break;
            case Method.INAPP_MARK_AS_UNREAD:
                if(args==null || args.size()<2)
                    return;
                templateId=Integer.parseInt(args.get(0).toString());
                eventId=args.get(1).toString();
                inAppMarkAsUnRead(templateId,eventId,result);
                break;
            case Method.INAPP_MARK_AS_DELETED:
                if(args==null || args.size()<2)
                    return;
                templateId=Integer.parseInt(args.get(0).toString());
                eventId=args.get(1).toString();
                inAppMarkAsDeleted(templateId,eventId,result);
                break;
            default:
                result.notImplemented();
                break;
        }
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
            options.notificationMode = NotificationMode.BACKGROUND_AND_FOREGROUND;
            Appoxee.engage(application, options);
            this.result = result;
            Appoxee.instance().addInitListener(onInitCompletedListener);
            Appoxee.instance().setReceiver(PushBroadcastReceiver.class);
        } catch (Exception e) {
            result.error(Method.ENGAGE, e.getMessage(), null);
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
            result.error(Method.GET_DEVICE_ALIAS, e.getMessage(), null);
        }
    }

    private void isPushEnabled(@NonNull Result result) {
        try {
            boolean isPushEnabled = Appoxee.instance().isPushEnabled();
            result.success(isPushEnabled);
        } catch (Exception e) {
            result.error(Method.IS_PUSH_ENABLED, e.getMessage(), null);
        }
    }

    private void setPushEnabled(boolean pushEnabled, @NonNull Result result) {
        try {
            RequestStatus status = Appoxee.instance().setPushEnabled(pushEnabled);
            if (status == RequestStatus.SUCCESS) {
                result.success(true);
            } else {
                result.error(Method.OPT_IN, "Error getting push enabled state", null);
            }
        } catch (Exception e) {
            result.error(Method.OPT_IN, e.getMessage(), null);
        }
    }

    private void triggerInApp(String event, @NonNull Result result) {
        try {
            Appoxee.instance().triggerInApp(activity, event);
            result.success("");
        } catch (Exception e) {
            result.error(Method.IS_READY, e.getMessage(), null);
        }
    }

    private void isReady(@NonNull Result result) {
        try {
            boolean isReady = Appoxee.instance().isReady();
            result.success(isReady);
        } catch (Exception e) {
            result.error(Method.IS_READY, e.getMessage(), null);
        }
    }

    private void getDeviceInfo(@NonNull Result result) {
        try {
            DeviceInfo deviceInfo = Appoxee.instance().getDeviceInfo();
            if (deviceInfo != null) {
                result.success(MappSerializer.deviceInfoToMap(deviceInfo));
            } else {
                result.error(Method.GET_DEVICE_INFO, "Can't get device info!", null);
            }
        } catch (Exception e) {
            result.error(Method.GET_DEVICE_INFO, e.getMessage(), null);
        }
    }

    private void fetchInboxMessage(int templateId, @NonNull Result result) {
        try {
            Appoxee.instance().fetchInboxMessage(activity, templateId);
            handleInAppInboxMessages(result);
        } catch (Exception e) {
            result.error(Method.FETCH_INBOX_MESSAGE, e.getMessage(), null);
        }
    }

    private void fetchInboxMessages(@NonNull Result result) {
        try {
            Appoxee.instance().fetchInboxMessages(activity);
            handleInAppInboxMessages(result);
        } catch (Exception e) {
            result.error(Method.FETCH_INBOX_MESSAGES, e.getMessage(), null);
        }
    }

    private void getFcmToken(@NonNull Result result) {
        try {
            Appoxee.instance().getFcmToken(result::success);
        } catch (Exception e) {
            result.error(Method.GET_FCM_TOKEN, e.getMessage(), null);
        }
    }

    private void setToken(String token, @NonNull Result result) {
        try {
            Appoxee.instance().setToken(token);
            result.success(token);
        } catch (Exception e) {
            result.error(Method.SET_TOKEN, e.getMessage(), null);
        }
    }

    private void startGeoFencing(@NonNull Result result) {
        try {
            Appoxee.instance().startGeoFencing(result::success);
        } catch (Exception e) {
            result.error(Method.START_GEOFENCING, e.getMessage(), null);
        }
    }

    private void stopGeoFencing(@NonNull Result result) {
        try {
            Appoxee.instance().stopGeoFencing(result::success);
        } catch (Exception e) {
            result.error(Method.STOP_GEOFENCING, e.getMessage(), null);
        }
    }

    private void addTag(String tag, @NonNull Result result) {
        try {
            RequestStatus status = Appoxee.instance().addTag(tag);
            if (status == RequestStatus.SUCCESS) {
                result.success(tag);
            } else {
                result.error(Method.ADD_TAG, "Error adding TAG!", null);
            }
        } catch (Exception e) {
            result.error(Method.ADD_TAG, e.getMessage(), null);
        }
    }

    private void getTags(@NonNull Result result) {
        try {
            Set<String> tags = Appoxee.instance().getTags();
            result.success(tags);
        } catch (Exception e) {
            result.error(Method.FETCH_DEVICE_TAGS, e.getMessage(), null);
        }
    }

    private void logOut(boolean pushEnabled, @NonNull Result result) {
        try {
            Appoxee.instance().logOut(pushEnabled);
            result.success("logged out with 'PushEnabled' status: " + pushEnabled);
        } catch (Exception e) {
            result.error(Method.LOGOUT_WITH_OPT_IN, e.getMessage(), null);
        }
    }

    private void isDeviceRegistered(@NonNull Result result) {
        try {
            boolean isRegistered = Appoxee.instance().isDeviceRegistered();
            result.success(isRegistered);
        } catch (Exception e) {
            result.error(Method.IS_DEVICE_REGISTERED, e.getMessage(), null);
        }
    }

    private void removeBadgeNumber(@NonNull Result result) {
        Appoxee.removeBadgeNumber(application.getApplicationContext());
    }

    public void inAppMarkAsRead(Integer templateId, String eventId, @NonNull Result result) {
        try {
            Appoxee.instance().triggerStatistcs(activity, getInAppStatisticsRequestObject(templateId,
                    eventId,
                    InAppStatistics.INBOX_INBOX_MESSAGE_READ_KEY, null, null, null));
            result.success(true);
        } catch (Exception e) {
            result.error(Method.INAPP_MARK_AS_READ, e.getMessage(),null);
        }
    }

    public void inAppMarkAsUnRead(Integer templateId, String eventId, @NonNull Result result) {
        try {
            Appoxee.instance().triggerStatistcs(activity, getInAppStatisticsRequestObject(templateId,
                    eventId,
                    InAppStatistics.INBOX_INBOX_MESSAGE_UNREAD_KEY, null, null, null));
            result.success(true);
        } catch (Exception e) {
            result.error(Method.INAPP_MARK_AS_UNREAD, e.getMessage(),null);
        }
    }

    public void inAppMarkAsDeleted(Integer templateId, String eventId, @NonNull Result result) {
        try {
            Appoxee.instance().triggerStatistcs(activity, getInAppStatisticsRequestObject(templateId,
                    eventId,
                    InAppStatistics.INBOX_INBOX_MESSAGE_DELETED_KEY, null, null, null));
            result.success(true);
        } catch (Exception e) {
            result.error(Method.INAPP_MARK_AS_DELETED, e.getMessage(),null);
        }
    }

    private AppoxeeOptions.Server getServerByIndex(int index) {
        if (index < 0 || index > AppoxeeOptions.Server.values().length) {
            throw new IndexOutOfBoundsException("Server must be one of the following: L3 [0], L3_US [1], EMC [2], EMC_US [3], CROC [4], TEST [5], TEST55 [6] and proper index provided.");
        }
        return AppoxeeOptions.Server.values()[index];
    }

    private void handleInAppInboxMessages(@NonNull Result result) {
        InAppInboxCallback inAppInboxCallback = new InAppInboxCallback();
        inAppInboxCallback.addInAppInboxMessagesReceivedCallback(new InAppInboxCallback.onInAppInboxMessagesReceived() {
            @Override
            public void onInAppInboxMessages(List<APXInboxMessage> messages) {
                JSONArray array = new JSONArray();
                for (APXInboxMessage message : messages) {
                    JSONObject json = MappSerializer.messageToJson(message);
                    array.put(json);
                }
                result.success(array.toString());
            }

            @Override
            public void onInAppInboxMessage(APXInboxMessage message) {
                JSONObject json = MappSerializer.messageToJson(message);
                result.success(json.toString());
            }
        });
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        devLogger.d("attached to activity");
        this.activity = binding.getActivity();
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        devLogger.d("detached from activity for config changes: " + (activity != null ? activity.getClass().getName() : "null"));
        this.activity = null;
        this.channel.setMethodCallHandler(null);
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        devLogger.d("reattached to activity on config changes: " + binding.getActivity());
        this.activity = binding.getActivity();
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromActivity() {
        devLogger.d("detached from activity: " + (activity != null ? activity.getClass().getName() : "null"));
        this.activity = null;
        this.channel.setMethodCallHandler(null);
    }

    public static void handleIntent(Activity activity, Intent intent) {
        if (activity == null || intent == null) return;
        Intent richIntent = intent.getParcelableExtra("intent");
        if (richIntent != null && richIntent.getAction().equals(Action.RICH_PUSH)) {
            Appoxee.handleRichPush(activity, richIntent);
        }
    }

}