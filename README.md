# CopySnap

> Lightweight incremental snapshot backup tool for local directories (Time Machine–style)

CopySnap creates space-efficient, incremental backups of a directory by tracking file changes using modification time and content hashing.

It is designed for local backup drives only.

---

## Features

- Incremental snapshots (only changed files are copied)
- Content + modification-time based change detection
- Space-efficient storage via file reuse/linking
- Simple CLI workflow
- Ignore patterns (glob-style)
- Human-readable snapshot structure

---

## Quick Start

```shell
# 1. Go to your backup location
cd /path/to/backup/location

# 2. Initialize a backup context
copysnap init /home/user/Pictures .venv **/cache/** .trash **/*.log

# 3. Check status
copysnap status

# 4. Create first snapshot
copysnap snapshot
```

## Installation

Run the provided `install.sh` after cloning this repo.

You must provide a version available on the [release page](https://github.com/johannesbuchholz/copysnap/releases).

Versions follow semantic versioning in the format 1.2.3.
To install a different version, rerun the script with the desired version:
```shell
./install.sh <version>
```

Alternatively, you may run this one-liner below. You still need to fill in a specific version.
```shell
wget --quiet --show-progress -O install.sh https://raw.githubusercontent.com/johannesbuchholz/copysnap/master/install.sh && sh install.sh <version>
```

## Build from source

CopySnap is built with [mvn](https://maven.apache.org/index.html).

Clone the repository and build the project using:
```shell
mvn clean package
```

This command runs all unit tests. Tests may create temporary files in your system’s temporary directory.
The resulting JAR file will be located at `target/copysnap-<version>.jar`.

Run it with:
```shell
java -jar target/copysnap-<version>.jar
```

## Usage
The basic workflow is:
### 1. Choose a backup location
Navigate to the directory where snapshots should be stored:
 ```shell
 cd /path/to/backup/location
 ```

### 2. Initialize a context
Initialize a copysnap context and provide the path to the source directory that you want to back up. You may also add glob-like patterns or directory/file names to exclude.
```shell
copysnap init /home/johannes/Pictures .venv **/my/personal/data/** .trash **/*.yaml 
```

### 3. Check status
Inspect the current context:
```shell
copysnap status
```

Example output:
```shell
[INFO] Current context
source : /home/johannes/Pictures
home   : /path/to/backup/location/Pictures-copysnap
created: 2026-06-21-09-50-35
ignore : .venv:**/my/personal/data/**:.trash:**/*.yaml
latest snapshot
none
[INFO] App properties: /home/johannes/.copysnap/copysnap.properties
[INFO] App version: 0.21.1
```

### 4. Create a snapshot
Run:
```shell
copysnap snapshot
```

The first run may take some time. Example output:
```shell
[INFO] Loading latest snapshot file system state - started: 2026-06-21-09-55-09, from: /path/to/backup/location/Pictures-copysnap
[INFO] Could not find latest snapshot in /path/to/backup/location/Pictures-copysnap. Loading with empty file system state.
[INFO] Creating new snapshot - started: 2026-06-21-09-55-09, at: /path/to/backup/location/Pictures-copysnap/2026-06-21-09-55-09, createPlainCopiesOnly: false
[INFO] Computing file differences - started: 2026-06-21-09-55-09, at: /home/johannes/Pictures
[INFO] File count statistics:
    new: 18304
    changed: 0
    removed: 0
    unchanged: 0
    ignored: 0
    erroneous: 0
[INFO] Done computing file differences (1643 ms)
[INFO] Applying copy actions - started: 2026-06-21-09-55-10, count: 18304
Writing files [#-------------------------------]   724/18304 (  4%)
```

#### 5. Snapshot structure
After completion, your backup directory will look like this:
   ```text
   Pictures-copysnap/
   ├── 2026-06-21-09-55-09
   │  ├── Pictures
   │  │   └── ...
   │  ├── .latest.db
   │  └── report.txt
   └── context.properties
   ```
- `2026-06-21-09-55-09`: Contains the backed-up state.
- `.latest.db`: Internal metadata used to detect changes in future snapshots.
- `report.txt`: Detailed log of detected files and copy operations. Can be safely deleted if not needed.

### 6. Incremental snapshots
Subsequent runs of `copysnap snapshot` only copy new or changed files. Unchanged files are reused from previous snapshots via linking, making snapshots space-efficient. 

### 7. Switching contexts
Use the following command to switch to a different CopySnap context:
```shell
copysnap load /path/to/another/context/OtherContext-copysnap`
```

You can verify that the context was loaded correctly with `copysnap status`. 

## Help
You can view available options for any command using:
```shell
copysnap <command> --help
```