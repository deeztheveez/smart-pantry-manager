# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user records the ingredients they
already have at home, and the app suggests only the recipes they can cook right now.

Built for Mobile App Development 700 (Richfield) by [Your name], student number [number].

## The strict-matching rule
A recipe is suggested only if EVERY ingredient is in the pantry in at least the required
quantity. Recipes with one ingredient missing are kept in a separate "Almost there" list.
Matching copes with singular/plural names (tomato/tomatoes), British/American names
(aubergine/eggplant) and unit differences (kg/g, tbsp/ml).

## Features
- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- 19 recipes pre-loaded on first run
- Suggested Recipes screen (strict matches) plus an "Almost there" tab
- Recipe detail screen with ingredient ticks/crosses and a "Cook this" button
- Expiry colour coding, an expiring-soon banner and "use it up" recipe priority
- Settings screen (alerts switch, priority switch, sample pantry tool)
- Input validation and a friendly message when no recipes match

## Database choice: SQLite
I chose SQLite (using SQLiteOpenHelper) because it runs on the phone itself, needs no
internet connection, account or server, and suits private, personal data like a pantry.
It is also the approach covered in the module's persistent data chapter.

## Setup and run instructions
1. Install Android Studio and clone this repository
   (`git clone https://github.com/[your-username]/smart-pantry-manager.git`).
2. In Android Studio choose File > Open and select the cloned folder.
3. Wait for Gradle sync to finish.
4. Create an emulator in Device Manager (e.g. Pixel 8, API 34 or higher) or connect a phone
   with USB debugging on.
5. Press Run.
6. To try it quickly: open Settings and tap "Add sample pantry", then look at the Recipes tab.

## Tests
Run `MatcherTest` (app/src/test) to check the matching logic.

## Technology
Java, Android Studio, SQLite, RecyclerView, Material Components.