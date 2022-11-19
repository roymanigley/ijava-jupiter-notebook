# JupyterHelper

## Build

1. Build using Apache Maven
    ```
    mvn clean package
    ```

2. Copy to the `notebooks/libs` directory
    ```
    cp target/JupyterHelper-1.0-SNAPSHOT-jar-with-dependencies.jar ../notebooks/libs
    ```
## Usage in Jupyter Notebook

```
%jars libs/JupyterHelper-1.0-SNAPSHOT-jar-with-dependencies.jar
```

```
import ch.bytecrowd.JupyterHelper;
import ch.bytecrowd.JupyterHelper.XYSeries;

var series = new XYSeries("TEST", List.of(1,2,3,4,5),List.of(1,2,3,4,5));
JupyterHelper.plotScatterChart(List.of(series));
```