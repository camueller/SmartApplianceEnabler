# Value extraction
In the case of queries, the *Smart Appliance Enabler* often receives a comprehensive response (XML, JSON, ...) from which the numerical value actually must first be extracted.

For this purpose, either a (JSON) path (field name `Extraction path`) or a regular expression (field name `Regex for extraction`) can be configured at various places in the *Smart Appliance Enabler*.

## Using JSON path
This value extraction method only works if the response is in JSON format!

Whether a response is in JSON format can be seen relatively easily from the curly brackets, which determine its structure.

The JSON path can be determined relatively easily with the [JSON Path Finder](https://jsonpathfinder.com/): Simply insert the JSON on the _left side_ (can be taken from the log if the *Smart Appliance Enabler* already communicated with this device). Then you can open the data structure on the _right side_ and select the desired value. The respective JSON path is then displayed above, beginning with `x`. When entering the path in the *Smart Appliance Enabler*, this `x` must be replaced by a `$`.

![JSON Path Finder](../pics/JsonPathFinder.png)

For the example above, the *Smart Appliance Enabler* must be configured:

`Extraction path`: `$.StatusSNS.ENERGY.Power`

In order for the *Smart Appliance Enabler* to know that the response should be interpreted as JSON, `JSON` must also be specified as `Format`!

## Using regular expression (aka regex)

Value extraction with a [Regular Expression](http://www.regexe.de/hilfe.jsp) always works. However, their formulation is not immediately clear to everyone.

This [Java Regex Tester](https://www.freeformatter.com/java-regex-tester.html) can be used to test whether the selected regular expression extracts the desired value from the response.

In addition to the regular expression, you need the response from which the value is to be extracted. If the *Smart Appliance Enabler* is already communicating with this device, its response can be found in the log.

The names shown in italics refer to the corresponding fields on the Regex Tester page.

_Java Regular Expression_: `.*"Power":(\d+).*`

_Entry to test against_
```json
{"StatusSNS":{"Time":"2019-09-06T20:06:19","ENERGY":{"TotalStartTime":"2019-08-18T11:07:55","Total":0.003,"Yesterday":0.000,"Today":0.003,"Power":26,"ApparentPower":25,"ReactivePower":25,"Factor":0.06,"Voltage":239,"Current":0.106}}}
```

_Replace with (Optional)_: `$1`

_Flags_: `[x] Dotall`

After clicking on `REPLACE FIRST` the following is displayed:

```
Results
.matches() method: true
.lookingAt() method: true
String replacement result:
26
```

So the value 26 was successfully extracted from the response using the regular expression above.

If a regular expression is configured, no value should be configured as `Format` in the *Smart Appliance Enabler*!

In the *Smart Appliance Enabler* the following must be configured for this example:

`Extraction regex`: `.*"Power":(\d+).*`

`Format`: leave empty
