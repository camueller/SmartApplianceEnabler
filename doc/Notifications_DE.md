# Benachrichtigungen

Durch Benachrichtigungen kann der *Smart Appliance Enabler* über Ereignisse ("Gerät wurde eingeschaltet", "Kommunikationsfehler") informieren.

Für die Nutzung von Benachrichtigungen muss ein [Shell-Script in den Einstellungen konfiguriert](Settings_DE.md#benachrichtigungen) sein.

Nachdem ein Shell-Script konfiguriert wurde, kann für Zähler und Schalter durch Aktivieren der Checkbox `Aktiviert` das Versenden von Benachrichtigungen gerätespezifisch aktiviert werden.

Ohne Auswahl einzelner Ereignisse erfolgt die Benachrichtiung für alle Ereignisse:

![Alle Ereignisse](../pics/fe/NotificationsAll.png)

Alternativ kann die Benachrichtiung auf einzlene Ereignisse beschränkt werden:

![Ausgewählte Ereignisse](../pics/fe/NotificationsSome.png)

### Telegram
Zunächst muss in Telegram eine [Gruppe erstellt](https://telegram.org/faq/de#f-wie-kann-ich-eine-gruppe-erstellen) werden, die man z.B. "Haus" nennen könnte.

Für jedes Gerät, das Benachrichtigungen versenden soll, muss ein [Bot anlegt](https://core.telegram.org/bots#3-how-do-i-create-a-bot) werden. Dabei muss ein "Username" angegeben werden, der Telegramweit (!) eindeutig sein und auf `_bot` enden muss. Man kann davon ausgehen, dass `waschmaschine_bot` bereits vergeben ist. Wenn der Bot erfolgreich angelegt wurde, wird in der Bestätigungsnachricht ein `Token to access HTTP API` mitgeteilt. Diesen Token sollte sollte man sichern und niemandem mitteilen. Im *Smart Appliance Enabler* muss dieser Token in der Geräte-Konfiguration in das Feld `Geräte-ID für Benachrichtigungen` eingetragen werden.

Im Dialog mit dem "BotFather" kann man den Bots auch Fotos zuweisen.

Diese [Bots müssen der zuvor angelegten Gruppe hinzugefügt](https://telegram.org/faq/de#f-wie-kann-ich-mehr-mitglieder-hinzufgen-und-was-ist-ein-einladu) werden.

Zum Versenden von Benachrichtigungen wird die Chat ID der Gruppe benötigt. Diese wird angezeigt, wenn man den Bot `@getidsbot` zur Gruppe hinzufügt. Wenn man die ID ermittelt hat, kann der `@getidsbot` wieder aus der Gruppe entfernt werden.

Für die Nutzung von [Benachrichtigungen via Telegram wird ein Shell-Script](Installation_DE.md#Benachrichtigungen) bereitgestellt. In diesem Shell-Script muss die zuvor ermittelte Chat ID eingetragen werden (ggf. vorhandenes Minus-Zeichen muss übernommen werden):
```
chat=-123456789
```

Jetzt steht dem Empfang von Benachrichtigungen nichts mehr im Wege:

![Telegram](../pics/Telegram.jpg)
