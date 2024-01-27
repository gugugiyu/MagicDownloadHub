# MagicDownloadHub

A generic youtube with integrated search engine and multithreading downloader implemented based on sealedtx/java-youtube-downloader

## Features

- Text User Interface with colorized annotations
- A fully compatible search engine with filters
- Multithreading videos download
- Video Manager (WIP)

## Step 1: Clone this project

MagicDownloadHub currently required **SDK 21+** to run, you can download it from the official source of Oracle [here](https://www.oracle.com/java/technologies/downloads/)

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