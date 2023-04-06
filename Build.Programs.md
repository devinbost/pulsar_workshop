
# 1. Build the Scenario Programs

The scenario programs included in this GitHub repository may be written in different languages such as Java, Python, Go, etc. The building process of these programs may vary from language to language.

## 1.1. Build Java Programs

All Java based programs in this GitHub repository are organized in a single [`Apache Maven`](https://maven.apache.org/) project in a structure like below:
```
main
  |--- base-code
  |--- <scenario_1_main_module>
  |      |--- <scenario_1_sub_module_1>
  |      |--- <scenario_1_sub_module_2>
  |      |--- ... ...
  |--- <scenario_2_main_module>
  |--- ... ...
``` 

In the above structure,
* The `main` module is the "root" parent module for all other modules
* The `base-code` module contains some common codes that are used by scenario specific modules. 
* Each scenario specific module may or may not have its own submodules.

In order to build all scenarios, go to `scenarios` folder and run the following command:
```
mvn clean install
```