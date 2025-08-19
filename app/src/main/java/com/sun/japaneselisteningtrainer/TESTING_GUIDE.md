# 🧪 AudioService Testing Guide

## 🚀 **Bước 1: Chuẩn bị Test**

### 1.1 Enable Test Screen
✅ MainActivity.kt đã được cập nhật với `TestAudioPlayerIntegration()`

### 1.2 Build và Run App
```bash
# Build project
./gradlew build

# Hoặc từ Android Studio: Build > Rebuild Project
```

## 📱 **Bước 2: Test Database Integration**

### Test Case 1: Empty Database
1. **Run app** 
2. **Expected**: Thấy "No audios in database"
3. **Action**: Tap "Add Sample Audio"
4. **Expected**: Sample audio xuất hiện trong list
5. **Verify**: Title, ID, listen times hiển thị đúng

### Test Case 2: Audio Selection
1. **Action**: Tap "Select" trên audio
2. **Expected**: FilterChip được highlight
3. **Action**: Tap "Open Player with Audio [ID]"
4. **Expected**: Navigate đến MusicPlayerScreen

## 🎵 **Bước 3: Test Audio Playback**

### Test Case 3: Service Connection
1. **Expected**: Loading spinner xuất hiện
2. **Expected**: "Đang tải audio..." text
3. **After 2-3s**: UI chuyển sang player hoặc error

### Test Case 4: Audio Loading
**Scenario A: Success**
- **Expected**: Player UI hiển thị
- **Expected**: Audio title từ database
- **Expected**: Controls enabled

**Scenario B: Error (No file)**
- **Expected**: Error message hiển thị
- **Expected**: "Thử lại" button
- **Action**: Tap "Thử lại" → reload

### Test Case 5: Playback Controls
1. **Play/Pause Button**:
   - Tap play → Audio bắt đầu (nếu có file)
   - RotatingDisc bắt đầu quay
   - Button chuyển thành pause icon

2. **Progress Bar**:
   - Drag để seek → Position thay đổi
   - Time labels update real-time

3. **Transport Controls**:
   - Previous/Next buttons
   - Shuffle toggle (icon thay đổi màu)

## 🔔 **Bước 4: Test Background Service**

### Test Case 6: Foreground Service
1. **Start playback**
2. **Expected**: Notification xuất hiện
3. **Expected**: Notification shows audio title
4. **Expected**: Play/pause/previous/next buttons

### Test Case 7: Background Playback
1. **Start audio playback**
2. **Minimize app** (Home button)
3. **Expected**: Audio continues playing
4. **Expected**: Notification still visible
5. **Test notification controls**:
   - Tap pause → Audio stops
   - Tap play → Audio resumes
   - Tap next/previous → Works

### Test Case 8: App Lifecycle
1. **Play audio → Minimize → Reopen app**
2. **Expected**: UI sync với service state
3. **Expected**: Controls reflect current state
4. **Kill app → Reopen**
5. **Expected**: Service stops, no audio

## 📊 **Bước 5: Test Database Updates**

### Test Case 9: Favorite Toggle
1. **Initial**: Heart icon empty
2. **Tap favorite** → Icon fills, color changes
3. **Back to test screen**
4. **Expected**: Favorite status persisted
5. **Reopen player** → Favorite still active

### Test Case 10: Listen Times
1. **Note current listen times** (trong test screen)
2. **Play audio** for a few seconds
3. **Back to test screen**
4. **Expected**: Listen times increased by 1
5. **Play again** → Times increase again

## 🐛 **Bước 6: Test Error Scenarios**

### Test Case 11: No Audio File
1. **Audio có filePath invalid**
2. **Expected**: Playback error
3. **Expected**: ExoPlayer error handling
4. **Expected**: UI shows error state

### Test Case 12: Service Disconnect
1. **Force close AudioService** (Developer options)
2. **Expected**: UI shows disconnected state
3. **Expected**: Auto-reconnect attempts

### Test Case 13: Database Errors
1. **Corrupt audioId** (không tồn tại)
2. **Expected**: "Không tìm thấy audio với ID: X"
3. **Expected**: Graceful error handling

## ✅ **Expected Results Summary**

### Database Layer
- ✅ SQLite operations work
- ✅ Repository pattern functional  
- ✅ Real-time data updates
- ✅ State persistence

### Service Layer
- ✅ AudioService starts/stops correctly
- ✅ ExoPlayer integration works
- ✅ Foreground service + notification
- ✅ Background playback
- ✅ Service binding/unbinding

### UI Layer
- ✅ StateFlow reactive updates
- ✅ Loading/error states
- ✅ Control synchronization
- ✅ Navigation works

### Integration
- ✅ End-to-end data flow
- ✅ Error handling
- ✅ Performance acceptable
- ✅ Memory management

## 🔧 **Debug Tips**

### Check Logs
```bash
# Filter AudioService logs
adb logcat | grep -E "(AudioService|AudioPlayer|AudioNotification)"

# Check for errors
adb logcat | grep -E "(ERROR|Exception)"
```

### Common Issues
1. **No audio plays**: Check file path validity
2. **Service doesn't start**: Check permissions
3. **Notification missing**: Check notification channel
4. **Database empty**: Use "Add Sample Audio"
5. **Crashes**: Check null safety

### Performance Check
- Monitor memory usage
- Check for memory leaks
- Verify service cleanup
- Test rotation/configuration changes

## 🎯 **Success Criteria**

Your AudioService integration is **successful** if:

✅ Database operations work smoothly  
✅ Audio loads and plays correctly  
✅ Background service functions properly  
✅ UI stays synchronized with service  
✅ Error handling is graceful  
✅ Performance is acceptable  
✅ No crashes or memory leaks  

**Ready to test!** 🚀
