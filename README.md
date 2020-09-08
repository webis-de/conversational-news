# conversational-news
Repository for the [Google DNI project Conversational News](https://newsinitiative.withgoogle.com/dnifund/dni-projects/conversational-news/). 

## Listenability Tools

Compile:
```
./gradlew shadowjar
```

Listenability analysis of the example data:
```
java -cp build/libs/conversational-news-open-0.1.0-all.jar de.webis.listenability.Analyze --input example-data --output analyzed-example-data.zip
```

Running an example service for re-writing suggestions using Netspeak synonyms (with a very basic interface):
```
java -cp build/libs/conversational-news-open-0.1.0-all.jar de.aitools.commons.uima.Serve --configuration src/main/resources/conf/service.conf
```
Then go to [http://localhost:8888/index.html](http://localhost:8888/index.html), type in "I know much" and hit "Analyze". This service is also available [here](https://listenability.webis.de/index.html).


## Editor Demo

The example service (see above) also runs the editor demo. It uses new WebRTC features that are so far available in Google Chrome only. Go to [http://localhost:8888/editor-demo.html](http://localhost:8888/editor-demo.html). This demo is also available [here](https://demo.webis.de/conversational-news/editor-demo.html).


## More

We are currently still in the progress of uploading here and simultaneously deploying the achievements of the project.
