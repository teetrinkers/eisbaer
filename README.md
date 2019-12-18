# Eisbär

![Eisbär Logo](app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png)

An android application for [Bear](https://bear.app).

This app can browse notes from an SQLite database which was edited using the Bear MacOS app.

## Usage

* Copy the [Bear database file](https://bear.app/faq/Where%20are%20Bear%27s%20notes%20located/) to the downloads folder of your phone.
* In the Eisbär app, select the database file in the settings.
* In the main search view, type anything into the search field. This will refresh the view.

## Features

* View notes
* Search notes
* Rendered Markdown
* List of tags

### Not now / Maybe later

* Support for images and attachments  

### Probably never

* Edit notes
* View encrypted notes

## Screenshots

![Note List](docs/screenshot_note_list.png) ![Note](docs/screenshot_note.png)

## Sync

There is currently no sync functionality built into Eisbär. To sync data between the android phone and the desktop app, you will need to update the sqlite database file either manually, using a third-party solution such as [Syncthing](https://syncthing.net), or by copying it to Google Drive.

On startup and then every 10 seconds, Eisbär checks the database file for changes, i.e., different size or modification date. If changes are detected, the external database file is copied into the private app data directory and the internal database is reopened. In the note list view, you can also use swipe-to-refresh to tell the document provider which provides the external database file, e.g. Google Drive, to refresh / download the file.

Working with an external sqlite database file appears to not be possible in android. Android apps can only open sqlite databases located in a specific folder in the private app data directory. That's why Eisbär copies the sqlite database when it detects changes to the external file. This may take a few seconds if you have a lot of notes.

## Why requery and not Room?

This app uses [requery](https://github.com/requery/requery/) to read the SQLite database because it supports partial models. With Room, the model entities must match the exact table specifications or the database cannot be opened. 

## Credits

#### Logo image
copyright: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/), based on an image from [creativetail](https://www.creativetail.com)

source: https://www.brandeps.com/icon/B/Bear-01

