Smart Pantry Manager

A Java Android app designed to help cut down on food waste. Users can log the ingredients they have at home, and the app recommends recipes they're able to make right now.

Created for Mobile App Development 700 (Richfield) by Deanna Govender, student number 402312375.

The strict-matching rule
A recipe gets recommended only if ALL ingredients are available in the pantry in at least the needed amount. Recipes that are missing just one ingredient go into a separate "Almost there" list instead.
Matching handles different forms like singular and plural versions (tomato/tomatoes), different English spellings (aubergine/eggplant), and different measurement units (kg/g, tbsp/ml).

Features
-Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
-19 recipes pre-loaded on first run
-Suggested Recipes screen (strict matches) plus an "Almost there" tab
-Recipe detail screen with ingredient ticks/crosses and a "Cook this" button
-Expiry colour coding, an expiring-soon banner and "use it up" recipe priority
-Settings screen (alerts switch, priority switch, sample pantry tool)
-Input validation and a friendly message when no recipes match

Database choice: SQLite
I picked SQLite (using SQLiteOpenHelper) because it works directly on your phone, doesn't need internet, an account, or a server, and is perfect for keeping personal stuff like your pantry private. It's also what we learned about in the module's section on saving data.

Setup and run instructions
1.Install Android Studio and clone this repository(git clone https://github.com/[your-username]/smart-pantry-manager.git).
2.In Android Studio go to File > Open and pick the cloned folder.
3.Let Gradle finish syncing.
4.Make an emulator in Device Manager (like Pixel 8, API 34 or newer) or plug in a phone with USB debugging turned on.
5.Click Run.
6.To test it fast: go to Settings and tap "Add sample pantry", then check out the Recipes tab.

Tests
Run MatcherTest (app/src/test) to check the matching logic.

Technology
Java, Android Studio, SQLite, RecyclerView, Material Components.
