# Papaya

![Papaya Logo](resources/Logo-128.png)

## Dependencies

- Kotlin 2.0.21 (or greater)
- Java 21
- For the example HTTP server:
  - [Python](https://www.python.org/) 3.12 (or greater)
  - [Pipenv](https://pipenv.pypa.io/en/latest/index.html) 2024.3.1 (or greater)

## Compilation

Use Gradle compilation system to generate a Fat Jar
```bash
./gradlew shadowJar
```

The Jar file has been generated in `build/libs/`

## Running

Running is straight forward once you have set up your YAML configuration file and your
web server is running:

```bash
java -jar build/libs/Papaya-1.0-SNAPSHOT-all.jar <configuration_file>.yaml
```

> If you have multiple Java versions in your system, you may need to specified like:
> ```bash
> /usr/lib/jvm/java-21-openjdk/bin/java -jar build/libs/Papaya-1.0-SNAPSHOT-all.jar <configuration_file>.yaml
> ```

There is a very simple web HTTP server distributed alongside its configuration file with this project.
For installing the server dependencies go to `development_server` and install all dependencies in the Pipfile.
Then, activate the environment and run `main.py`
```bash
cd development_server/
pipenv install
pipenv shell  # Activates the Python environment
fastapi dev main.py  # Runs the server
```

Once the server is running, the example YAML may be run as follows:
```bash
java -jar build/libs/Papaya-1.0-SNAPSHOT-all.jar development_server/server_config.yaml
```

 
## Creating a YAML configuration file

The YAML configuration file is explained [in its own document](CONFIGURATION_FILE.md).

## Team

| Name                  |    ID | Email address                                     |
|:----------------------|------:|:--------------------------------------------------|
| Pedro J. López Guzman | 60711 | [pelopez@itba.edu.ar](mailto:pelopez@itba.edu.ar) |
| Martín E. Zahnd       | 60401 | [mzahnd@itba.edu.ar](mailto:mzahnd@itba.edu.ar)   |

