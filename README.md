# diffupdate
android实现增量更新

---
注: 里面的4个 todo 需要额外注意
---
## 增量更新原理
原理和介绍网上讲的都很多了, 我这里就不介绍了, 感兴趣的小伙伴可以搜来看看, 本文使用bsdiff工具来实现 Android 的增量更新
环境: windows10, Android Studio 4.1.1
> *注意: 下载文件需要APP开放存储权限, 这部分需要自己开发实现

## 在现有项目上增加增量更新功能
### 1. 复制github上 diffupdate 文件夹, 并将项目添加到as项目中
> 如需修改包名, 请注意 `diffupdate/src/main/cpp/update-lib.cpp` 路径第 10 行也需要一起修改
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210326143650492.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2w3MDcyNjg3NDM=,size_16,color_FFFFFF,t_70)


> 修改 根目录下 `settings.gradle` 文件, 将 `include ':diffupdate'` 加入到项目当中
> `app/build.gradle` 新增引入当前module, `    implementation project(path: ':diffupdate')`

### 2. Link c++
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210326141901447.png)
等待重新编译即可
### 3. 在需要更新的地方调用
```java
/**
 *
 * @param autoInstall 是否直接安装, 如果为false, 则弹出dialog, 提示用户更新信息并在确定后进行安装
 */
UpdateUtil.updateApp(mContext, true);
```
### 4. 测试更新功能
1. 打包当前apk
2. 修改内容, 打包新的apk, 注意打包新的apk时, 需要增加`versionName`的版本号
3. 使用 `tools/windows/获取查分文件.bat` , 获取查分文件patch.patch 和 md5 值
4. 修改服务器代码, 新增两个方法 `getAppUpdateInfo` 和 `downloadApp`
> 注意: 服务器端url需要修改文件 `com.liyk.app.diffupdate.network.GetUpdateInfo.GET_UPDATE_INFO_URL`

java服务器参考代码
```java

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/diffUpdate")
public class DiffUpdate {

    @GetMapping("getAppUpdateInfo")
    public Map<String, Object> getAppUpdateInfo(String versionCode){
        Map<String, Object> result = new HashMap<>();
        if ("1.0".equals(versionCode)) {
            result.put("needUpdate", true);
            result.put("patchFileUrl", "patch.patch");
            result.put("updatedMd5", "xxx");
            result.put("modifyContent", "更新了版本号而已");
            result.put("updatedVersionCode", "1.0.1");
        } else {
            result.put("needUpdate", false);
        }
        return result;
    }

    @RequestMapping(value = "downloadApp")
    public ResponseEntity<byte[]> downloadApp(HttpServletRequest request, HttpServletResponse response, String patchFileUrl) throws Exception {
        if (StringUtils.isEmpty(patchFileUrl)) {
            return null;
        }
        String rootPath = "e:/demo/" + patchFileUrl;
        File file = new File(rootPath);
        InputStream is = null;
        if (file.exists()) {
            try {
                byte[] body = null;
                is = new FileInputStream(file);
                body = new byte[is.available()];
                int read = is.read(body);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attchement;filename=patch.patch");
                HttpStatus statusCode = HttpStatus.OK;
                return new ResponseEntity<>(body, headers, statusCode);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return null;
    }
}

```
