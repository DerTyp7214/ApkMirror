@echo off
del app\release\app-release.apk
del app\build\outputs\apk\release\app-release-unsigned.apk
del app\build\outputs\apk\release\app-release-unsigned-aligned.apk
call gradlew assembleRelease
call zipalign -v -p 4 app\build\outputs\apk\release\app-release-unsigned.apk app\build\outputs\apk\release\app-release-unsigned-aligned.apk
call apksigner sign --ks ..\..\Android\key-Hacker-Boing747.jks --out app\release\app-release.apk app\build\outputs\apk\release\app-release-unsigned-aligned.apk
sh release.sh %1 %2