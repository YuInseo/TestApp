# TestApp

간단한 안드로이드 앱 (Kotlin + Jetpack Compose).

## 기능
- 이름을 입력하면 한국어 인사말 표시
- "눌러보세요" 버튼으로 카운터 증가

## 빌드 환경
- Android Studio Hedgehog 이상 권장
- JDK 17
- Gradle 8.7 (wrapper)
- Android SDK 34, minSdk 24

## 실행 방법

### Android Studio에서 실행
1. Android Studio에서 이 프로젝트 폴더를 엽니다.
2. Gradle Sync가 끝날 때까지 기다립니다.
3. 안드로이드 폰을 USB로 연결하고 **개발자 옵션 > USB 디버깅**을 켭니다.
4. 상단의 ▶ Run 버튼을 누르면 폰에 설치 후 실행됩니다.

### 명령줄(CLI)에서 빌드 & 설치
```bash
# 디버그 APK 빌드
./gradlew assembleDebug

# 연결된 폰에 설치 + 실행
./gradlew installDebug
adb shell am start -n com.example.testapp/.MainActivity

# 또는 APK 파일을 직접 설치
adb install app/build/outputs/apk/debug/app-debug.apk
```

빌드된 APK는 `app/build/outputs/apk/debug/app-debug.apk` 에 생성됩니다.
