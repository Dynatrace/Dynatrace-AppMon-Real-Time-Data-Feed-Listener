# Dynatrace-Real-Time-Data-Feed-Listener
Implements a listener for the Real Time Business Transaction Data Feed Feature of Dynatrace. It allows you to easily get LIVE metrics from Dynatrace. Can for instance be used during any automated test to enrich your reports!

Find more Dynatrace Plugins and Extension on the [Dynatrace Community Portal](https://community.dynatrace.com/community/display/DL/FastPacks+and+Plugins)

## Example: Get your Dynatrace App Metrics as Console Output while running your Load Test!
![](https://github.com/Dynatrace/Dynatrace-Real-Time-Data-Feed-Listener/blob/master/images/BTConfiguration.png)

# How to get this up & running?

## Step #0: Get Dynatrace Personal License

Simply register here and get your lifetime Dynatrace AppMon License: http://bit.ly/dtpersonal

## Step #1: Launch the console app!
Simply start the app. The app will listen on http://localhost:4001/test. Port and endpoint are configurable via command line option. By default the LIVE Data Stream output goes to the console but you can also use the "aggregator" output which will give you an aggragted output when you end the app

Usage: app [-port:4001] [-url:/test] [-aggregate] [-console]

## Step #2: Configure Dynatrace Real Time Data Feed

![](https://github.com/Dynatrace/Dynatrace-Real-Time-Data-Feed-Listener/blob/master/images/ServerConfiguration.png)

## Step #3: Configure your BT for Real Time Data Output and Execute some Traffice

You want to turn on these settings in the BT as highlighted in the screenshot. Once you have PurePaths coming in that map to that BT you will see data being recorded by the tool
![](https://github.com/Dynatrace/Dynatrace-Real-Time-Data-Feed-Listener/blob/master/images/BTConfiguration.png)
