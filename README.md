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

### Not now / Maybe later

* List of tags
* Support for images and attachments  
* Some kind of sync

### Probably never

* Edit notes
* View encrypted notes

## Screenshots

![Note List](docs/screenshot_note_list.png) ![Note](docs/screenshot_note.png)

## Sync

There is currently no sync functionality built into Eisbär. To sync data between the android phone and the desktop app, you will need to update the sqlite database file either manually, or using a third-party solution such as [Syncthing](https://syncthing.net).

Every time the app process is started, it copies the sqlite file into the private app data folder. If the sqlite file was updated, you need to stop the Eisbär app process, e.g. by removing it from the recent apps screen. Then start Eisbär again and the app will read the updated sqlite file.

Working with an external sqlite database file appears to not be possible in android. Android apps can only open sqlite databases located in a specific folder in the private app data directory. That's why Eisbär copies the sqlite database when the app is launched. This may take a few seconds if you have a lot of notes.

Reacting to database changes will need some work or may change to some kind of online sync in the future.

## Why requery and not Room?

This app uses [requery](https://github.com/requery/requery/) to read the SQLite database because it supports partial models. With Room, the model entities must match the exact table specifications or the database cannot be opened. 

## Credits

#### Logo image
copyright: [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/), based on an image from [creativetail](https://www.creativetail.com)

source: https://www.brandeps.com/icon/B/Bear-01

