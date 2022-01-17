import 'dart:async';
import 'dart:convert';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mapp_sdk/helper_classes.dart';
import 'package:mapp_sdk/mapp_sdk.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
          primaryColor: const Color(0xFF00BAFF),
          primaryColorDark: const Color(0xFF0592D7),
          accentColor: const Color(0xFF58585A),
          cardColor: const Color(0xFF888888)),
      home: HomePage(),
    );
  }
}

// ignore: use_key_in_widget_constructors
class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String? _platformVersion = 'Unknown';
  String? _aliasToSetString = '';
  String? _tagToSetString = '';
  String? _tagToRemoveString = '';
  String? _stringToSetString = '';
  String? _attributeToGetString = '';
  String? _stringToRemoveString = '';

  List<String> _screens = [];

  @override
  void initState() {
    super.initState();

    MappSdk.engage(Config.sdkKey, Config.googleProjectId, Config.server,
        Config.appID, Config.tenantID);

    initPlatformState();
  }

  void didReceiveDeepLinkWithIdentifierHandler(dynamic arguments) {
    print("deep link received!");
    print(arguments);
  }

  void didReceiveInappMessageWithIdentifierHandler(dynamic arguments) {
    print("Inapp message received!");
    print(arguments);
  }

  void didReceiveCustomLinkWithIdentifierHandler(dynamic arguments) {
    print("Custom Link With Identifier received!");
    print(arguments);
  }

  void didReceiveInBoxMessagesHandler(dynamic arguments) {
    print("Inbox Messages received!");
    print(arguments);
  }

  void inAppCallFailedWithResponseHandler(dynamic arguments) {
    print("inApp Call Failed received!");
    print(arguments);
  }

  void didReceiveInBoxMessageHandler(dynamic arguments) {
    print("Inbox Message received!");
    print(arguments);
  }

  void remoteNotificationHandler(dynamic arguments) {
    print("remote Notification received!");
    print(arguments);
  }

  void richContentHandler(dynamic arguments) {
    print("rich Content received!");
    print(arguments);
  }

  void pushOpenedHandler(dynamic arguments) {
    print("Push opened!");
    print(arguments);
  }

  void pushDismissedHandler(dynamic arguments) {
    print("Push dismissed!");
    print(arguments);
  }

  void pushSilentHandler(dynamic arguments) {
    print("Push silent!");
    print(arguments);
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String? platformVersion;

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await MappSdk.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    setState(() {
      _platformVersion = platformVersion;
      _screens = [
        "Set Device Alias Text",
        "Set Device Alias",
        "Get Device Alias",
        "Device Information",
        "Is Push Enabled",
        "Opt in",
        "Opt out",
        "Start Geo",
        "Stop Geo",
        "Fetch inbox messages",
        "In App: App Open",
        "In App: App Feedback",
        "In App: App Discount",
        "In App: App Promo",
        "Remove Badge Number",
        "Lock Orientation",
        "Engage",
        "Log out"
      ];
    });
  }

  Future<void> _showMyDialog(String title, String subtitle, String text) async {
    return showDialog<void>(
      context: context,
      barrierDismissible: false, // user must tap button!
      builder: (BuildContext context) {
        return AlertDialog(
          title: Text(title),
          content: SingleChildScrollView(
            child: ListBody(
              children: <Widget>[
                Text(subtitle),
                Text(text),
              ],
            ),
          ),
          actions: <Widget>[
            TextButton(
              child: const Text('Ok'),
              onPressed: () {
                Navigator.of(context).pop();
              },
            ),
          ],
        );
      },
    );
  }

  Card _createTextFieldOrButton(int index) {
    switch (index) {
      case 0:
        return Card(
          child: TextFormField(
            decoration: const InputDecoration(
                border: UnderlineInputBorder(), labelText: 'Enter alias'),
            onChanged: (String? value) {
              _aliasToSetString = value?.trim();
            },
          ),
        );
      default:
        return Card(
          child: ListTile(
            title: Text(
              _screens[index],
              style: TextStyle(color: Theme.of(context).primaryColor),
              textAlign: TextAlign.center,
            ),
            onTap: () {
              onTap(index);
            },
          ),
        );
    }
  }

  void onTap(int index) {
    if (_screens[index] == "Engage") {
      MappSdk.engage(Config.sdkKey, Config.googleProjectId, Config.server,
          Config.appID, Config.tenantID);
      print(
          "ENGAGE WITH PARAMS: SDK_KEY: ${Config.sdkKey}, Server: ${Config.server.toString()}, APP_ID: ${Config.appID}, TENANT_ID: ${Config.tenantID}");
    } else if (_screens[index] == "Set Device Alias") {
      if (_aliasToSetString?.isNotEmpty ?? false) {
        MappSdk.setAlias(_aliasToSetString!);
      } else {
        _showMyDialog('Alias', "Not set", "Alias can't be empty");
      }
    } else if (_screens[index] == "Get Device Alias") {
      MappSdk.getAlias().then(
          (String value) => {_showMyDialog("Show Alias", "Alias:", value)});
    } else if (_screens[index] == "Device Information") {
      String data = '';
      MappSdk.getDeviceInfo().then((Map<String, dynamic>? map) {
        data = map != null ? jsonEncode(map) : "null";
        _showMyDialog("Device info", "", data);
      });
    } else if (_screens[index] == "Is Push Enabled") {
      MappSdk.isPushEnabled().then((bool value) =>
          {_showMyDialog("Show Device Information", "", value ? "YES" : "NO")});
    } else if (_screens[index] == "Opt in") {
      MappSdk.setPushEnabled(true);
    } else if (_screens[index] == "Opt out") {
      MappSdk.setPushEnabled(false);
    } else if (_screens[index] == "Remove Badge Number") {
      MappSdk.removeBadgeNumber();
    } else if (_screens[index] == "Log out") {
      MappSdk.logOut(false);
    } else if (_screens[index] == "In App: App Open") {
      MappSdk.triggerInApp("app_open");
    } else if (_screens[index] == "Fetch inbox messages") {
      MappSdk.fetchInboxMessage();
    } else if (_screens[index] == "In App: App Feedback") {
      //FetchInBox se koristi samo za testiranje
      // MappSdk.fetchInBoxMessageWithMessageId(18870);
      MappSdk.triggerInApp("app_feedback");
    } else if (_screens[index] == "In App: App Discount") {
      MappSdk.triggerInApp("app_discount");
    } else if (_screens[index] == "In App: App Promo") {
      MappSdk.triggerInApp("app_promo");
    }
  }

  ListView _buildListView(BuildContext context) {
    return ListView.builder(
      itemCount: _screens.length,
      itemBuilder: (context, index) {
        return _createTextFieldOrButton(index);
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    MappSdk.didReceiveDeepLinkWithIdentifier = (dynamic arguments) =>
        didReceiveDeepLinkWithIdentifierHandler(arguments);

    MappSdk.didReceiveInappMessageWithIdentifier = (dynamic arguments) =>
        didReceiveInappMessageWithIdentifierHandler(arguments);

    MappSdk.didReceiveCustomLinkWithIdentifier = (dynamic arguments) =>
        didReceiveCustomLinkWithIdentifierHandler(arguments);

    MappSdk.didReceiveInBoxMessages =
        (dynamic arguments) => didReceiveInBoxMessagesHandler(arguments);

    MappSdk.inAppCallFailedWithResponse =
        (dynamic arguments) => inAppCallFailedWithResponseHandler(arguments);

    MappSdk.didReceiveInBoxMessage =
        (dynamic arguments) => didReceiveInBoxMessageHandler(arguments);

    MappSdk.handledRemoteNotification =
        (dynamic arguments) => remoteNotificationHandler(arguments);

    MappSdk.handledRichContent =
        (dynamic arguments) => richContentHandler(arguments);

    MappSdk.handledPushOpen =
        (dynamic arguments) => pushOpenedHandler(arguments);

    MappSdk.handledPushDismiss =
        (dynamic arguments) => pushDismissedHandler(arguments);

    MappSdk.handledPushSilent =
        (dynamic arguments) => pushSilentHandler(arguments);

    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Mapp SDK Demo'),
            backgroundColor: Theme.of(context).primaryColorDark,
          ),
          body: _buildListView(context)),
    );
  }
}
