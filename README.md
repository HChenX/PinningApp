<div align="center">
<h1>固定应用(Pinning App)</h1>

![stars](https://img.shields.io/github/stars/HChenX/PinningApp?style=flat)
![downloads](https://img.shields.io/github/downloads/HChenX/PinningApp/total)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/PinningApp)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/PinningApp)](https://github.com/HChenX/PinningApp/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date/HChenX/PinningApp)](https://github.com/HChenX/PinningApp/releases)
![last commit](https://img.shields.io/github/last-commit/HChenX/PinningApp?style=flat)
![language](https://img.shields.io/badge/language-java-purple)

<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>
</div>

### 模块介绍:

- 模块通过调用安卓自有的固定应用功能实现对应用的固定。
- 上层建筑比如：
- 触发固定方式、忽略退出手势、拒绝弹出侧边栏、退出固定息屏、
- Pad： 拒绝呼出Dock栏，拒绝响应小窗手势，隐藏小白条。
- 等上述功能均为模块自身实现，不依赖原生安卓的固定应用功能。
- ~~本模块功能类似于IOS的引导式访问~~

### 模块效果：

- 当进入固定模式时，手机将被固定显示一个App，系统返回等手势将失效。
- 如下是GIF动图展示：

![GIF](https://github.com/HChenX/PinningApp/blob/master/pinning_app_gif.gif)

- 无法查看点击[此处](https://github.com/HChenX/PinningApp/blob/master/pinning_app_gif.gif)

### 使用方法：

- 首先确定你是小米手机，模块只支持小米手机！当然如果你有办法可以为本项目PR，来获得更广泛的支持！
- 其次下载本模块并勾选推荐的作用域，然后重启手机！
- 重启完成模块即正在运行。

##### 开关固定模式：

- 在应用界面长按通知栏1~2秒即可进入，如果失败请确定期间手指没有移动！
- 进入时会有Toast提示，如果出现提示即成功进入
- 再次在本应用界面长按通知栏1~2秒即可退出！同样也会有Toast提示！

##### 开关`退出固定时锁屏`功能：

- 打开MT或其他可以执行Shell命令的软件。
- 执行`su`回车获取ROOT权限，这是必要的！！
- 执行`pm pinning -l 0`为关闭本功能；
- 执行`pm pinning -l 1`为开启本功能。
- 执行`pm pinning -l -g`可获取本功能状态。
- 获取详细帮助可执行`pm pinning -h`

###### `退出固定时锁屏`功能效果：

- 开启后在退出固定模式时将强制跳转到锁屏密码界面。
- ~~妈妈再也不用担心手机隐私被别人看见啦！~~

##### 开关`固定时拒绝弹出侧边栏`功能：

- 打开MT或其他可以执行Shell命令的软件。
- 执行`su`回车获取ROOT权限，这是必要的！！
- 执行`pm pinning -s 0`为关闭本功能；
- 执行`pm pinning -s 1`为开启本功能。
- 执行`pm pinning -s -g`可获取本功能状态。
- 获取详细帮助可执行`pm pinning -h`

###### `固定时拒绝弹出侧边栏`功能效果：

- 顾名思义，开启后进入固定模式不能呼出侧边栏。

##### 使用时常见问题：

- Q：为什么我没有成功进入？ A：可能长按时间偏短或者手指移动，请再试。
- Q：为什么在桌面时长按无效？ A：模块暂不支持在桌面时进入固定模式。
- Q：为什么提示“`请在被锁定应用界面解锁”？ A：你通过某种手段切换了界面，需要在进入固定模式的App界面解锁。但无需担心，超过一定次数会自动解除固定模式。
- Q：为什么模块没有工作？ A：请带上Lsp日志等信息反馈，虽然我修的概率很小。

### 项目感谢：

- 感谢Android官方提供本功能的基本底层支持！
- 感谢Xposed工具提供强大的Hook支持！
- 感谢DexKit工具对本模块功能性支持！
- 感谢[Sevtinge](https://github.com/Sevtinge)对本模块适配Pad做出的贡献！

### 项目要求：

- 本项目完全开源！
- 但是也请遵守GPL3.0的开源协议！
- 任何使用本模块必须注明作者和GitHub地址！
- 对本模块有任何要求请不要疯狂Call作者，请自行PR，我没有义务实现你的功能或适配你的手机！

### 交流群：

- QQ：517788148
- 电报：t.me/HChen_AppRetention

### 赞助我：

- 爱发电：[焕晨HChen](https://afdian.net/a/HChen)

