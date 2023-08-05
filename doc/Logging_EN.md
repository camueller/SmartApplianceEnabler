# Logging
## Log files
The *Smart Appliance Enabler* generates the following log files:
- `/tmp/rolling-<date>.log` (e.g. `/tmp/rolling-2019-12-30.log`) contains the log output of the *Smart Appliance Enabler*
- `/var/log/smartapplianceenabler.log` contains the `stdout` output of the *Smart Appliance Enabler* process
- `/var/log/smartapplianceenabler.err` contains the `stderr` output of the *Smart Appliance Enabler* process

## Log output of *Smart Appliance Enabler*
The logging of the *Smart Appliance Enabler* was implemented in such a way that it makes transparent what the *Smart Appliance Enabler* is currently doing. Values are often also output before they are used for decisions. As a result, the log should also provide the best possible support for troubleshooting.

If the log entries refer to a specific device, they always contain the [`ID`](Appliance_EN.md#id) to enable filtering of the log entries:
```
2019-12-30 11:46:51,501 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:160] F-00000001-000000000015-00: Response code is 200
```

## Log level
The log level can be changed in the file `/opt/sae/logback-spring.xml`:
```
<logger name="de.avanux" level="debug" additivity="false">
```
The following values are possible:
- `debug` is set by default and should contain sufficiently detailed information in most cases
- `info` can be set if you want significantly less information in the log file
- `trace` can be set to log everything that is possible

## Storage of log files
By default, only the logs of the last 7 days are saved. If necessary, this can be changed in the file `/opt/sae/logback-spring.xml`:
```
<maxHistory>7</maxHistory>
```

## Helpful commands for log analysis
A few commands have proven to be very helpful for log analysis.

With the commands shown as an example, the date in the file name of the log file must always be adjusted!

### Track log live
#### Console
The following command continuously displays log outputs for all devices as they are written to the log file:
```console
sae@raspi ~ $ tail -f /tmp/rolling-2020-12-31.log
2020-12-31 11:23:19,671 DEBUG [pi4j-gpio-event-executor-94] d.a.s.c.StartingCurrentSwitch [StartingCurrentSwitch.java:325] F-00000001-000000000012-00: Finished current not detected.
2020-12-31 11:23:19,683 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000004-00: Executing timer task name=PollEnergyMeter id=21173788
2020-12-31 11:23:19,690 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000005-00: Executing timer task name=PollEnergyMeter id=24264618
```

If you have configured various appliances, this output is quite confusing. In this case, it makes sense to filter the log output so that you only see those of the appliance you are interested in:
```console
pi@raspi ~ $ tail -f /tmp/rolling-2020-12-31.log | grep --line-buffered F-00000001-000000000002-00
2020-12-31 11:23:19,695 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000002-00: Executing timer task name=PollPowerMeter id=26904981
2020-12-31 11:23:19,696 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:107] F-00000001-000000000002-00: Sending GET request url=http://wasserversorgung//cm?cmnd=Status%208
2020-12-31 11:23:19,841 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:168] F-00000001-000000000002-00: Response code is 200
```

#### <a name="webmin-logs"></a> Webmin

System -> System Logs
Click the file selection button to the right of `View log file`.

Navigate to `tmp` in the file selection dialog and select the appropriate file.

Now the name of the selected log file should appear after `View log file`.

After clicking the `View` button, the last 100 lines of the file are displayed.

Automatic updating of the display can be activated by clicking on the arrow on the right in the `View` button.

To only see the log entries of the device you are interested in, enter the ID of the device after `Only show lines with text`, e.g. `F-00000001-000000000002-00`.

## Check log for errors

If the *Smart Appliance Enabler* is suspected of malfunctioning, the log should be specifically searched for errors.

The *Smart Appliance Enabler* logs errors with the log level `ERROR`. i.e. this "word" can be used as a search criterion.
```console
sae@raspi:~ $ grep ERROR /tmp/rolling-2020-12-31.log
```

*Webmin*: In [View Logfile](#webmin-logs) after `Only show lines with text` enter `error ` with a space at the end (since the search is unfortunately not case-sensitive)!

If there is no output from this command, the log for that day does not contain any errors.
The cause of the error is usually written to the log together with the error. In order to display this cause of the error, the command must be expanded so that it also outputs the 2 lines before the error and 35 lines after the error:
```console
sae@raspi:~ $ grep ERROR -B 2 -A 35 /tmp/rolling-2020-12-31.log
2020-12-31 16:48:56,748 DEBUG [Timer-0] d.a.s.u.GuardedTimerTask [GuardedTimerTask.java:54] F-00000001-000000000006-00: Executing timer task name=PollPowerMeter id=25032537
2020-12-31 16:48:56,749 DEBUG [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:107] F-00000001-000000000006-00: Sending GET request url=http://thermomix/cm?cmnd=Status%208
2020-12-31 16:49:03,484 ERROR [Timer-0] d.a.s.h.HttpTransactionExecutor [HttpTransactionExecutor.java:120] F-00000001-000000000006-00: Error executing GET request.
java.net.SocketTimeoutException: Read timed out
        at java.base/java.net.SocketInputStream.socketRead0(Native Method)
        at java.base/java.net.SocketInputStream.socketRead(SocketInputStream.java:115)
        at java.base/java.net.SocketInputStream.read(SocketInputStream.java:168)
        at java.base/java.net.SocketInputStream.read(SocketInputStream.java:140)
        at org.apache.http.impl.io.SessionInputBufferImpl.streamRead(SessionInputBufferImpl.java:137)
        at org.apache.http.impl.io.SessionInputBufferImpl.fillBuffer(SessionInputBufferImpl.java:153)
        at org.apache.http.impl.io.SessionInputBufferImpl.readLine(SessionInputBufferImpl.java:280)
        at org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead(DefaultHttpResponseParser.java:138)
        at org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead(DefaultHttpResponseParser.java:56)
        at org.apache.http.impl.io.AbstractMessageParser.parse(AbstractMessageParser.java:259)
        at org.apache.http.impl.DefaultBHttpClientConnection.receiveResponseHeader(DefaultBHttpClientConnection.java:163)
        at org.apache.http.impl.conn.CPoolProxy.receiveResponseHeader(CPoolProxy.java:157)
        at org.apache.http.protocol.HttpRequestExecutor.doReceiveResponse(HttpRequestExecutor.java:273)
        at org.apache.http.protocol.HttpRequestExecutor.execute(HttpRequestExecutor.java:125)
        at org.apache.http.impl.execchain.MainClientExec.execute(MainClientExec.java:272)
        at org.apache.http.impl.execchain.ProtocolExec.execute(ProtocolExec.java:186)
        at org.apache.http.impl.execchain.RetryExec.execute(RetryExec.java:89)
        at org.apache.http.impl.execchain.RedirectExec.execute(RedirectExec.java:110)
        at org.apache.http.impl.client.InternalHttpClient.doExecute(InternalHttpClient.java:185)
        at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:83)
        at org.apache.http.impl.client.CloseableHttpClient.execute(CloseableHttpClient.java:108)
        at de.avanux.smartapplianceenabler.http.HttpTransactionExecutor.get(HttpTransactionExecutor.java:116)
        at de.avanux.smartapplianceenabler.http.HttpTransactionExecutor.executeLeaveOpen(HttpTransactionExecutor.java:96)
        at de.avanux.smartapplianceenabler.http.HttpTransactionExecutor.execute(HttpTransactionExecutor.java:76)
        at de.avanux.smartapplianceenabler.http.HttpHandler.getValue(HttpHandler.java:85)
        at de.avanux.smartapplianceenabler.http.HttpHandler.getFloatValue(HttpHandler.java:45)
        at de.avanux.smartapplianceenabler.meter.HttpElectricityMeter.getValue(HttpElectricityMeter.java:281)
        at de.avanux.smartapplianceenabler.meter.HttpElectricityMeter.pollPower(HttpElectricityMeter.java:259)
        at de.avanux.smartapplianceenabler.meter.PollPowerMeter.addValue(PollPowerMeter.java:70)
        at de.avanux.smartapplianceenabler.meter.PollPowerMeter$1.runTask(PollPowerMeter.java:54)
        at de.avanux.smartapplianceenabler.util.GuardedTimerTask.run(GuardedTimerTask.java:57)
        at java.base/java.util.TimerThread.mainLoop(Timer.java:556)
        at java.base/java.util.TimerThread.run(Timer.java:506)
2020-12-31 16:49:03,487 DEBUG [Timer-0] d.a.s.n.NotificationHandler [NotificationHandler.java:96] F-00000001-000000000006-00: Checking notification preconditions: errorCountPerDay=0
```

*Webmin*: If you find an error in [View Logfile](#webmin-logs), copy the time of the log entry (e.g. `15:37:22`) and add it for a new search `Only show lines with text` but omitting the last seconds value (this makes the above example `15:37:2`) so that you can also see the log entries from the seconds before the error.

## Version of Smart Appliance Enabler
Immediately after the start, the *Smart Appliance Enabler* logs its version. You can display this entry with the following command:
```console
sae@raspi:~ $ grep "Running version" /tmp/rolling-2020-12-31.log 
2020-12-31 14:36:22,435 INFO [main] d.a.s.Application [Application.java:49] Running version 1.6.7 2020-12-31 13:31
```

*Webmin*: In [View Logfile](#webmin-logs) enter `Running version` after `Only show lines with text` and press Refresh.
