# 🐛 DEBUG COMMANDS FOR AUDIO SERVICE

## Check Service Status
```bash
# Check if AudioService is running
adb shell dumpsys activity services | grep AudioService

# Check app processes
adb shell ps | grep japaneselistening
```

## Check Notifications
```bash
# Check notification permissions
adb shell dumpsys notification | grep japaneselistening

# Check notification channels
adb shell cmd notification list_channels com.sun.japaneselisteningtrainer
```

## Logcat Monitoring
```bash
# Monitor AudioService logs
adb logcat | grep "AudioService\|AudioPlayer\|AudioServiceManager"

# Monitor ExoPlayer logs
adb logcat | grep "ExoPlayer"

# Monitor all app logs
adb logcat | grep "com.sun.japaneselistening"
```

## Clear App Data (if needed)
```bash
# Reset app completely
adb shell pm clear com.sun.japaneselisteningtrainer
```

## Install Debug APK
```bash
# Build and install
./gradlew installDebug

# Force stop and restart
adb shell am force-stop com.sun.japaneselisteningtrainer
adb shell am start -n com.sun.japaneselisteningtrainer/.MainActivity
```
