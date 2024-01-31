<div align="center">
<h1>Pinning App</h1>

![stars](https://img.shields.io/github/stars/HChenX/PinningApp?style=flat)
![downloads](https://img.shields.io/github/downloads/HChenX/PinningApp/total)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/PinningApp)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/PinningApp)](https://github.com/HChenX/PinningApp/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date/HChenX/PinningApp)](https://github.com/HChenX/PinningApp/releases)
![last commit](https://img.shields.io/github/last-commit/HChenX/PinningApp?style=flat)
![language](https://img.shields.io/badge/language-java-purple)

<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>
</div>

### Module Introduction:

- The module achieves the fixation of applications by invoking the native Android fixed application
  functionality.
- Some of the upper-level structures include:
- Triggering fixation methods, ignoring exit gestures, rejecting the pop-up sidebar, exiting
  fixation on screen-off,
- Pad: Rejecting the call-out of the Dock bar, rejecting the response to the small window gesture,
  hiding the small white bar.
- All the above functions are implemented by the module itself and do not rely on the native Android
  fixed application functionality.
- ~~This module's functionality is similar to IOS's guided access.~~

### Module Effects:

- When entering fixation mode, the phone will be fixed to display a specific app, and system
  gestures such as back will be disabled.
- The GIF animation below illustrates this:

![GIF](https://github.com/HChenX/PinningApp/blob/master/pinning_app_gif.gif)

### Usage:

- First, make sure you are using a Xiaomi phone as the module only supports Xiaomi phones! Of
  course, if you have a way, you can contribute to this project to achieve broader support!
- Next, download the module and check the recommended scopes, then restart your phone!
- After the restart, the module will be running.

#### Turning On/Off Fixation Mode:

- Long press the notification bar in the application interface for 1-2 seconds to enter fixation
  mode. If it fails, make sure your finger did not move during the process!
- There will be a Toast notification when entering; if you see the notification, it indicates
  successful entry.
- Long press the notification bar again in the app interface for 1-2 seconds to exit fixation mode.
  There will also be a Toast notification for successful exit.

##### Turning On/Off 'Lock Screen on Exit' Feature:

- Open MT or any other shell command execution software.
- Execute su to obtain ROOT permission; this is necessary!
- Execute pm pinning -l 0 to turn off this feature;
- Execute pm pinning -l 1 to turn on this feature.
- Execute pm pinning -l -g to get the status of this feature.
- For detailed help, execute pm pinning -h.

###### Effects of 'Lock Screen on Exit' Feature:

- When enabled, exiting fixation mode will force the device to go to the lock screen password
  interface.
- ~~Now you don't have to worry about others seeing your private information!~~

##### Turning On/Off 'Reject Sidebar Popup during Fixation' Feature:

- Open MT or any other shell command execution software.
- Execute su to obtain ROOT permission; this is necessary!
- Execute pm pinning -s 0 to turn off this feature;
- Execute pm pinning -s 1 to turn on this feature.
- Execute pm pinning -s -g to get the status of this feature.
- For detailed help, execute pm pinning -h.

###### Effects of 'Reject Sidebar Popup during Fixation' Feature:

- As the name suggests, when enabled, you cannot bring up the sidebar during fixation mode.

#### Common Issues During Usage:

- Q: Why didn't I successfully enter? A: The long-press duration may be too short or your finger may
  have moved; please try again.
- Q: Why is long-pressing on the home screen not working? A: The module does not currently support
  entering fixation mode on the home screen.
- Q: Why does it prompt "Please unlock in the locked app interface"? A: You may have switched
  interfaces through some means; unlock in the app interface where you entered fixation mode.
  However, don't worry; it will automatically release the fixation mode after a certain number of
  attempts.
- Q: Why is the module not working? A: Please provide logs such as LSP for feedback. Although the
  probability of fixing it is low, I appreciate your feedback.

### Project Acknowledgments:

- Thanks to the Android team for providing the basic underlying support for this feature!
- Thanks to the Xposed framework for providing powerful hook support!
- Thanks to the DexKit tool for supporting the functionality of this module!
- Thanks to [Sevtinge](https://github.com/Sevtinge) for contributing to the adaptation of this
  module for Pad!

### Project Requirements:

- This project is entirely open source!
- However, please comply with the GPL 3.0 open source license!
- Any use of this module must include the author and GitHub address!
- If you have any requirements for this module, please do not relentlessly ask the author; please
  submit a pull request, and I am not obligated to implement your functionality or adapt it to your
  phone!

### Communication Group:

- QQ: 517788148
- Telegram: t.me/HChen_AppRetention

### Sponsorship:

- Afdian: [焕晨HChen](https://afdian.net/a/HChen)

