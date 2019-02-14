# 百度人脸采集组件

## 总体介绍

​	组件所在源码路径为https://github.com/SunflowerGJ/react-native-baiduAI

## 使用介绍

### npm 安装

1.在项目根目录的package.js里添加  "react-native-baiduAI": "git+https://github.com/SunflowerGJ/react-native-baiduAI.git"

2.执行 npm install

### IOS

配置
1. 在项目中选中Libraries文件夹，右键选择add File to "你的工程"，在node_modules文件夹下找到react-native-baiduAI/ios/RNBaiduAI.xcodeproj，add到项目中
2. 选中项目在targets中的Build Phases选项
 （1 找到Link Binary and Libraries并添加 libRNBaiduAI.a
（2 找到Copy Bundle Resources并添加```idl-license-Enp.face-ios ， idl-license-Com.face-ios，com.baidu.idl.face.faceSDK.bundle，com.baidu.idl.face.model.bundle```四个文件
3. 选中项目在targets中的Build Settings：
(1)  找到Framework Search Paths ，在右边添加路径 ```$(SRCROOT)/../node_modules/react-native-baiduAI/ios/RNBaiduAI/Frameworks/FaceSDK ```
(2) 找到Header Search Paths ，在右边添加路径```$(SRCROOT)/../node_modules/react-native-baiduAI/ios/RNBaiduAI```

以上都不用选Copy items if needed 


### android

待补充

### 使用方式

引入包文件
import {BaiduAIView} from 'react-native-baiduAI'
使用方法：
```
<BaiduAIView style={{height: global.appStyles.cssSize.DeviceHeight}}
                               ref='detectionView'
                               onRecongnizeFinish={(event) => { //识别结果
                                   if (event && event.nativeEvent) {
                                       let data = event.nativeEvent;
                                       if (data.isSuccess) {
                                           let imgData = data.imgData;

                                           let baseImg = 'data:image/png;base64,' + imgData;
                                           let list = this.state.imageList;
                                           list.push(baseImg)
                                           this.setState({
                                               imageList: new Array().concat(list),
                                           })
                                           console.log('react--得到人脸照片:' + baseImg.length);
                                           this._uploadFacePhoto(baseImg);
                                       } else {
                                           alert('识别失败');
                                       }
                                   }
                                   console.log('react-识别完毕：' + event)
                               }}
                >
                </BaiduAIView>
```
重新采集：
```
_reload = () => {
        if (global.myActions.currentScene == 'faceRecongnize') {
            this.setState({
                imageList: [],
            })
            this.refs.detectionView.reload()
        }
    }
```
开启采集线程
```
 this.refs.detectionView && this.refs.detectionView.start();
```
关闭线程：
```
 this.refs.detectionView && this.refs.detectionView.destroy();
```
