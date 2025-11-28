# Dynamic Island Android

Ứng dụng Android tạo hiệu ứng Dynamic Island giống iOS. Hiển thị thông báo từ các ứng dụng khác dưới dạng Dynamic Island với animation mượt mà.

## Tính năng

- **Dynamic Island UI**: Giao diện giống iOS với thiết kế dark rounded pill
- **Notification Listener**: Lắng nghe thông báo từ tất cả các ứng dụng
- **Expand/Collapse**: Nhấn để mở rộng xem nội dung đầy đủ
- **Action Buttons**: Hiển thị các nút hành động của thông báo (nếu có)
- **Animation mượt mà**: Hiệu ứng overshoot và fade khi hiển thị/ẩn
- **Auto-hide**: Tự động ẩn sau thời gian cấu hình
- **Boot Receiver**: Tự động khởi động sau khi reboot thiết bị

## Yêu cầu

- Android 8.0 (API Level 26) trở lên
- Khoảng 5MB dung lượng

## Quyền cần thiết

- `SYSTEM_ALERT_WINDOW`: Hiển thị overlay trên các ứng dụng khác
- `BIND_NOTIFICATION_LISTENER_SERVICE`: Đọc thông báo từ các ứng dụng
- `POST_NOTIFICATIONS`: Hiển thị thông báo foreground service
- `RECEIVE_BOOT_COMPLETED`: Tự động khởi động sau boot

## Cài đặt

### Từ GitHub Releases

1. Vào tab [Releases](../../releases) của repository
2. Tải file APK mới nhất
3. Cài đặt trên thiết bị Android
4. Cấp quyền khi được yêu cầu
5. Bật Dynamic Island từ ứng dụng

### Build từ source

```bash
# Clone repository
git clone https://github.com/your-username/dynamic-island-android.git
cd dynamic-island-android

# Build debug APK
./gradlew assembleDebug

# APK sẽ ở: app/build/outputs/apk/debug/app-debug.apk
```

## Cấu trúc Project

```
app/src/main/
├── java/com/dynamicisland/android/
│   ├── service/
│   │   ├── DynamicIslandService.kt    # NotificationListenerService
│   │   ├── OverlayService.kt          # Overlay Window Manager
│   │   └── BootReceiver.kt            # Auto-start on boot
│   ├── ui/
│   │   └── MainActivity.kt            # Main Activity
│   └── util/
│       ├── NotificationData.kt        # Data Models
│       ├── PreferencesManager.kt      # Settings Management
│       └── AppUtils.kt                # Utility Functions
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   └── dynamic_island_layout.xml
│   ├── drawable/                      # Graphics & Shapes
│   ├── anim/                          # Animations
│   └── values/                        # Strings, Colors, Themes
└── AndroidManifest.xml
```

## GitHub Actions

Project được cấu hình tự động build APK khi push lên GitHub:

1. Push code lên `main` hoặc `master` branch
2. GitHub Actions sẽ tự động build Debug và Release APK
3. APK có thể tải từ:
   - **Artifacts**: Trong tab Actions của mỗi workflow run
   - **Releases**: Tự động tạo release với APK

## Sử dụng

1. **Mở ứng dụng** lần đầu tiên
2. **Cấp quyền Notification Listener**: Nhấn vào card và cho phép trong Settings
3. **Cấp quyền Overlay**: Nhấn vào card và cho phép trong Settings
4. **Bật Dynamic Island**: Nhấn nút "Bật Dynamic Island"
5. **Test**: Nhấn "Gửi thông báo test" để kiểm tra

## Tùy chỉnh

Có thể thay đổi các thông số trong `PreferencesManager.kt`:

- `displayDuration`: Thời gian hiển thị (mặc định 4 giây)
- `isAutoExpand`: Tự động mở rộng (mặc định true)
- `excludedApps`: Danh sách ứng dụng bị loại trừ

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: Service-based
- **UI**: XML Layouts with ViewBinding
- **Build**: Gradle Kotlin DSL
- **CI/CD**: GitHub Actions

## License

MIT License - Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

## Đóng góp

Mọi đóng góp đều được hoan nghênh! Vui lòng tạo Pull Request hoặc Issue nếu bạn có ý tưởng cải tiến.
