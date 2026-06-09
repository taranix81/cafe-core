# Examples

## 1. Generating PlantUML diagram (C4) from BPMN workflow

```shell
application --srcType=BPMN --destType=PlantUML --srcFiles=*/*.bpmn --generate
```

Arguments

- srcType (configuration)
- destType (configuration)
- srcFiles (configuration)
- generate (execution)

Expected execution
(srcType | destType | srcFile) -> generate

Optional/ Mandatory

- generate -> (srcType , destType , srcFile)