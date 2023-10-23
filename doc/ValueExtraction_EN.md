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

[RegEx101](https://regex101.com/) can be used to test whether the selected regular expression extracts the desired value from the response. The advantage of this online tool is it's ability to evaluate the input right as you type it in. So no need to click some buttons and wait for the result after each modification.

In addition to the regular expression, you need the response from which the value is to be extracted. If the *Smart Appliance Enabler* is already communicating with this device, its response can be found in the log.

**If a regular expression is configured, no value should be configured as `Format` in the *Smart Appliance Enabler*!**

If the results are available as key-value-pairs, it must taken into account, that the given regular expression is applied to the whole string! So not only the value. This can be evaluated with [RegEx101](https://regex101.com/) too. 

In the following example a meter value for a heat pump is given as key-value-pair:

![MeteringKeyValueExample](../pics/MeteringKeyValueExample.png)

So for this heat pump the regular expression and the test string is as following:

_Regular Expression_: `(\d+.?\d*)`

_Test String_: `waermepumpe=235.419998`

![RegEx101-Example](../pics/RegEx101-Example.png)

The value `235.419998` was successfully extracted.

Within *Smart Appliance Enabler* the following regular expression needs to be configured:

`Regex für Extraktion`: `(\d+.?\d*)`

`Format`: leave empty
