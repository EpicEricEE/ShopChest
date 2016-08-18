# ShopChest
ShopChest - Spigot/Bukkit Plugin

## API
To use the API, you need to add the following repository and dependency in your maven project:

```xml
<repositories>
  <repository>
    <id>shopchest-repo</id>
    <url>https://epicericee.github.io/ShopChest/maven/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>de.epiceric</groupId>
    <artifactId>ShopChest</artifactId>
    <version>1.10.2</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

You can find the javadoc here: https://epicericee.github.io/ShopChest/javadoc/

## Build
Clone this repository and use ``mvn clean package`` or ``mvn clean install``.
After the build succeeded, the ShopChest.jar is found in the ``/target/`` folder.

## Issues
If you find any issues, please provide them in the [Issues Section](https://github.com/EpicEricEE/ShopChest/issues) with a good description of how to reproduce it. If you get any error messages in the console, please also provide them.


## Download
You can view this resource/plugin on the official spigot page here: https://www.spigotmc.org/resources/shopchest.11431/
