# MagicDownloadHub

![demo](https://github.com/gugugiyu/MagicDownloadHub/assets/106458387/cb11be59-7a85-4a0d-8149-b065af322089)

A generic youtube with integrated search engine and multithreading downloader implemented based on *sealedtx/java-youtube-downloader*

## Features

- Text User Interface with colorized annotations
- A fully compatible search engine with filters
- Multithreading videos download
- Video Manager (WIP)

## Default Configuration
| settings                       | type    | default value       | description                                                  |
| :----------------------------- | :------ | :------------------ | :----------------------------------------------------------- |
| downloadPath                   | String  | "downloaded_videos" | The root download path                                       |
| openVideoAfterDownload         | Boolean | true                | For single video download only. Auto open up the video (if supported) when downloaded |
| timeoutTimeInMillisecond       | Long    | 1000 * 60 * 60 * 12 | Timeout time of a downloading thread if takes too long       |
| verifyVideoIDOnPayloadDownload | Boolean | false               | Verify if that videoId is existed or not. Useful to make sure you enter valid id |
| filterDuplicateVideoId         | Boolean | true                | Filter out duplicate videoId in your payload. Disable to download a video multiple times |

## Run with Maven

### Step 1: Clone the project

MagicDownloadHub currently required **SDK 21+** to run, you can download it from the official source of Oracle [here](https://www.oracle.com/java/technologies/downloads/). Also, the Text User Interface works only for utf-8 supported console only. For Window user, you can set the code page of the command prompt to be UTF-8 using [chcp](https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/chcp).

```sh
chcp 65001
```

Since the main repo java-youtube-downloader is experiencing some issues about the test cases, unit tests is temporarily disable in this repo. However, you may fix and build this with tests at any time.

Next, you can clone this repo (we don't support SSH cloning right now)

```sh
git clone https://github.com/gugugiyu/MagicDownloadHub.git
```
### Step 2: Install the dependencies

Then, build the jar file from the artifact

```sh
mvn clean install
```

### Step 3: Run the program

and finally run the executable jar

```shell
mvn exec:java -DmainClass=com.magic.Application
```

## Run with JAVA directly

This project comes pre-packaged into a jar file so that you can run with just java, follow the following command to run it:

```sh
git clone https://github.com/gugugiyu/MagicDownloadHub.git
cd target
java -Dfile.encoding="UTF-8" -cp magic-hub-1.0-SNAPSHOT.jar com.magic.Application
```

We can even further optimized the process above to just one click by creating a bat file that runs this program from just a click. Most convenient if you want a one-click application

```sh
chcp 65001
cd "PATH_TO_TARGET_FILE"
java -Dfile.encoding="UTF-8" -cp magic-hub-1.0-SNAPSHOT.jar com.magic.Application
pause
cd "PATH_TO_POST_APPLICATION DIRECTORY" # Left this blank if you want
```

*Note*: The [1] show saved video directory will **NOT** work before you download any video when run with java directly as it will be initialized on your first download (by either single or payload download) 

## Contribution

This project is under development, and any contributions are welcome
