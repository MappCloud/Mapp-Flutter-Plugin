enum SERVER { L3, L3_US, EMC, EMC_US, CROC, TEST, TEST55 }


class Config{
  static const String sdkKey="17d09eb9903618.30242929";
  static const String appID="300551";
  static const String googleProjectId="785651527831";
  static const String tenantID="33";
  static const server=SERVER.TEST;

  static final defaultConfig=[Config.sdkKey, Config.googleProjectId, Config.server.index, Config.appID, Config.tenantID];
}
