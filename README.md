# MagicDownloadHub

![demo](https://github.com/gugugiyu/MagicDownloadHub/assets/106458387/cb11be59-7a85-4a0d-8149-b065af322089)

A generic youtube with integrated search engine and multithreading downloader implemented based on *sealedtx/java-youtube-downloader*

## Features

- Text User Interface with colorized annotations
- A fully compatible search engine with filters
- Multithreading videos download
- Video Manager (WIP)

## Step 1: Clone this project

MagicDownloadHub currently required **SDK 21+** to run, you can download it from the official source of Oracle [here](https://www.oracle.com/java/technologies/downloads/). Also, the Text User Interface works only for utf-8 supported console only. For Window user, you can set the code page of the command prompt to be UTF-8 using [chcp](https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/chcp).

```sh
chcp 65001
```

Since the main repo java-youtube-downloader is experiencing some issues about the test cases, unit tests is temporarily disable in this repo. However, you may fix and build this with tests at any time.

Next, you can clone this repo (we don't support SSH cloning right now)

```sh
git clone https://github.com/gugugiyu/MagicDownloadHub.git
```
You can also switch to the no test branch

```sh
git checkout main-(no-test)
```

## Step 2: Install the dependencies

Then, build the jar file from the artifact

```sh
mvn clean install
```

## Step 3: Run the application

and finally run the executable jar

```shell
mvn exec:java -DmainClass=com.magic.Application
```

## Contribution

This project is under development, and any contributions are welcome
