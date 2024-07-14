![image](https://raw.githubusercontent.com/AshiVered/support-israel-banner/main/assets/support-israel-banner.jpg)


# TitanPad - simple and lightweight keyboard app for Unihertz Titan Pocket and compatible keypad Android phones
Forked from https://gitlab.com/suborg/qinpad.
# Features

- Two layouts: Engish and Hebrew.
- Layout switching with `alt` key, Caps switching with `shift` key.
- Special character input in `sym` key.
- Non-invasive passing input control to the system in number entry fields.


# Building

The app project is compatible with Android Studio 3.3.2. Just clone the repo, import the project and build it with the Studio distribution.

# Installation/Update

Enable Developer settings and USB debugging. Then run:

```
adb install [your_built_apk]
```

To update the version, run:

```
adb uninstall aiv.ashivered.titanpad
adb install [your_new_apk]
```

# Initial setup

Start the keyboard from the main menu. You'll be taken to the standard Android input method selection dialog (hidden by default on Xiaomi Qin 1s). Set the mark on QinPad and remove it from the stock keyboard.

You'll need to do this after each version update.
