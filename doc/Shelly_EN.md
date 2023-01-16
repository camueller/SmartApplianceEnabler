# Shelly Plug (S)
With the [Shelly plug](https://shelly.cloud/shelly-plug/) or the [Shelly Plug S](https://shelly.cloud/knowledge-base/devices/shelly-plug-s/), connected to the WLAN, a device can be switched and its current power consumption measured.

## meter usage
The following settings are required for use as an electricity meter (IP address or host name must be adjusted):

| Field                                     | Value                      |
|-------------------------------------------|----------------------------|
| Format                                    | JSON                       |
| URL                                       | http://192.168.1.1/meter/0 |
| Extraction path (using parameter `Power`) | $.power                    |

## switch usage
The following settings are required for use as a switch (IP address or host name must be adjusted):

| Field                | Value                                 |
|----------------------|---------------------------------------|
| Action "Einschalten" | http://192.168.1.xxx/relay/0?turn=on  |
| Action "Ausschalten" | http://192.168.1.xxx/relay/0?turn=off |

# Shelly 3EM
## meter usage
The following settings are required for use as an electricity meter (IP address or host name must be adjusted):

| Field                                     | Value                     |
|-------------------------------------------|---------------------------|
| Format                                    | JSON                      |
| URL                                       | http://192.168.1.1/status |
| Extraction path (using parameter `Power`) | $.total_power             |

# Shelly 4PM
With the [Shelly 4 Pro](https://shelly.cloud/shelly-4-pro/), which is connected to the WLAN, 4 devices can be switched and their current power consumption measured.

## meter usage
The following settings are required for use as an electricity meter (# must be replaced by the number of the channel [0,1,2,3]; IP address or host name must be adjusted):

| Field                                      | Value                                        |
|--------------------------------------------|----------------------------------------------|
| Format                                     | JSON                                         |
| URL                                        | http://192.168.1.1/rpc/Switch.GetStatus?id=# |
| Extraction path (using parameter `Power`)  | $.apower                                     |

## switch usage
The following settings are required for use as a switch (# must be replaced by the number of the channel [0,1,2,3]; IP address or host name must be adjusted):

| Field                                  | Value                              |
|----------------------------------------|------------------------------------|
| Format                                 | JSON                               |
| URL                                    | http://192.168.1.1/relay/#?turn=on |

# Shelly Plus 1PM
With the [Shelly Plus 1PM](https://shelly.cloud/shelly-plus-1pm/), which is connected to the WLAN, a device can be switched on and its current power consumption measured.

## meter usage
The following settings are required for use as an electricity meter (IP address or host name must be adjusted):

| Field                                      | Value                                   |
|--------------------------------------------|-----------------------------------------|
| Format                                     | JSON                                    |
| URL                                        | http://192.168.1.1/rpc/Shelly.GetStatus |
| Extraction path (using parameter `Power`)  | $.switch:0.apower                       |

## switch usage
The following settings are required for use as a switch (IP address or host name must be adjusted):

| Field                          | Value                              |
|--------------------------------|------------------------------------|
| Format                         | JSON                               |
| URL                            | http://192.168.1.1/relay/0?turn=on |
