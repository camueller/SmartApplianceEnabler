# Notifications
The *Smart Appliance Enabler* provides notifications about events ("device was switched on", "communication error") as soon as they occur.

A main use case for me is notification when devices are unreachable. It happens to me again and again that devices with Tasmota are no longer in the WLAN at some point and I don't notice it right away. Then, of course, the consumption in the *Sunny Home Manager* is missing. But it's also nice if you get a notification when the washing machine is finished or the car is fully charged.

Before notifications can be configured, there must be a [shell script configured in the settings](Settings_DE.md#user-content-notifications) via which the *Smart Appliance Enabler* sends the notifications.

After a shell script has been configured, the sending of device-specific notifications can be activated for counters and switches by activating the checkbox `Activated`.

Without selecting individual events, the notification is for all events:

![Alle Ereignisse](../pics/fe/NotificationsAll.png)

Alternatively, the notification can be limited to individual events:

![Ausgewählte Ereignisse](../pics/fe/NotificationsSome.png)

## Messenger platforms
There are shell scripts for the *Smart Appliance Enabler* to use the messanger platforms described below. With little shell script know-how, it should be possible to create shell scripts for other notification channels based on the existing shell scripts. Please also make these scripts available to the other *Smart Appliance Enabler* users as a **pull request**!

### Telegram (https://telegram.org)
The shell script for use as a notification channel in *Smart Appliance Enabler* is https://github.com/camueller/SmartApplianceEnabler/blob/master/run/notifyWithTelegram.sh.

First, a [group](https://telegram.org/faq/de#f-wie-kann-ich-eine-gruppe-erstellen-erstellen) must be created in Telegram, which you could name e.g. "House".

Add the people who should receive the notifications to this group as usual.

A [bot must be created](https://core.telegram.org/bots#3-how-do-i-create-a-bot) for each device that should send notifications. A "username" must be specified, which must be unique telegram-wide (!) and end with `_bot`. It is safe to assume that `washing_machine_bot` is already taken. If the bot was created successfully, a `Token to access HTTP API` will be provided in the confirmation message. This token should be secured and not shared with anyone. In the *Smart Appliance Enabler*, this token must be entered in the device configuration in the field `Device ID for notifications`.

In the dialog with the "BotFather" you can also assign photos to the bots.

These [bots must be added to the previously created group](https://telegram.org/faq/en#f-how-can-ich-ich-more-members-added-and-was-ist-ein-invite).

The chat ID of the group is required to send notifications. This is displayed when you add the `@getidsbot` bot to the group. Once the ID has been determined, the `@getidsbot` can be removed from the group.

A shell script](ManualInstallation_DE.md#user-content-notifications) is provided for the use of [Notifications via Telegram]. The previously determined chat ID must be entered in this shell script (possibly existing minus sign must be accepted):
```
chat=-123456789
```

Now nothing stands in the way of receiving notifications:

![Telegram](../pics/Telegram.jpg)

### Signal (https://signal.org/)
The shell script for using it as a notification channel in *Smart Appliance Enabler* is https://github.com/camueller/SmartApplianceEnabler/blob/master/run/notifyWithSignal.sh.

### Prowl (https://www.prowlapp.com)
The shell script for using it as a notification channel in *Smart Appliance Enabler* is https://github.com/camueller/SmartApplianceEnabler/blob/master/run/notifyWithProwl.sh.