# conversational-news
Repository for the [Google DNI project Conversational News](https://newsinitiative.withgoogle.com/dnifund/dni-projects/conversational-news/). 

## Listenability Tools

Compile:
```
./gradlew shadowjar
```

Listenability analysis of the example data:
```
java -cp build/libs/conversational-news-0.1.0-all.jar de.webis.listenability.Analyze --input example-data --output analyzed-example-data.zip
```

Running an example service for re-writing suggestions using Netspeak synonyms (with a very basic interface):
```
java -cp build/libs/conversational-news-0.1.0-all.jar de.aitools.commons.uima.Serve --configuration src/main/resources/conf/service.conf
```
Then go to [http://localhost:8888/index.html](http://localhost:8888/index.html), type in "I know much" and hit "Analyze". This service is also available [here](https://listenability.webis.de/index.html).
