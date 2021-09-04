# RoadBook

Track, log and save automated driving statistics such as distance, time, and weather. 

## Installation
Clone this repository and import into **Android Studio**
```bash
git clone git@github.com:tlm9201/RoadBook.git
```

## Configuration
### API Keys:
Generate API Keys using the following free APIs:
* [OpenWeatherMap](https://openweathermap.org/api)
* [Radar.io](https://radar.io/product/api)

And place both API keys under `local.properties` file:
```
WEATHER_API_KEY=...
RADAR_API_KEY=...
```

## Maintainers
This project is created and mantained by:
* [Timophey McGrath](http://github.com/tlm9201)

## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Push your branch (git push origin my-new-feature)
5. Create a new Pull Request
