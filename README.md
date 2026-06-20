# CopySnap

CopySnap is a lightweight command-line tool for creating incremental backup snapshots of a target directory, similar to macOS Time Machine.

Snapshots are computed based on the last modified date and actual file contents. Currently, CopySnap only supports copying files to a locally available drive.


## Installation

Run the provided `./install.sh` like this
```shell
sh install.sh <version>
```

You must provide a version available on the [release page](https://github.com/johannesbuchholz/copysnap/releases).
Versions must be specified in the format `1.2.3`.
To install a different version, simply rerun the script with the desired version.

## Build from source

Copysnap is built using [mvn](https://maven.apache.org/index.html).

Clone this repository and then build, test, and package the project using the Maven `package` goal:
```shell
mvn clean package
```

This also runs unit tests, which create temporary files in your system's temporary directory.
The resulting `.jar` file `target/copysnap-<version>.jar` can then be run using
```shell
java -jar target/copysnap-<version>.jar
```