bsdiff.exe old.apk new.apk patch.patch
bspatch.exe old.apk new_temp.apk patch.patch
certutil -hashfile new.apk MD5
certutil -hashfile new_temp.apk MD5
pause