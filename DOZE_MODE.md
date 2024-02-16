# Doze Mode Testing

## Enter

```shell
adb shell dumpsys battery unplug
adb shell dumpsys deviceidle force-idle
```


## Exit

```shell
adb shell dumpsys deviceidle unforce
adb shell dumpsys battery reset
```