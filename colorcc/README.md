# ColorCC for Miku-Picked UI

ColorCC is a ColorOS-style control centre by **Guest** (gtsw1 / @Guestoss,
<https://github.com/addictedcaf/ColorCC>), originally an LSPosed module for One UI 9 / 8.5.
© 2026 Guest. The author lost the sources and agreed to this port into Miku-Picked UI.

This holds that code, recovered from the ColorCC 0.99.68 release APK with jadx
(unchanged) and then ported to AOSP SystemUI. It lives in `vendor/miku/colorcc`.

- `java/dev/colorcc`: the panel. `Hook.java` no longer installs Xposed hooks: SystemUI's
  `com.android.systemui.picked.colorcc.ColorCcHost` (frameworks/base, branch
  `qs-control-centre`) places the panel and answers its questions through `Hook.Bridge`.
  `XposedHelpers.java` keeps the module's reflective calls working without Xposed.
- `res`: the module's strings (English, French, Indonesian, Italian, Turkish), shipped as the
  `dev.colorcc` package the panel reads them from.

Selected with Settings.Secure `qs_panel_style` = 7.
